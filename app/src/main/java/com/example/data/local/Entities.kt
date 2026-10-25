package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio")
data class PortfolioEntity(
    @PrimaryKey val id: Int = 1,
    val usdtAvailable: Double = 7_000_000.00,
    val fundingUsdt: Double = 0.00,
    val isBalanceVisible: Boolean = true
)

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey val symbol: String, // e.g. "BTC"
    val coinName: String,
    val quantity: Double,
    val totalCostBasis: Double
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val side: String,
    val type: String,
    val price: Double,
    val quantity: Double,
    val totalValue: Double,
    val fee: Double,
    val status: String,
    val timestamp: Long
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val amount: Double,
    val asset: String,
    val fee: Double,
    val status: String,
    val timestamp: Long,
    val details: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val symbol: String
)
