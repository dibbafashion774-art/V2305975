package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Candlestick
import com.example.data.model.CryptoPairInfo
import com.example.data.model.MarketTrade
import com.example.data.model.OrderBookEntry
import com.example.data.network.WsConnectionState
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MarketCategory {
    ALL,
    FAVORITES,
    GAINERS,
    LOSERS,
    VOLUME
}

enum class SortType {
    DEFAULT,
    PRICE_DESC,
    PRICE_ASC,
    CHANGE_DESC,
    CHANGE_ASC,
    VOLUME_DESC,
    NAME_ASC
}

class MarketViewModel(
    private val marketRepository: MarketRepository
) : ViewModel() {

    val connectionState: StateFlow<WsConnectionState> = marketRepository.connectionState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MarketCategory.ALL)
    val selectedCategory: StateFlow<MarketCategory> = _selectedCategory.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    val allPairs: StateFlow<List<CryptoPairInfo>> = marketRepository.pairsFlow

    val hotPairs: StateFlow<List<CryptoPairInfo>> = marketRepository.pairsFlow
        .combine(MutableStateFlow(Unit)) { pairs, _ ->
            pairs.take(6)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topGainers: StateFlow<List<CryptoPairInfo>> = marketRepository.pairsFlow
        .combine(MutableStateFlow(Unit)) { pairs, _ ->
            pairs.sortedByDescending { it.priceChangePercent }.take(8)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topLosers: StateFlow<List<CryptoPairInfo>> = marketRepository.pairsFlow
        .combine(MutableStateFlow(Unit)) { pairs, _ ->
            pairs.sortedBy { it.priceChangePercent }.take(8)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPairs: StateFlow<List<CryptoPairInfo>> = combine(
        marketRepository.pairsFlow,
        _searchQuery,
        _selectedCategory,
        _sortType
    ) { pairs, query, category, sort ->
        var list = pairs

        // 1. Filter by category
        list = when (category) {
            MarketCategory.ALL -> list
            MarketCategory.FAVORITES -> list.filter { it.isFavorite }
            MarketCategory.GAINERS -> list.filter { it.priceChangePercent > 0 }.sortedByDescending { it.priceChangePercent }
            MarketCategory.LOSERS -> list.filter { it.priceChangePercent < 0 }.sortedBy { it.priceChangePercent }
            MarketCategory.VOLUME -> list.sortedByDescending { it.turnover24h }
        }

        // 2. Filter by search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.symbol.lowercase().contains(q) ||
                it.baseAsset.lowercase().contains(q) ||
                it.fullName.lowercase().contains(q)
            }
        }

        // 3. Apply sort
        when (sort) {
            SortType.DEFAULT -> list
            SortType.PRICE_DESC -> list.sortedByDescending { it.currentPrice }
            SortType.PRICE_ASC -> list.sortedBy { it.currentPrice }
            SortType.CHANGE_DESC -> list.sortedByDescending { it.priceChangePercent }
            SortType.CHANGE_ASC -> list.sortedBy { it.priceChangePercent }
            SortType.VOLUME_DESC -> list.sortedByDescending { it.turnover24h }
            SortType.NAME_ASC -> list.sortedBy { it.baseAsset }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Selected Coin Detail State ---
    private val _selectedPairSymbol = MutableStateFlow("BTCUSDT")
    val selectedPairSymbol: StateFlow<String> = _selectedPairSymbol.asStateFlow()

    val selectedPair: StateFlow<CryptoPairInfo?> = combine(
        marketRepository.pairsFlow,
        _selectedPairSymbol
    ) { pairs, symbol ->
        pairs.find { it.symbol == symbol } ?: pairs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedInterval = MutableStateFlow("15m")
    val selectedInterval: StateFlow<String> = _selectedInterval.asStateFlow()

    private val _isCandleMode = MutableStateFlow(true)
    val isCandleMode: StateFlow<Boolean> = _isCandleMode.asStateFlow()

    private val _klines = MutableStateFlow<List<Candlestick>>(emptyList())
    val klines: StateFlow<List<Candlestick>> = _klines.asStateFlow()

    private val _orderBook = MutableStateFlow<Pair<List<OrderBookEntry>, List<OrderBookEntry>>>(Pair(emptyList(), emptyList()))
    val orderBook: StateFlow<Pair<List<OrderBookEntry>, List<OrderBookEntry>>> = _orderBook.asStateFlow()

    private val _recentTrades = MutableStateFlow<List<MarketTrade>>(emptyList())
    val recentTrades: StateFlow<List<MarketTrade>> = _recentTrades.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    private var detailPollingJob: Job? = null

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: MarketCategory) {
        _selectedCategory.value = category
    }

    fun setSortType(sort: SortType) {
        _sortType.value = sort
    }

    fun toggleFavorite(symbol: String) {
        viewModelScope.launch {
            marketRepository.toggleFavorite(symbol)
        }
    }

    fun refresh() {
        marketRepository.refreshMarketData()
    }

    fun selectPair(symbol: String) {
        _selectedPairSymbol.value = symbol
        loadPairDetails(symbol)
    }

    fun selectInterval(interval: String) {
        _selectedInterval.value = interval
        loadKlines(_selectedPairSymbol.value, interval)
    }

    fun toggleChartMode() {
        _isCandleMode.value = !_isCandleMode.value
    }

    private fun loadPairDetails(symbol: String) {
        detailPollingJob?.cancel()
        detailPollingJob = viewModelScope.launch {
            _isDetailLoading.value = true
            loadKlines(symbol, _selectedInterval.value)
            _isDetailLoading.value = false

            // Poll orderbook and trades periodically for live realism
            while (isActive) {
                val book = marketRepository.fetchOrderBook(symbol)
                _orderBook.value = book
                val trades = marketRepository.fetchRecentTrades(symbol)
                _recentTrades.value = trades
                delay(2500)
            }
        }
    }

    private fun loadKlines(symbol: String, interval: String) {
        viewModelScope.launch {
            val candles = marketRepository.fetchKlines(symbol, interval)
            _klines.value = candles
        }
    }
}
