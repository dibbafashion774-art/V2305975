package com.example

import com.example.data.model.HoldingItem
import com.example.data.model.MiniTickerWsDto
import com.example.data.model.PortfolioSummary
import com.example.data.model.Ticker24HrDto
import com.example.data.network.NetworkClient
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testTicker24HrDtoJsonParsing() {
        val json = """
            {
                "symbol": "BTCUSDT",
                "priceChange": "1500.50",
                "priceChangePercent": "2.35",
                "lastPrice": "67500.00",
                "highPrice": "68200.00",
                "lowPrice": "65800.00",
                "volume": "25000.123",
                "quoteVolume": "1687500000.00"
            }
        """.trimIndent()

        val adapter = NetworkClient.moshi.adapter(Ticker24HrDto::class.java)
        val dto = adapter.fromJson(json)

        assertNotNull(dto)
        assertEquals("BTCUSDT", dto!!.symbol)
        assertEquals("67500.00", dto.lastPrice)
        assertEquals("2.35", dto.priceChangePercent)
        assertEquals("1687500000.00", dto.quoteVolume)
    }

    @Test
    fun testMiniTickerWebSocketArrayParsing() {
        val jsonArray = """
            [
                {
                    "e": "24hrMiniTicker",
                    "E": 1672531199000,
                    "s": "ETHUSDT",
                    "c": "2650.50",
                    "o": "2600.00",
                    "h": "2700.00",
                    "l": "2580.00",
                    "v": "50000.0",
                    "q": "132500000.0"
                }
            ]
        """.trimIndent()

        val listType = Types.newParameterizedType(List::class.java, MiniTickerWsDto::class.java)
        val adapter = NetworkClient.moshi.adapter<List<MiniTickerWsDto>>(listType)
        val list = adapter.fromJson(jsonArray)

        assertNotNull(list)
        assertEquals(1, list!!.size)
        assertEquals("ETHUSDT", list[0].symbol)
        assertEquals("2650.50", list[0].closePrice)
    }

    @Test
    fun testPortfolioValuationFormulas() {
        // Holding calculation: Asset Market Value = Asset Quantity × Current Market Price
        val holding = HoldingItem(
            symbol = "SOL",
            baseAsset = "SOL",
            coinName = "Solana",
            quantity = 100.0,
            totalCostBasis = 15_000.0,
            currentPrice = 180.0,
            currentMarketValue = 100.0 * 180.0 // 18,000.0
        )

        assertEquals(18_000.0, holding.currentMarketValue, 0.001)
        assertEquals(3_000.0, holding.unrealizedPnl, 0.001)
        assertEquals(20.0, holding.unrealizedPnlPercent, 0.001) // 3000 / 15000 = 20%

        // Total Portfolio Value = Available USDT + Total Market Value of Cryptocurrency Holdings
        val availableUsdt = 7_000_000.0
        val totalValue = availableUsdt + holding.currentMarketValue
        assertEquals(7_018_000.0, totalValue, 0.001)
    }

    @Test
    fun testFeeCalculations() {
        val tradeValue = 50_000.0
        val feeRate = 0.001 // 0.1% spot fee
        val fee = tradeValue * feeRate
        assertEquals(50.0, fee, 0.001)

        val totalCostWithFee = tradeValue + fee
        assertEquals(50_050.0, totalCostWithFee, 0.001)
    }
}
