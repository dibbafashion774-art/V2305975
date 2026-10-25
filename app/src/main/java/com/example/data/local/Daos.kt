package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio WHERE id = 1 LIMIT 1")
    fun getPortfolio(): Flow<PortfolioEntity?>

    @Query("SELECT * FROM portfolio WHERE id = 1 LIMIT 1")
    suspend fun getPortfolioSync(): PortfolioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(portfolio: PortfolioEntity)
}

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings WHERE quantity > 0 ORDER BY (quantity * totalCostBasis) DESC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE symbol = :symbol LIMIT 1")
    suspend fun getHoldingBySymbol(symbol: String): HoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE symbol = :symbol")
    suspend fun deleteHolding(symbol: String)

    @Query("DELETE FROM holdings")
    suspend fun clearHoldings()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'OPEN' ORDER BY timestamp DESC")
    fun getOpenOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getOrdersForSymbol(symbol: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'OPEN'")
    suspend fun getOpenOrdersSync(): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("DELETE FROM orders")
    suspend fun clearOrders()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}

@Dao
interface FavoriteDao {
    @Query("SELECT symbol FROM favorites")
    fun getAllFavoriteSymbols(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE symbol = :symbol)")
    suspend fun isFavorite(symbol: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE symbol = :symbol")
    suspend fun removeFavorite(symbol: String)
}
