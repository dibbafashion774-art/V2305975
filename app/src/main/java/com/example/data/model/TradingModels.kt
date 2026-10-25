package com.example.data.model

enum class OrderSide {
    BUY,
    SELL
}

enum class OrderType {
    LIMIT,
    MARKET
}

enum class OrderStatus {
    OPEN,
    FILLED,
    CANCELLED
}

enum class TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    BUY_TRADE,
    SELL_TRADE
}

enum class WalletType {
    SPOT,
    FUNDING
}

data class OrderItem(
    val id: Long,
    val symbol: String,
    val side: OrderSide,
    val type: OrderType,
    val price: Double,
    val quantity: Double,
    val totalValue: Double,
    val fee: Double,
    val status: OrderStatus,
    val timestamp: Long
)

data class TransactionRecord(
    val id: String,
    val type: TransactionType,
    val title: String,
    val amount: Double,
    val asset: String,
    val fee: Double,
    val status: String = "Completed",
    val timestamp: Long,
    val details: String = ""
)

data class HoldingItem(
    val symbol: String,
    val baseAsset: String,
    val coinName: String,
    val quantity: Double,
    val totalCostBasis: Double,
    val currentPrice: Double = 0.0,
    val currentMarketValue: Double = 0.0,
    val averageBuyPrice: Double = if (quantity > 0) totalCostBasis / quantity else 0.0,
    val unrealizedPnl: Double = currentMarketValue - totalCostBasis,
    val unrealizedPnlPercent: Double = if (totalCostBasis > 0) ((currentMarketValue - totalCostBasis) / totalCostBasis) * 100.0 else 0.0
)

data class PortfolioSummary(
    val totalPortfolioValueUsdt: Double = 7_000_000.00,
    val availableBalanceUsdt: Double = 7_000_000.00,
    val assetsInPositionsUsdt: Double = 0.00,
    val todayPnlUsdt: Double = 0.00,
    val todayPnlPercent: Double = 0.00,
    val fundingBalanceUsdt: Double = 0.00,
    val holdings: List<HoldingItem> = emptyList(),
    val isBalanceVisible: Boolean = true
)
