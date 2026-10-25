package com.example.ui.navigation

sealed class Screen {
    data object Home : Screen()
    data object Markets : Screen()
    data class Trade(val symbol: String = "BTCUSDT", val side: String = "BUY") : Screen()
    data object Assets : Screen()
    data object Profile : Screen()
    data class CoinDetail(val symbol: String) : Screen()
    data object TransactionHistory : Screen()
}

enum class BottomNavTab {
    HOME,
    MARKETS,
    TRADE,
    ASSETS,
    PROFILE
}
