package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.OrderSide
import com.example.ui.navigation.BottomNavTab
import com.example.ui.navigation.Screen
import com.example.ui.screens.AssetsScreen
import com.example.ui.screens.CoinDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TradeScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.DarkBgPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppViewModelFactory
import com.example.viewmodel.MarketViewModel
import com.example.viewmodel.PortfolioViewModel
import com.example.viewmodel.TradingViewModel

@Composable
fun MainApp(
    viewModelFactory: AppViewModelFactory = AppViewModelFactory(LocalContext.current)
) {
    val marketViewModel: MarketViewModel = viewModel(factory = viewModelFactory)
    val tradingViewModel: TradingViewModel = viewModel(factory = viewModelFactory)
    val portfolioViewModel: PortfolioViewModel = viewModel(factory = viewModelFactory)

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }
    val allPairs by marketViewModel.allPairs.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentScreen !is Screen.Home) {
        when (currentScreen) {
            is Screen.CoinDetail -> currentScreen = Screen.Markets
            is Screen.TransactionHistory -> currentScreen = Screen.Assets
            else -> {
                currentScreen = Screen.Home
                selectedTab = BottomNavTab.HOME
            }
        }
    }

    val isSubScreen = currentScreen is Screen.CoinDetail || currentScreen is Screen.TransactionHistory

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBgPrimary,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (!isSubScreen) {
                NavigationBar(
                    containerColor = DarkCardBg,
                    contentColor = BinanceGold,
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 8.dp
                ) {
                    // 1. Home
                    NavigationBarItem(
                        selected = selectedTab == BottomNavTab.HOME && currentScreen is Screen.Home,
                        onClick = {
                            selectedTab = BottomNavTab.HOME
                            currentScreen = Screen.Home
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == BottomNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedTab == BottomNavTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBgPrimary,
                            selectedTextColor = BinanceGold,
                            indicatorColor = BinanceGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    // 2. Markets
                    NavigationBarItem(
                        selected = selectedTab == BottomNavTab.MARKETS && currentScreen is Screen.Markets,
                        onClick = {
                            selectedTab = BottomNavTab.MARKETS
                            currentScreen = Screen.Markets
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == BottomNavTab.MARKETS) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Outlined.TrendingUp,
                                contentDescription = "Markets"
                            )
                        },
                        label = { Text("Markets", fontSize = 11.sp, fontWeight = if (selectedTab == BottomNavTab.MARKETS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBgPrimary,
                            selectedTextColor = BinanceGold,
                            indicatorColor = BinanceGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_markets")
                    )

                    // 3. Trade
                    NavigationBarItem(
                        selected = selectedTab == BottomNavTab.TRADE && currentScreen is Screen.Trade,
                        onClick = {
                            selectedTab = BottomNavTab.TRADE
                            currentScreen = Screen.Trade()
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == BottomNavTab.TRADE) Icons.Filled.CandlestickChart else Icons.Outlined.CandlestickChart,
                                contentDescription = "Trade"
                            )
                        },
                        label = { Text("Trade", fontSize = 11.sp, fontWeight = if (selectedTab == BottomNavTab.TRADE) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBgPrimary,
                            selectedTextColor = BinanceGold,
                            indicatorColor = BinanceGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_trade")
                    )

                    // 4. Assets
                    NavigationBarItem(
                        selected = selectedTab == BottomNavTab.ASSETS && currentScreen is Screen.Assets,
                        onClick = {
                            selectedTab = BottomNavTab.ASSETS
                            currentScreen = Screen.Assets
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == BottomNavTab.ASSETS) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                contentDescription = "Assets"
                            )
                        },
                        label = { Text("Assets", fontSize = 11.sp, fontWeight = if (selectedTab == BottomNavTab.ASSETS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBgPrimary,
                            selectedTextColor = BinanceGold,
                            indicatorColor = BinanceGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_assets")
                    )

                    // 5. Profile
                    NavigationBarItem(
                        selected = selectedTab == BottomNavTab.PROFILE && currentScreen is Screen.Profile,
                        onClick = {
                            selectedTab = BottomNavTab.PROFILE
                            currentScreen = Screen.Profile
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == BottomNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = if (selectedTab == BottomNavTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBgPrimary,
                            selectedTextColor = BinanceGold,
                            indicatorColor = BinanceGold,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        marketViewModel = marketViewModel,
                        portfolioViewModel = portfolioViewModel,
                        onNavigateToPairDetail = { sym ->
                            currentScreen = Screen.CoinDetail(sym)
                        },
                        onNavigateToTrade = { sym ->
                            tradingViewModel.setPair(sym)
                            selectedTab = BottomNavTab.TRADE
                            currentScreen = Screen.Trade(symbol = sym)
                        },
                        onNavigateToMarkets = {
                            selectedTab = BottomNavTab.MARKETS
                            currentScreen = Screen.Markets
                        },
                        onNavigateToAssets = {
                            selectedTab = BottomNavTab.ASSETS
                            currentScreen = Screen.Assets
                        },
                        onNavigateToHistory = {
                            currentScreen = Screen.TransactionHistory
                        },
                        onNavigateToProfile = {
                            selectedTab = BottomNavTab.PROFILE
                            currentScreen = Screen.Profile
                        }
                    )
                }

                is Screen.Markets -> {
                    MarketsScreen(
                        marketViewModel = marketViewModel,
                        onNavigateToPairDetail = { sym ->
                            currentScreen = Screen.CoinDetail(sym)
                        }
                    )
                }

                is Screen.Trade -> {
                    TradeScreen(
                        tradingViewModel = tradingViewModel,
                        availablePairs = allPairs,
                        onOpenChart = { sym ->
                            currentScreen = Screen.CoinDetail(sym)
                        }
                    )
                }

                is Screen.Assets -> {
                    AssetsScreen(
                        portfolioViewModel = portfolioViewModel,
                        onNavigateToTrade = { sym ->
                            tradingViewModel.setPair(sym)
                            selectedTab = BottomNavTab.TRADE
                            currentScreen = Screen.Trade(symbol = sym)
                        },
                        onNavigateToHistory = {
                            currentScreen = Screen.TransactionHistory
                        }
                    )
                }

                is Screen.Profile -> {
                    ProfileScreen(
                        portfolioViewModel = portfolioViewModel
                    )
                }

                is Screen.CoinDetail -> {
                    CoinDetailScreen(
                        symbol = screen.symbol,
                        marketViewModel = marketViewModel,
                        onBack = {
                            currentScreen = Screen.Markets
                        },
                        onNavigateToTrade = { sym, side ->
                            tradingViewModel.setPair(sym)
                            tradingViewModel.setSide(if (side == "BUY") OrderSide.BUY else OrderSide.SELL)
                            selectedTab = BottomNavTab.TRADE
                            currentScreen = Screen.Trade(symbol = sym, side = side)
                        }
                    )
                }

                is Screen.TransactionHistory -> {
                    TransactionHistoryScreen(
                        portfolioViewModel = portfolioViewModel,
                        onBack = {
                            currentScreen = Screen.Assets
                        }
                    )
                }
            }
        }
    }
}
