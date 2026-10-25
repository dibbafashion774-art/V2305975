package com.example.data.network

import com.example.data.model.OrderBookResponse
import com.example.data.model.Ticker24HrDto
import com.example.data.model.TradeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApiService {

    @GET("api/v3/ticker/24hr")
    suspend fun get24HrTickers(): List<Ticker24HrDto>

    @GET("api/v3/depth")
    suspend fun getDepth(
        @Query("symbol") symbol: String,
        @Query("limit") limit: Int = 20
    ): OrderBookResponse

    @GET("api/v3/trades")
    suspend fun getRecentTrades(
        @Query("symbol") symbol: String,
        @Query("limit") limit: Int = 20
    ): List<TradeResponse>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<List<Any>>
}
