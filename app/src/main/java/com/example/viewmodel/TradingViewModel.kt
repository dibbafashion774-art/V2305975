package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CryptoPairInfo
import com.example.data.model.MarketTrade
import com.example.data.model.OrderBookEntry
import com.example.data.model.OrderItem
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.repository.MarketRepository
import com.example.data.repository.TradingRepository
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

class TradingViewModel(
    private val tradingRepository: TradingRepository,
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _selectedSymbol = MutableStateFlow("BTCUSDT")
    val selectedSymbol: StateFlow<String> = _selectedSymbol.asStateFlow()

    val currentPair: StateFlow<CryptoPairInfo?> = combine(
        marketRepository.pairsFlow,
        _selectedSymbol
    ) { pairs, sym ->
        pairs.find { it.symbol == sym } ?: pairs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _side = MutableStateFlow(OrderSide.BUY)
    val side: StateFlow<OrderSide> = _side.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.MARKET)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _priceInput = MutableStateFlow("")
    val priceInput: StateFlow<String> = _priceInput.asStateFlow()

    private val _quantityInput = MutableStateFlow("")
    val quantityInput: StateFlow<String> = _quantityInput.asStateFlow()

    private val _selectedPercentage = MutableStateFlow<Int?>(null)
    val selectedPercentage: StateFlow<Int?> = _selectedPercentage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    // Balances
    val availableUsdt: StateFlow<Double> = tradingRepository.portfolioSummary.combine(MutableStateFlow(Unit)) { summary, _ ->
        summary.availableBalanceUsdt
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7_000_000.0)

    val availableCoinQty: StateFlow<Double> = combine(
        tradingRepository.portfolioSummary,
        _selectedSymbol
    ) { summary, sym ->
        val baseAsset = sym.replace("USDT", "")
        summary.holdings.find { it.symbol == baseAsset }?.quantity ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val openOrders: StateFlow<List<OrderItem>> = tradingRepository.openOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderHistory: StateFlow<List<OrderItem>> = tradingRepository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orderbook & Trades
    private val _orderBook = MutableStateFlow<Pair<List<OrderBookEntry>, List<OrderBookEntry>>>(Pair(emptyList(), emptyList()))
    val orderBook: StateFlow<Pair<List<OrderBookEntry>, List<OrderBookEntry>>> = _orderBook.asStateFlow()

    private val _recentTrades = MutableStateFlow<List<MarketTrade>>(emptyList())
    val recentTrades: StateFlow<List<MarketTrade>> = _recentTrades.asStateFlow()

    private var liveBookJob: Job? = null

    init {
        // Sync default price when pair updates
        viewModelScope.launch {
            currentPair.collect { pair ->
                if (pair != null && _priceInput.value.isBlank()) {
                    _priceInput.value = String.format("%.2f", pair.currentPrice)
                }
            }
        }
        startPollingOrderBook(_selectedSymbol.value)
    }

    fun setPair(symbol: String) {
        _selectedSymbol.value = symbol
        val pair = marketRepository.pairsFlow.value.find { it.symbol == symbol }
        if (pair != null) {
            _priceInput.value = String.format("%.2f", pair.currentPrice)
        }
        _quantityInput.value = ""
        _selectedPercentage.value = null
        startPollingOrderBook(symbol)
    }

    fun setSide(newSide: OrderSide) {
        _side.value = newSide
        _quantityInput.value = ""
        _selectedPercentage.value = null
    }

    fun setOrderType(type: OrderType) {
        _orderType.value = type
        if (type == OrderType.MARKET) {
            val p = currentPair.value?.currentPrice ?: 0.0
            _priceInput.value = String.format("%.2f", p)
        }
    }

    fun setPrice(price: String) {
        _priceInput.value = price
        recalcQuantityIfPercent()
    }

    fun setQuantity(qty: String) {
        _quantityInput.value = qty
        _selectedPercentage.value = null
    }

    fun selectPriceFromBook(price: Double) {
        _priceInput.value = String.format("%.2f", price)
        recalcQuantityIfPercent()
    }

    fun incrementPrice(delta: Double = 1.0) {
        val curr = _priceInput.value.toDoubleOrNull() ?: currentPair.value?.currentPrice ?: 0.0
        val next = (curr + delta).coerceAtLeast(0.0001)
        _priceInput.value = String.format("%.2f", next)
        recalcQuantityIfPercent()
    }

    fun decrementPrice(delta: Double = 1.0) {
        val curr = _priceInput.value.toDoubleOrNull() ?: currentPair.value?.currentPrice ?: 0.0
        val next = (curr - delta).coerceAtLeast(0.0001)
        _priceInput.value = String.format("%.2f", next)
        recalcQuantityIfPercent()
    }

    fun selectPercentage(percent: Int) {
        _selectedPercentage.value = percent
        applyPercentageCalculation(percent)
    }

    private fun recalcQuantityIfPercent() {
        val p = _selectedPercentage.value
        if (p != null) {
            applyPercentageCalculation(p)
        }
    }

    private fun applyPercentageCalculation(percent: Int) {
        val ratio = percent / 100.0
        val p = _priceInput.value.toDoubleOrNull() ?: currentPair.value?.currentPrice ?: 0.0

        if (_side.value == OrderSide.BUY) {
            val avail = availableUsdt.value
            if (p > 0.0 && avail > 0.0) {
                // fee is 0.1%
                val maxSpendable = (avail * ratio) / 1.001
                val qty = maxSpendable / p
                _quantityInput.value = if (qty < 1.0) String.format("%.6f", qty) else String.format("%.4f", qty)
            }
        } else {
            val availCoin = availableCoinQty.value
            val qty = availCoin * ratio
            _quantityInput.value = if (qty < 1.0) String.format("%.6f", qty) else String.format("%.4f", qty)
        }
    }

    fun submitOrder() {
        if (_isSubmitting.value) return
        val pair = currentPair.value ?: return

        val p = if (_orderType.value == OrderType.MARKET) {
            pair.currentPrice
        } else {
            _priceInput.value.toDoubleOrNull() ?: 0.0
        }

        val q = _quantityInput.value.toDoubleOrNull() ?: 0.0

        if (p <= 0.0) {
            showFeedback("Invalid price: Must be greater than zero.", true)
            return
        }

        if (q <= 0.0) {
            showFeedback("Invalid amount: Enter a valid quantity to trade.", true)
            return
        }

        val totalValue = q * p
        val fee = totalValue * 0.001

        if (_side.value == OrderSide.BUY && (totalValue + fee) > availableUsdt.value) {
            showFeedback("Insufficient USDT balance to execute order.", true)
            return
        }

        if (_side.value == OrderSide.SELL && q > availableCoinQty.value) {
            showFeedback("Insufficient ${pair.baseAsset} balance to sell.", true)
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = if (_orderType.value == OrderType.MARKET) {
                tradingRepository.executeMarketOrder(
                    symbol = pair.symbol,
                    baseAsset = pair.baseAsset,
                    coinName = pair.fullName,
                    side = _side.value,
                    quantity = q,
                    price = p
                )
            } else {
                tradingRepository.placeLimitOrder(
                    symbol = pair.symbol,
                    baseAsset = pair.baseAsset,
                    coinName = pair.fullName,
                    side = _side.value,
                    quantity = q,
                    limitPrice = p
                )
            }

            _isSubmitting.value = false
            result.onSuccess {
                _quantityInput.value = ""
                _selectedPercentage.value = null
                val action = if (_side.value == OrderSide.BUY) "Bought" else "Sold"
                val typeLabel = if (_orderType.value == OrderType.MARKET) "Market order filled" else "Limit order placed"
                showFeedback("$typeLabel: $action ${String.format("%.4f", q)} ${pair.baseAsset} at ${String.format("%.2f", p)} USDT", false)
            }.onFailure { err ->
                showFeedback(err.message ?: "Failed to execute order", true)
            }
        }
    }

    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            tradingRepository.cancelOrder(orderId)
                .onSuccess {
                    showFeedback("Order #$orderId cancelled successfully", false)
                }
                .onFailure {
                    showFeedback("Failed to cancel order: ${it.message}", true)
                }
        }
    }

    fun dismissFeedback() {
        _feedbackMessage.value = null
    }

    private fun showFeedback(msg: String, isErr: Boolean) {
        _feedbackMessage.value = msg
        _isError.value = isErr
        viewModelScope.launch {
            delay(4000)
            if (_feedbackMessage.value == msg) {
                _feedbackMessage.value = null
            }
        }
    }

    private fun startPollingOrderBook(symbol: String) {
        liveBookJob?.cancel()
        liveBookJob = viewModelScope.launch {
            while (isActive) {
                val book = marketRepository.fetchOrderBook(symbol)
                _orderBook.value = book
                val trades = marketRepository.fetchRecentTrades(symbol)
                _recentTrades.value = trades
                delay(2000)
            }
        }
    }
}
