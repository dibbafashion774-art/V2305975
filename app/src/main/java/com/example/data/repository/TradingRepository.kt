package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.HoldingEntity
import com.example.data.local.OrderEntity
import com.example.data.local.PortfolioEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.CryptoPairInfo
import com.example.data.model.HoldingItem
import com.example.data.model.OrderItem
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.PortfolioSummary
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.WalletType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TradingRepository(
    private val database: AppDatabase,
    private val marketRepository: MarketRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val tradingFeeRate = 0.001 // 0.1% Binance spot fee

    init {
        scope.launch {
            initPortfolioIfEmpty()
        }
    }

    private suspend fun initPortfolioIfEmpty() {
        val existing = database.portfolioDao().getPortfolioSync()
        if (existing == null) {
            database.portfolioDao().insertOrUpdate(
                PortfolioEntity(
                    id = 1,
                    usdtAvailable = 7_000_000.00,
                    fundingUsdt = 0.00,
                    isBalanceVisible = true
                )
            )
        }
    }

    val portfolioSummary: Flow<PortfolioSummary> = combine(
        database.portfolioDao().getPortfolio(),
        database.holdingDao().getAllHoldings(),
        marketRepository.pairsFlow
    ) { portfolioEntity, holdingsEntities, marketPairs ->
        val entity = portfolioEntity ?: PortfolioEntity()
        val pairMap = marketPairs.associateBy { it.baseAsset }

        var totalAssetsInPositions = 0.0
        var totalCostBasis = 0.0

        val holdingItems = holdingsEntities.map { holding ->
            val pair = pairMap[holding.symbol]
            val currentPrice = pair?.currentPrice ?: 0.0
            val marketValue = holding.quantity * currentPrice
            totalAssetsInPositions += marketValue
            totalCostBasis += holding.totalCostBasis

            HoldingItem(
                symbol = holding.symbol,
                baseAsset = holding.symbol,
                coinName = holding.coinName,
                quantity = holding.quantity,
                totalCostBasis = holding.totalCostBasis,
                currentPrice = currentPrice,
                currentMarketValue = marketValue
            )
        }

        val totalPortfolioValue = entity.usdtAvailable + totalAssetsInPositions
        val unrealizedPnl = totalAssetsInPositions - totalCostBasis
        val unrealizedPnlPercent = if (totalCostBasis > 0) (unrealizedPnl / totalCostBasis) * 100.0 else 0.0

        PortfolioSummary(
            totalPortfolioValueUsdt = totalPortfolioValue,
            availableBalanceUsdt = entity.usdtAvailable,
            assetsInPositionsUsdt = totalAssetsInPositions,
            todayPnlUsdt = unrealizedPnl,
            todayPnlPercent = unrealizedPnlPercent,
            fundingBalanceUsdt = entity.fundingUsdt,
            holdings = holdingItems,
            isBalanceVisible = entity.isBalanceVisible
        )
    }

    val orders: Flow<List<OrderItem>> = database.orderDao().getAllOrders().map { entities ->
        entities.map { mapOrderEntityToItem(it) }
    }

    val openOrders: Flow<List<OrderItem>> = database.orderDao().getOpenOrders().map { entities ->
        entities.map { mapOrderEntityToItem(it) }
    }

    val transactions: Flow<List<TransactionRecord>> = database.transactionDao().getAllTransactions().map { entities ->
        entities.map {
            TransactionRecord(
                id = it.id,
                type = runCatching { TransactionType.valueOf(it.type) }.getOrDefault(TransactionType.BUY_TRADE),
                title = it.title,
                amount = it.amount,
                asset = it.asset,
                fee = it.fee,
                status = it.status,
                timestamp = it.timestamp,
                details = it.details
            )
        }
    }

    suspend fun executeMarketOrder(
        symbol: String,
        baseAsset: String,
        coinName: String,
        side: OrderSide,
        quantity: Double,
        price: Double
    ): Result<OrderItem> = withContext(Dispatchers.IO) {
        if (quantity <= 0.0 || price <= 0.0) {
            return@withContext Result.failure(IllegalArgumentException("Quantity and price must be greater than zero."))
        }

        val portfolio = database.portfolioDao().getPortfolioSync() ?: PortfolioEntity()
        val orderTotal = quantity * price
        val fee = orderTotal * tradingFeeRate

        when (side) {
            OrderSide.BUY -> {
                val totalRequired = orderTotal + fee
                if (portfolio.usdtAvailable < totalRequired) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient USDT balance. Required: ${String.format("%.2f", totalRequired)} USDT, Available: ${String.format("%.2f", portfolio.usdtAvailable)} USDT")
                    )
                }

                // Deduct USDT from available
                val updatedPortfolio = portfolio.copy(
                    usdtAvailable = portfolio.usdtAvailable - totalRequired
                )
                database.portfolioDao().insertOrUpdate(updatedPortfolio)

                // Update holdings
                val existingHolding = database.holdingDao().getHoldingBySymbol(baseAsset)
                val newQty = (existingHolding?.quantity ?: 0.0) + quantity
                val newCostBasis = (existingHolding?.totalCostBasis ?: 0.0) + orderTotal
                database.holdingDao().insertOrUpdate(
                    HoldingEntity(
                        symbol = baseAsset,
                        coinName = coinName,
                        quantity = newQty,
                        totalCostBasis = newCostBasis
                    )
                )

                // Record order
                val orderEntity = OrderEntity(
                    symbol = symbol,
                    side = OrderSide.BUY.name,
                    type = OrderType.MARKET.name,
                    price = price,
                    quantity = quantity,
                    totalValue = orderTotal,
                    fee = fee,
                    status = OrderStatus.FILLED.name,
                    timestamp = System.currentTimeMillis()
                )
                val orderId = database.orderDao().insertOrder(orderEntity)

                // Record transaction
                val tx = TransactionEntity(
                    id = "TX-${UUID.randomUUID().toString().take(8).uppercase()}",
                    type = TransactionType.BUY_TRADE.name,
                    title = "Spot Buy $baseAsset",
                    amount = quantity,
                    asset = baseAsset,
                    fee = fee,
                    status = "Completed",
                    timestamp = System.currentTimeMillis(),
                    details = "Filled at ${String.format("%.4f", price)} USDT/coin"
                )
                database.transactionDao().insertTransaction(tx)

                Result.success(mapOrderEntityToItem(orderEntity.copy(id = orderId)))
            }

            OrderSide.SELL -> {
                val existingHolding = database.holdingDao().getHoldingBySymbol(baseAsset)
                val availableCoinQty = existingHolding?.quantity ?: 0.0

                if (availableCoinQty < quantity) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient $baseAsset balance. Required: $quantity, Available: $availableCoinQty")
                    )
                }

                val grossProceeds = orderTotal
                val netProceeds = grossProceeds - fee

                // Credit USDT to available
                val updatedPortfolio = portfolio.copy(
                    usdtAvailable = portfolio.usdtAvailable + netProceeds
                )
                database.portfolioDao().insertOrUpdate(updatedPortfolio)

                // Update holdings
                val remainingQty = availableCoinQty - quantity
                if (remainingQty <= 0.00000001) {
                    database.holdingDao().deleteHolding(baseAsset)
                } else {
                    val proportionalCostBasis = existingHolding!!.totalCostBasis * (remainingQty / availableCoinQty)
                    database.holdingDao().insertOrUpdate(
                        existingHolding.copy(
                            quantity = remainingQty,
                            totalCostBasis = proportionalCostBasis
                        )
                    )
                }

                // Record order
                val orderEntity = OrderEntity(
                    symbol = symbol,
                    side = OrderSide.SELL.name,
                    type = OrderType.MARKET.name,
                    price = price,
                    quantity = quantity,
                    totalValue = orderTotal,
                    fee = fee,
                    status = OrderStatus.FILLED.name,
                    timestamp = System.currentTimeMillis()
                )
                val orderId = database.orderDao().insertOrder(orderEntity)

                // Record transaction
                val tx = TransactionEntity(
                    id = "TX-${UUID.randomUUID().toString().take(8).uppercase()}",
                    type = TransactionType.SELL_TRADE.name,
                    title = "Spot Sell $baseAsset",
                    amount = quantity,
                    asset = baseAsset,
                    fee = fee,
                    status = "Completed",
                    timestamp = System.currentTimeMillis(),
                    details = "Filled at ${String.format("%.4f", price)} USDT/coin"
                )
                database.transactionDao().insertTransaction(tx)

                Result.success(mapOrderEntityToItem(orderEntity.copy(id = orderId)))
            }
        }
    }

    suspend fun placeLimitOrder(
        symbol: String,
        baseAsset: String,
        coinName: String,
        side: OrderSide,
        quantity: Double,
        limitPrice: Double
    ): Result<OrderItem> = withContext(Dispatchers.IO) {
        if (quantity <= 0.0 || limitPrice <= 0.0) {
            return@withContext Result.failure(IllegalArgumentException("Quantity and price must be greater than zero."))
        }

        val portfolio = database.portfolioDao().getPortfolioSync() ?: PortfolioEntity()
        val orderTotal = quantity * limitPrice
        val fee = orderTotal * tradingFeeRate

        when (side) {
            OrderSide.BUY -> {
                val totalRequired = orderTotal + fee
                if (portfolio.usdtAvailable < totalRequired) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient USDT to place limit order. Available: ${String.format("%.2f", portfolio.usdtAvailable)} USDT")
                    )
                }
                // Lock the USDT
                database.portfolioDao().insertOrUpdate(
                    portfolio.copy(usdtAvailable = portfolio.usdtAvailable - totalRequired)
                )
            }
            OrderSide.SELL -> {
                val holding = database.holdingDao().getHoldingBySymbol(baseAsset)
                val availableCoin = holding?.quantity ?: 0.0
                if (availableCoin < quantity) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient $baseAsset to place limit order. Available: $availableCoin")
                    )
                }
                // Lock the coin quantity
                val remaining = availableCoin - quantity
                if (remaining <= 0.00000001) {
                    database.holdingDao().deleteHolding(baseAsset)
                } else {
                    val proportionalCost = holding!!.totalCostBasis * (remaining / availableCoin)
                    database.holdingDao().insertOrUpdate(
                        holding.copy(quantity = remaining, totalCostBasis = proportionalCost)
                    )
                }
            }
        }

        val orderEntity = OrderEntity(
            symbol = symbol,
            side = side.name,
            type = OrderType.LIMIT.name,
            price = limitPrice,
            quantity = quantity,
            totalValue = orderTotal,
            fee = fee,
            status = OrderStatus.OPEN.name,
            timestamp = System.currentTimeMillis()
        )
        val id = database.orderDao().insertOrder(orderEntity)
        Result.success(mapOrderEntityToItem(orderEntity.copy(id = id)))
    }

    suspend fun cancelOrder(orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val openOrders = database.orderDao().getOpenOrdersSync()
        val target = openOrders.find { it.id == orderId }
            ?: return@withContext Result.failure(IllegalArgumentException("Open order not found."))

        val portfolio = database.portfolioDao().getPortfolioSync() ?: PortfolioEntity()

        if (target.side == OrderSide.BUY.name) {
            // Unlock USDT
            val refund = target.totalValue + target.fee
            database.portfolioDao().insertOrUpdate(portfolio.copy(usdtAvailable = portfolio.usdtAvailable + refund))
        } else {
            // Unlock Coin
            val baseAsset = target.symbol.replace("USDT", "")
            val existing = database.holdingDao().getHoldingBySymbol(baseAsset)
            val newQty = (existing?.quantity ?: 0.0) + target.quantity
            val restoredCost = (existing?.totalCostBasis ?: 0.0) + target.totalValue
            database.holdingDao().insertOrUpdate(
                HoldingEntity(
                    symbol = baseAsset,
                    coinName = baseAsset,
                    quantity = newQty,
                    totalCostBasis = restoredCost
                )
            )
        }

        database.orderDao().updateOrder(target.copy(status = OrderStatus.CANCELLED.name))
        Result.success(Unit)
    }

    suspend fun depositFunds(amount: Double, asset: String, network: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("Deposit amount must be positive"))
        val portfolio = database.portfolioDao().getPortfolioSync() ?: PortfolioEntity()
        database.portfolioDao().insertOrUpdate(portfolio.copy(usdtAvailable = portfolio.usdtAvailable + amount))

        val tx = TransactionEntity(
            id = "DEP-${UUID.randomUUID().toString().take(8).uppercase()}",
            type = TransactionType.DEPOSIT.name,
            title = "Deposit $asset",
            amount = amount,
            asset = asset,
            fee = 0.0,
            status = "Completed",
            timestamp = System.currentTimeMillis(),
            details = "Network: $network • Credited to Spot Wallet"
        )
        database.transactionDao().insertTransaction(tx)
        Result.success(Unit)
    }

    suspend fun withdrawFunds(amount: Double, asset: String, address: String, network: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("Withdrawal amount must be positive"))
        val portfolio = database.portfolioDao().getPortfolioSync() ?: PortfolioEntity()
        val fee = 1.0 // 1 USDT network fee
        val total = amount + fee

        if (portfolio.usdtAvailable < total) {
            return@withContext Result.failure(IllegalStateException("Insufficient USDT balance for withdrawal + network fee ($fee USDT)."))
        }

        database.portfolioDao().insertOrUpdate(portfolio.copy(usdtAvailable = portfolio.usdtAvailable - total))

        val tx = TransactionEntity(
            id = "WTH-${UUID.randomUUID().toString().take(8).uppercase()}",
            type = TransactionType.WITHDRAW.name,
            title = "Withdraw $asset",
            amount = amount,
            asset = asset,
            fee = fee,
            status = "Completed",
            timestamp = System.currentTimeMillis(),
            details = "To: ${address.take(6)}...${address.takeLast(4)} • Network: $network"
        )
        database.transactionDao().insertTransaction(tx)
        Result.success(Unit)
    }

    suspend fun transferFunds(amount: Double, from: WalletType, to: WalletType): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("Transfer amount must be positive"))
        val portfolio = database.portfolioDao().getPortfolioSync() ?: PortfolioEntity()

        if (from == WalletType.SPOT && to == WalletType.FUNDING) {
            if (portfolio.usdtAvailable < amount) {
                return@withContext Result.failure(IllegalStateException("Insufficient Spot balance."))
            }
            database.portfolioDao().insertOrUpdate(
                portfolio.copy(
                    usdtAvailable = portfolio.usdtAvailable - amount,
                    fundingUsdt = portfolio.fundingUsdt + amount
                )
            )
        } else if (from == WalletType.FUNDING && to == WalletType.SPOT) {
            if (portfolio.fundingUsdt < amount) {
                return@withContext Result.failure(IllegalStateException("Insufficient Funding balance."))
            }
            database.portfolioDao().insertOrUpdate(
                portfolio.copy(
                    usdtAvailable = portfolio.usdtAvailable + amount,
                    fundingUsdt = portfolio.fundingUsdt - amount
                )
            )
        }

        val tx = TransactionEntity(
            id = "TRF-${UUID.randomUUID().toString().take(8).uppercase()}",
            type = TransactionType.TRANSFER.name,
            title = "Internal Transfer USDT",
            amount = amount,
            asset = "USDT",
            fee = 0.0,
            status = "Completed",
            timestamp = System.currentTimeMillis(),
            details = "From ${from.name} to ${to.name}"
        )
        database.transactionDao().insertTransaction(tx)
        Result.success(Unit)
    }

    suspend fun toggleBalanceVisibility() = withContext(Dispatchers.IO) {
        val current = database.portfolioDao().getPortfolioSync() ?: PortfolioEntity()
        database.portfolioDao().insertOrUpdate(current.copy(isBalanceVisible = !current.isBalanceVisible))
    }

    suspend fun resetSimulation() = withContext(Dispatchers.IO) {
        database.portfolioDao().insertOrUpdate(
            PortfolioEntity(
                id = 1,
                usdtAvailable = 7_000_000.00,
                fundingUsdt = 0.00,
                isBalanceVisible = true
            )
        )
        database.holdingDao().clearHoldings()
        database.orderDao().clearOrders()
        database.transactionDao().clearTransactions()
    }

    private fun mapOrderEntityToItem(e: OrderEntity): OrderItem {
        return OrderItem(
            id = e.id,
            symbol = e.symbol,
            side = runCatching { OrderSide.valueOf(e.side) }.getOrDefault(OrderSide.BUY),
            type = runCatching { OrderType.valueOf(e.type) }.getOrDefault(OrderType.MARKET),
            price = e.price,
            quantity = e.quantity,
            totalValue = e.totalValue,
            fee = e.fee,
            status = runCatching { OrderStatus.valueOf(e.status) }.getOrDefault(OrderStatus.FILLED),
            timestamp = e.timestamp
        )
    }
}
