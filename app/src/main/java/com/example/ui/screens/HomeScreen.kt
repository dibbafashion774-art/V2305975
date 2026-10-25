package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoPairInfo
import com.example.ui.components.BinExTopBar
import com.example.ui.components.CoinAvatarBadge
import com.example.ui.components.CryptoPriceRow
import com.example.ui.components.MiniSparkline
import com.example.ui.components.PortfolioBalanceCard
import com.example.ui.components.formatCryptoPrice
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.DarkBgPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.MarketViewModel
import com.example.viewmodel.PortfolioViewModel

@Composable
fun HomeScreen(
    marketViewModel: MarketViewModel,
    portfolioViewModel: PortfolioViewModel,
    onNavigateToPairDetail: (String) -> Unit,
    onNavigateToTrade: (String) -> Unit,
    onNavigateToMarkets: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectionState by marketViewModel.connectionState.collectAsState()
    val portfolioSummary by portfolioViewModel.portfolioSummary.collectAsState()
    val hotPairs by marketViewModel.hotPairs.collectAsState()
    val topGainers by marketViewModel.topGainers.collectAsState()
    val topLosers by marketViewModel.topLosers.collectAsState()
    val allPairs by marketViewModel.allPairs.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Hot", "Gainers", "Losers", "24h Volume")

    val displayedPairs = when (selectedTabIndex) {
        0 -> hotPairs
        1 -> topGainers
        2 -> topLosers
        3 -> allPairs.sortedByDescending { it.turnover24h }
        else -> hotPairs
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        // Top App Bar
        BinExTopBar(
            connectionState = connectionState,
            onSearchClick = onNavigateToMarkets,
            onNotificationClick = { /* Notifications sheet or toast */ },
            onProfileClick = onNavigateToProfile
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Portfolio Balance Card
            item {
                PortfolioBalanceCard(
                    summary = portfolioSummary,
                    onToggleVisibility = { portfolioViewModel.toggleBalanceVisibility() },
                    onQuickAction = { action ->
                        when (action) {
                            "BUY" -> onNavigateToTrade("BTCUSDT")
                            "SELL" -> onNavigateToTrade("BTCUSDT")
                            "MARKETS" -> onNavigateToMarkets()
                            "TRADE" -> onNavigateToTrade("BTCUSDT")
                            "ASSETS" -> onNavigateToAssets()
                            "HISTORY" -> onNavigateToHistory()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Quick Hot Coins Cards Row
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Featured Markets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(hotPairs) { pair ->
                        HotCoinCard(
                            pair = pair,
                            onClick = { onNavigateToPairDetail(pair.symbol) }
                        )
                    }
                }
            }

            // Market Overview Section with Tabs
            item {
                Spacer(modifier = Modifier.height(20.dp))
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = DarkBgPrimary,
                    contentColor = BinanceGold,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = BinanceGold,
                            height = 2.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) BinanceGold else TextSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Pairs List
            items(displayedPairs, key = { it.symbol }) { pair ->
                CryptoPriceRow(
                    pair = pair,
                    onClick = { onNavigateToPairDetail(pair.symbol) },
                    onToggleFavorite = { marketViewModel.toggleFavorite(pair.symbol) },
                    showSparkline = true
                )
            }
        }
    }
}

@Composable
fun HotCoinCard(
    pair: CryptoPairInfo,
    onClick: () -> Unit
) {
    val changeColor = if (pair.isPositive) CryptoGreen else CryptoRed

    Box(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("hot_card_${pair.symbol}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinAvatarBadge(asset = pair.baseAsset)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pair.baseAsset,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = pair.displayChange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = changeColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatCryptoPrice(pair.currentPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (pair.sparklinePoints.isNotEmpty()) {
                MiniSparkline(
                    points = pair.sparklinePoints,
                    lineColor = changeColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
