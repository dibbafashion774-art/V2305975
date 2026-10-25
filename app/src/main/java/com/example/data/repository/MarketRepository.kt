package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteEntity
import com.example.data.model.Candlestick
import com.example.data.model.CryptoPairInfo
import com.example.data.model.MarketTrade
import com.example.data.model.OrderBookEntry
import com.example.data.model.OrderBookResponse
import com.example.data.network.BinanceApiService
import com.example.data.network.BinanceWebSocketManager
import com.example.data.network.WsConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketRepository(
    private val apiService: BinanceApiService,
    private val webSocketManager: BinanceWebSocketManager,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Known metadata for popular cryptocurrencies
    private val coinMetadata = mapOf(
        "BTCUSDT" to Pair("BTC", "Bitcoin"),
        "ETHUSDT" to Pair("ETH", "Ethereum"),
        "BNBUSDT" to Pair("BNB", "BNB"),
        "SOLUSDT" to Pair("SOL", "Solana"),
        "XRPUSDT" to Pair("XRP", "Ripple"),
        "DOGEUSDT" to Pair("DOGE", "Dogecoin"),
        "ADAUSDT" to Pair("ADA", "Cardano"),
        "TRXUSDT" to Pair("TRX", "TRON"),
        "AVAXUSDT" to Pair("AVAX", "Avalanche"),
        "LINKUSDT" to Pair("LINK", "Chainlink"),
        "LTCUSDT" to Pair("LTC", "Litecoin"),
        "DOTUSDT" to Pair("DOT", "Polkadot"),
        "BCHUSDT" to Pair("BCH", "Bitcoin Cash"),
        "SUIUSDT" to Pair("SUI", "Sui"),
        "TONUSDT" to Pair("TON", "Toncoin"),
        "NEARUSDT" to Pair("NEAR", "Near Protocol"),
        "MATICUSDT" to Pair("POL", "Polygon"),
        "PEPEUSDT" to Pair("PEPE", "Pepe"),
        "SHIBUSDT" to Pair("SHIB", "Shiba Inu"),
        "APTUSDT" to Pair("APT", "Aptos")
    )

    private val _pairsFlow = MutableStateFlow<List<CryptoPairInfo>>(createInitialFallbackPairs())
    val pairsFlow: StateFlow<List<CryptoPairInfo>> = _pairsFlow.asStateFlow()

    val connectionState: StateFlow<WsConnectionState> = webSocketManager.connectionState

    init {
        // Start listening to live WebSocket ticker updates
        webSocketManager.startListening()
        listenToWebSocketUpdates()
        // Bootstrap with REST initial data
        refreshMarketData()
    }

    private fun listenToWebSocketUpdates() {
        scope.launch {
            webSocketManager.tickerUpdates.collect { wsTickers ->
                val current = _pairsFlow.value.toMutableList()
                val tickerMap = wsTickers.associateBy { it.symbol }
                var changed = false

                for (i in current.indices) {
                    val pair = current[i]
                    val wsItem = tickerMap[pair.symbol]
                    if (wsItem != null) {
                        val newPrice = wsItem.closePrice.toDoubleOrNull() ?: pair.currentPrice
                        if (newPrice > 0 && newPrice != pair.currentPrice) {
                            val openPrice = wsItem.openPrice?.toDoubleOrNull() ?: (pair.currentPrice - pair.priceChangeAmount)
                            val changeAmount = newPrice - openPrice
                            val changePercent = if (openPrice > 0) (changeAmount / openPrice) * 100.0 else pair.priceChangePercent
                            val newHigh = maxOf(pair.high24h, wsItem.highPrice?.toDoubleOrNull() ?: newPrice)
                            val newLow = minOf(pair.low24h, wsItem.lowPrice?.toDoubleOrNull() ?: newPrice)
                            val newVol = wsItem.volume?.toDoubleOrNull() ?: pair.volume24h
                            val newTurnover = wsItem.quoteVolume?.toDoubleOrNull() ?: pair.turnover24h

                            // Keep sparkline rolling
                            val updatedSparkline = (pair.sparklinePoints + newPrice.toFloat()).takeLast(20)

                            current[i] = pair.copy(
                                currentPrice = newPrice,
                                priceChangePercent = changePercent,
                                priceChangeAmount = changeAmount,
                                high24h = newHigh,
                                low24h = newLow,
                                volume24h = newVol,
                                turnover24h = newTurnover,
                                sparklinePoints = updatedSparkline,
                                lastUpdated = System.currentTimeMillis()
                            )
                            changed = true
                        }
                    }
                }
                if (changed) {
                    _pairsFlow.value = current
                }
            }
        }
    }

    fun refreshMarketData() {
        scope.launch {
            try {
                val remoteTickers = apiService.get24HrTickers()
                val favList = database.favoriteDao().getAllFavoriteSymbols().firstOrNull() ?: emptyList()
                val favSet = favList.toSet()

                val tickerMap = remoteTickers.associateBy { it.symbol }
                val updatedList = mutableListOf<CryptoPairInfo>()

                // Filter to supported pairs first, keeping order
                for ((symbol, meta) in coinMetadata) {
                    val ticker = tickerMap[symbol]
                    if (ticker != null) {
                        val price = ticker.lastPrice.toDoubleOrNull() ?: 0.0
                        val changePercent = ticker.priceChangePercent?.toDoubleOrNull() ?: 0.0
                        val changeAmount = ticker.priceChange?.toDoubleOrNull() ?: 0.0
                        val high = ticker.highPrice?.toDoubleOrNull() ?: price
                        val low = ticker.lowPrice?.toDoubleOrNull() ?: price
                        val vol = ticker.volume?.toDoubleOrNull() ?: 0.0
                        val turnover = ticker.quoteVolume?.toDoubleOrNull() ?: 0.0

                        // Generate a plausible sparkline based on high, low, open, close
                        val open = ticker.openPrice?.toDoubleOrNull() ?: price
                        val sparkline = generateSparkline(open, low, high, price)

                        updatedList.add(
                            CryptoPairInfo(
                                symbol = symbol,
                                baseAsset = meta.first,
                                quoteAsset = "USDT",
                                fullName = meta.second,
                                currentPrice = price,
                                priceChangePercent = changePercent,
                                priceChangeAmount = changeAmount,
                                high24h = high,
                                low24h = low,
                                volume24h = vol,
                                turnover24h = turnover,
                                sparklinePoints = sparkline,
                                isFavorite = favSet.contains(symbol),
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                    }
                }

                if (updatedList.isNotEmpty()) {
                    _pairsFlow.value = updatedList
                }
            } catch (e: Exception) {
                Log.e("MarketRepository", "Failed to fetch 24hr tickers: ${e.message}")
            }
        }
    }

    suspend fun getPair(symbol: String): CryptoPairInfo? {
        return _pairsFlow.value.find { it.symbol == symbol }
    }

    suspend fun fetchKlines(symbol: String, interval: String): List<Candlestick> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getKlines(symbol = symbol, interval = interval, limit = 80)
            response.mapNotNull { row ->
                try {
                    val openTime = (row[0] as? Number)?.toLong() ?: 0L
                    val open = (row[1] as? String)?.toFloatOrNull() ?: 0f
                    val high = (row[2] as? String)?.toFloatOrNull() ?: 0f
                    val low = (row[3] as? String)?.toFloatOrNull() ?: 0f
                    val close = (row[4] as? String)?.toFloatOrNull() ?: 0f
                    val volume = (row[5] as? String)?.toFloatOrNull() ?: 0f
                    val closeTime = (row[6] as? Number)?.toLong() ?: 0L
                    Candlestick(
                        openTime = openTime,
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume,
                        closeTime = closeTime
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("MarketRepository", "Error fetching klines for $symbol: ${e.message}")
            generateFallbackKlines(symbol)
        }
    }

    suspend fun fetchOrderBook(symbol: String): Pair<List<OrderBookEntry>, List<OrderBookEntry>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDepth(symbol = symbol, limit = 15)
            val bids = parseBookSide(response.bids)
            val asks = parseBookSide(response.asks)
            Pair(bids, asks)
        } catch (e: Exception) {
            Log.e("MarketRepository", "Error fetching depth for $symbol: ${e.message}")
            // Fallback synthetic depth around current price
            val currentPrice = _pairsFlow.value.find { it.symbol == symbol }?.currentPrice ?: 65000.0
            generateSyntheticOrderBook(currentPrice)
        }
    }

    private fun parseBookSide(raw: List<List<String>>): List<OrderBookEntry> {
        var runningTotal = 0.0
        val entries = raw.mapNotNull { item ->
            val p = item.getOrNull(0)?.toDoubleOrNull() ?: return@mapNotNull null
            val a = item.getOrNull(1)?.toDoubleOrNull() ?: return@mapNotNull null
            runningTotal += a
            OrderBookEntry(price = p, amount = a, total = runningTotal)
        }
        val maxTotal = runningTotal.coerceAtLeast(0.0001)
        return entries.map { it.copy(relativeDepthPercent = (it.total / maxTotal).toFloat()) }
    }

    suspend fun fetchRecentTrades(symbol: String): List<MarketTrade> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecentTrades(symbol = symbol, limit = 20)
            response.map {
                MarketTrade(
                    id = it.id,
                    price = it.price.toDoubleOrNull() ?: 0.0,
                    amount = it.qty.toDoubleOrNull() ?: 0.0,
                    time = it.time,
                    isBuy = !it.isBuyerMaker
                )
            }
        } catch (e: Exception) {
            Log.e("MarketRepository", "Error fetching trades: ${e.message}")
            emptyList()
        }
    }

    suspend fun toggleFavorite(symbol: String) = withContext(Dispatchers.IO) {
        val dao = database.favoriteDao()
        if (dao.isFavorite(symbol)) {
            dao.removeFavorite(symbol)
        } else {
            dao.addFavorite(FavoriteEntity(symbol))
        }
        val favs = dao.getAllFavoriteSymbols().firstOrNull()?.toSet() ?: emptySet()
        _pairsFlow.value = _pairsFlow.value.map {
            it.copy(isFavorite = favs.contains(it.symbol))
        }
    }

    private fun generateSparkline(open: Double, low: Double, high: Double, close: Double): List<Float> {
        val count = 16
        val points = mutableListOf<Float>()
        points.add(open.toFloat())
        val range = (high - low).coerceAtLeast(0.1)
        for (i in 1 until count - 1) {
            val ratio = i.toDouble() / count
            val baseline = open + (close - open) * ratio
            val noise = (Math.sin(i * 1.3) * 0.4) * (range * 0.3)
            val v = (baseline + noise).coerceIn(low, high).toFloat()
            points.add(v)
        }
        points.add(close.toFloat())
        return points
    }

    private fun generateFallbackKlines(symbol: String): List<Candlestick> {
        val pair = _pairsFlow.value.find { it.symbol == symbol }
        val price = (pair?.currentPrice ?: 67000.0).toFloat()
        val now = System.currentTimeMillis()
        val list = mutableListOf<Candlestick>()
        var curr = price * 0.96f
        for (i in 60 downTo 0) {
            val t = now - (i * 15 * 60 * 1000L)
            val open = curr
            val delta = (Math.sin(i * 0.4) * 0.015f * curr).toFloat()
            val close = (open + delta).coerceAtLeast(1f)
            val high = maxOf(open, close) * 1.004f
            val low = minOf(open, close) * 0.996f
            val vol = (100f + (Math.cos(i.toDouble()) * 40f).toFloat()).coerceAtLeast(10f)
            curr = close
            list.add(Candlestick(t, open, high, low, close, vol, t + 15 * 60 * 1000L))
        }
        return list
    }

    private fun generateSyntheticOrderBook(price: Double): Pair<List<OrderBookEntry>, List<OrderBookEntry>> {
        val bids = mutableListOf<OrderBookEntry>()
        val asks = mutableListOf<OrderBookEntry>()
        var bidTotal = 0.0
        var askTotal = 0.0
        for (i in 1..10) {
            val bp = price * (1.0 - i * 0.0006)
            val ba = 0.15 * i + 0.1
            bidTotal += ba
            bids.add(OrderBookEntry(bp, ba, bidTotal, 0f))

            val ap = price * (1.0 + i * 0.0006)
            val aa = 0.18 * i + 0.08
            askTotal += aa
            asks.add(OrderBookEntry(ap, aa, askTotal, 0f))
        }
        val bMax = bidTotal.coerceAtLeast(0.001)
        val aMax = askTotal.coerceAtLeast(0.001)
        return Pair(
            bids.map { it.copy(relativeDepthPercent = (it.total / bMax).toFloat()) },
            asks.map { it.copy(relativeDepthPercent = (it.total / aMax).toFloat()) }
        )
    }

    private fun createInitialFallbackPairs(): List<CryptoPairInfo> {
        val initialData = listOf(
            Triple("BTCUSDT", "BTC", 67450.0),
            Triple("ETHUSDT", "ETH", 2620.0),
            Triple("BNBUSDT", "BNB", 590.0),
            Triple("SOLUSDT", "SOL", 175.5),
            Triple("XRPUSDT", "XRP", 0.535),
            Triple("DOGEUSDT", "DOGE", 0.142),
            Triple("ADAUSDT", "ADA", 0.355),
            Triple("TRXUSDT", "TRX", 0.165),
            Triple("AVAXUSDT", "AVAX", 28.4),
            Triple("LINKUSDT", "LINK", 11.8),
            Triple("LTCUSDT", "LTC", 72.5),
            Triple("DOTUSDT", "DOT", 4.35),
            Triple("BCHUSDT", "BCH", 365.0),
            Triple("SUIUSDT", "SUI", 2.15),
            Triple("TONUSDT", "TON", 5.25)
        )
        return initialData.map { (symbol, asset, price) ->
            val fullName = coinMetadata[symbol]?.second ?: asset
            val change = when (asset) {
                "BTC" -> 2.45
                "ETH" -> -0.85
                "SOL" -> 5.12
                "BNB" -> 1.15
                "SUI" -> 8.42
                else -> 0.65
            }
            CryptoPairInfo(
                symbol = symbol,
                baseAsset = asset,
                quoteAsset = "USDT",
                fullName = fullName,
                currentPrice = price,
                priceChangePercent = change,
                priceChangeAmount = price * (change / 100.0),
                high24h = price * 1.03,
                low24h = price * 0.97,
                volume24h = 15000.0,
                turnover24h = 15000.0 * price,
                sparklinePoints = listOf(
                    (price * 0.98).toFloat(),
                    (price * 0.99).toFloat(),
                    (price * 0.985).toFloat(),
                    (price * 1.01).toFloat(),
                    price.toFloat()
                )
            )
        }
    }
}
