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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.components.CoinAvatarBadge
import com.example.ui.components.InteractiveCryptoChart
import com.example.ui.components.OrderBookView
import com.example.ui.components.RecentTradesView
import com.example.ui.components.formatCryptoPrice
import com.example.ui.components.formatVolume
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

@Composable
fun CoinDetailScreen(
    symbol: String,
    marketViewModel: MarketViewModel,
    onBack: () -> Unit,
    onNavigateToTrade: (String, String) -> Unit, // symbol, side
    modifier: Modifier = Modifier
) {
    LaunchedEffect(symbol) {
        marketViewModel.selectPair(symbol)
    }

    val pair by marketViewModel.selectedPair.collectAsState()
    val klines by marketViewModel.klines.collectAsState()
    val selectedInterval by marketViewModel.selectedInterval.collectAsState()
    val isCandleMode by marketViewModel.isCandleMode.collectAsState()
    val (bids, asks) = marketViewModel.orderBook.collectAsState().value
    val recentTrades by marketViewModel.recentTrades.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Order Book, 1: Recent Trades

    val intervals = listOf("1m", "5m", "15m", "1h", "4h", "1d", "1w")

    val changeColor = if ((pair?.priceChangePercent ?: 0.0) >= 0) CryptoGreen else CryptoRed

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                CoinAvatarBadge(asset = pair?.baseAsset ?: "BTC")
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${pair?.baseAsset ?: "BTC"}/USDT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = pair?.fullName ?: "Cryptocurrency",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            IconButton(
                onClick = { pair?.let { marketViewModel.toggleFavorite(it.symbol) } }
            ) {
                Icon(
                    imageVector = if (pair?.isFavorite == true) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Favorite",
                    tint = if (pair?.isFavorite == true) BinanceGold else TextSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Price & 24h Stats Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Large Current Price + %
                    Column {
                        Text(
                            text = formatCryptoPrice(pair?.currentPrice ?: 0.0),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = changeColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${pair?.displayChange ?: "+0.00%"} (${String.format("%s%.2f", if ((pair?.priceChangeAmount ?: 0.0) >= 0) "+" else "", pair?.priceChangeAmount ?: 0.0)} USDT)",
                            style = MaterialTheme.typography.bodySmall,
                            color = changeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Right: 24h High, Low, Volume
                    Column(horizontalAlignment = Alignment.End) {
                        Row {
                            Text("24h High: ", fontSize = 11.sp, color = TextTertiary)
                            Text(formatCryptoPrice(pair?.high24h ?: 0.0), fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row {
                            Text("24h Low: ", fontSize = 11.sp, color = TextTertiary)
                            Text(formatCryptoPrice(pair?.low24h ?: 0.0), fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row {
                            Text("24h Turnover: ", fontSize = 11.sp, color = TextTertiary)
                            Text(formatVolume(pair?.turnover24h ?: 0.0), fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Interval selector & Chart mode toggle
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(intervals) { interval ->
                            val isSel = selectedInterval == interval
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) DarkCardElevated else Color.Transparent)
                                    .border(1.dp, if (isSel) BinanceGold else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { marketViewModel.selectInterval(interval) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = interval,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) BinanceGold else TextSecondary
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { marketViewModel.toggleChartMode() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isCandleMode) Icons.Default.ShowChart else Icons.Default.CandlestickChart,
                            contentDescription = "Toggle Chart Mode",
                            tint = BinanceGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Interactive Chart
            item {
                Spacer(modifier = Modifier.height(8.dp))
                InteractiveCryptoChart(
                    candles = klines,
                    isCandleMode = isCandleMode,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Order Book & Recent Trades Tabs
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = DarkBgPrimary,
                    contentColor = BinanceGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                            color = BinanceGold,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = {
                            Text(
                                "Order Book",
                                fontSize = 13.sp,
                                fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeSubTab == 0) BinanceGold else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = {
                            Text(
                                "Market Trades",
                                fontSize = 13.sp,
                                fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeSubTab == 1) BinanceGold else TextSecondary
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (activeSubTab == 0) {
                        OrderBookView(
                            bids = bids,
                            asks = asks,
                            currentPrice = pair?.currentPrice ?: 0.0,
                            onSelectPrice = { price ->
                                onNavigateToTrade(pair?.symbol ?: "BTCUSDT", "BUY")
                            }
                        )
                    } else {
                        RecentTradesView(trades = recentTrades)
                    }
                }
            }
        }

        // Bottom Sticky Action Bar: Buy (Green) and Sell (Red)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCardBg)
                .border(1.dp, DarkBorder)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onNavigateToTrade(pair?.symbol ?: "BTCUSDT", "BUY") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("detail_buy_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Buy ${pair?.baseAsset ?: ""}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = { onNavigateToTrade(pair?.symbol ?: "BTCUSDT", "SELL") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("detail_sell_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CryptoRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Sell ${pair?.baseAsset ?: ""}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
