package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.network.BinanceWebSocketManager
import com.example.data.network.NetworkClient
import com.example.data.repository.MarketRepository
import com.example.data.repository.TradingRepository

class AppViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val database = AppDatabase.getInstance(context)
    private val webSocketManager = BinanceWebSocketManager()
    val marketRepository = MarketRepository(NetworkClient.apiService, webSocketManager, database)
    val tradingRepository = TradingRepository(database, marketRepository)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MarketViewModel::class.java) -> {
                MarketViewModel(marketRepository) as T
            }
            modelClass.isAssignableFrom(TradingViewModel::class.java) -> {
                TradingViewModel(tradingRepository, marketRepository) as T
            }
            modelClass.isAssignableFrom(PortfolioViewModel::class.java) -> {
                PortfolioViewModel(tradingRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
