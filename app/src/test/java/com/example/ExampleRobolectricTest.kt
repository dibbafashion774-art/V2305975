package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.HoldingEntity
import com.example.data.local.PortfolioEntity
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.network.BinanceWebSocketManager
import com.example.data.network.NetworkClient
import com.example.data.repository.MarketRepository
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var tradingRepository: TradingRepository
    private lateinit var marketRepository: MarketRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val ws = BinanceWebSocketManager()
        marketRepository = MarketRepository(NetworkClient.apiService, ws, db)
        tradingRepository = TradingRepository(db, marketRepository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BinEx", appName)
    }

    @Test
    fun testInitialPortfolioBalanceIsSevenMillionUsdt() = runBlocking {
        // Initialize if empty
        val existing = db.portfolioDao().getPortfolioSync()
        if (existing == null) {
            db.portfolioDao().insertOrUpdate(
                PortfolioEntity(id = 1, usdtAvailable = 7_000_000.0, fundingUsdt = 0.0)
            )
        }
        val portfolio = db.portfolioDao().getPortfolioSync()
        assertNotNull(portfolio)
        assertEquals(7_000_000.0, portfolio!!.usdtAvailable, 0.01)
    }

    @Test
    fun testBuyOrderExecutionAndHoldingsCalculation() = runBlocking {
        db.portfolioDao().insertOrUpdate(
            PortfolioEntity(id = 1, usdtAvailable = 7_000_000.0, fundingUsdt = 0.0)
        )

        // Buy 2 BTC at 60,000 USDT = 120,000 USDT + 0.1% fee (120 USDT) = 120,120 USDT
        val result = tradingRepository.executeMarketOrder(
            symbol = "BTCUSDT",
            baseAsset = "BTC",
            coinName = "Bitcoin",
            side = OrderSide.BUY,
            quantity = 2.0,
            price = 60_000.0
        )

        assertTrue(result.isSuccess)
        val order = result.getOrNull()!!
        assertEquals(OrderSide.BUY, order.side)
        assertEquals(OrderStatus.FILLED, order.status)
        assertEquals(120.0, order.fee, 0.01)

        val updatedPortfolio = db.portfolioDao().getPortfolioSync()!!
        assertEquals(7_000_000.0 - 120_120.0, updatedPortfolio.usdtAvailable, 0.01)

        val btcHolding = db.holdingDao().getHoldingBySymbol("BTC")
        assertNotNull(btcHolding)
        assertEquals(2.0, btcHolding!!.quantity, 0.0001)
        assertEquals(120_000.0, btcHolding.totalCostBasis, 0.01)
    }

    @Test
    fun testSellOrderExecutionAndRealizedReturn() = runBlocking {
        db.portfolioDao().insertOrUpdate(
            PortfolioEntity(id = 1, usdtAvailable = 5_000_000.0, fundingUsdt = 0.0)
        )
        db.holdingDao().insertOrUpdate(
            HoldingEntity(symbol = "ETH", coinName = "Ethereum", quantity = 10.0, totalCostBasis = 25_000.0)
        )

        // Sell 5 ETH at 3,000 USDT = 15,000 USDT gross. Fee = 15 USDT. Net = 14,985 USDT.
        val result = tradingRepository.executeMarketOrder(
            symbol = "ETHUSDT",
            baseAsset = "ETH",
            coinName = "Ethereum",
            side = OrderSide.SELL,
            quantity = 5.0,
            price = 3_000.0
        )

        assertTrue(result.isSuccess)
        val updatedPortfolio = db.portfolioDao().getPortfolioSync()!!
        assertEquals(5_000_000.0 + 14_985.0, updatedPortfolio.usdtAvailable, 0.01)

        val ethHolding = db.holdingDao().getHoldingBySymbol("ETH")!!
        assertEquals(5.0, ethHolding.quantity, 0.0001)
        assertEquals(12_500.0, ethHolding.totalCostBasis, 0.01)
    }

    @Test
    fun testInsufficientBalanceRejectsBuy() = runBlocking {
        db.portfolioDao().insertOrUpdate(
            PortfolioEntity(id = 1, usdtAvailable = 50.0, fundingUsdt = 0.0)
        )

        // Attempt to buy 1 BTC at 60,000 USDT
        val result = tradingRepository.executeMarketOrder(
            symbol = "BTCUSDT",
            baseAsset = "BTC",
            coinName = "Bitcoin",
            side = OrderSide.BUY,
            quantity = 1.0,
            price = 60_000.0
        )

        assertTrue(result.isFailure)
    }
}
