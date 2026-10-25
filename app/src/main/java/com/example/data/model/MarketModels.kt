package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Ticker24HrDto(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "priceChange") val priceChange: String? = "0.0",
    @Json(name = "priceChangePercent") val priceChangePercent: String? = "0.0",
    @Json(name = "lastPrice") val lastPrice: String = "0.0",
    @Json(name = "highPrice") val highPrice: String? = "0.0",
    @Json(name = "lowPrice") val lowPrice: String? = "0.0",
    @Json(name = "volume") val volume: String? = "0.0",
    @Json(name = "quoteVolume") val quoteVolume: String? = "0.0",
    @Json(name = "openPrice") val openPrice: String? = "0.0",
    @Json(name = "closeTime") val closeTime: Long? = 0L
)

@JsonClass(generateAdapter = true)
data class MiniTickerWsDto(
    @Json(name = "s") val symbol: String,
    @Json(name = "c") val closePrice: String,
    @Json(name = "o") val openPrice: String? = null,
    @Json(name = "h") val highPrice: String? = null,
    @Json(name = "l") val lowPrice: String? = null,
    @Json(name = "v") val volume: String? = null,
    @Json(name = "q") val quoteVolume: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderBookResponse(
    @Json(name = "lastUpdateId") val lastUpdateId: Long = 0L,
    @Json(name = "bids") val bids: List<List<String>> = emptyList(),
    @Json(name = "asks") val asks: List<List<String>> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TradeResponse(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "price") val price: String = "0.0",
    @Json(name = "qty") val qty: String = "0.0",
    @Json(name = "time") val time: Long = 0L,
    @Json(name = "isBuyerMaker") val isBuyerMaker: Boolean = false
)

data class OrderBookEntry(
    val price: Double,
    val amount: Double,
    val total: Double = 0.0,
    val relativeDepthPercent: Float = 0f
)

data class MarketTrade(
    val id: Long,
    val price: Double,
    val amount: Double,
    val time: Long,
    val isBuy: Boolean
)

data class Candlestick(
    val openTime: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float,
    val closeTime: Long
)

data class CryptoPairInfo(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String = "USDT",
    val fullName: String,
    val currentPrice: Double,
    val priceChangePercent: Double,
    val priceChangeAmount: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val turnover24h: Double,
    val sparklinePoints: List<Float> = emptyList(),
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isPositive: Boolean get() = priceChangePercent >= 0
    val displayChange: String get() = String.format("%s%.2f%%", if (isPositive) "+" else "", priceChangePercent)
}
