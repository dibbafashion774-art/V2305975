package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PortfolioSummary
import com.example.data.network.WsConnectionState
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun BinExTopBar(
    connectionState: WsConnectionState,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Identity with Gold Diamond logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onProfileClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BinanceGold, Color(0xFFD49E00))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BinEx",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Connection Status Pill
                    WsStatusBadge(connectionState = connectionState)
                }
                Text(
                    text = "Pro Crypto Exchange",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        // Action Icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("topbar_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search markets",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("topbar_notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = BinanceGold)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated)
                    .border(1.dp, DarkBorder, CircleShape)
                    .clickable { onProfileClick() }
                    .testTag("topbar_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VIP1",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceGold
                )
            }
        }
    }
}

@Composable
fun WsStatusBadge(connectionState: WsConnectionState) {
    val (statusColor, statusText) = when (connectionState) {
        WsConnectionState.LIVE -> Pair(CryptoGreen, "LIVE")
        WsConnectionState.CONNECTING -> Pair(BinanceGold, "SYNC")
        WsConnectionState.DISCONNECTED -> Pair(TextTertiary, "OFFLINE")
        WsConnectionState.ERROR -> Pair(CryptoRed, "RETRY")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = statusText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}

@Composable
fun PortfolioBalanceCard(
    summary: PortfolioSummary,
    onToggleVisibility: () -> Unit,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF222630),
                        DarkCardBg
                    )
                )
            )
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header: Label + Visibility Toggle + Disclaimer Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Total Portfolio Value",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (summary.isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Balance Visibility",
                        tint = TextTertiary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onToggleVisibility() }
                            .testTag("balance_visibility_toggle")
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF16191E))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Demo Spot Trading",
                        fontSize = 10.sp,
                        color = BinanceGold,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large Balance
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (summary.isBalanceVisible) {
                    Text(
                        text = formatCurrency(summary.totalPortfolioValueUsdt),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "USDT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BinanceGold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else {
                    Text(
                        text = "••••••••••",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "USDT",
                        style = MaterialTheme.typography.titleMedium,
                        color = BinanceGold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Today's PnL & Performance
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Today's PnL: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                val pnlColor = if (summary.todayPnlUsdt >= 0) CryptoGreen else CryptoRed
                val prefix = if (summary.todayPnlUsdt >= 0) "+" else ""
                Text(
                    text = if (summary.isBalanceVisible) {
                        "$prefix${formatCurrency(summary.todayPnlUsdt)} USDT ($prefix${String.format("%.2f", summary.todayPnlPercent)}%)"
                    } else "••••",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breakdown Subcard: Available Balance & Assets in Positions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF14171C))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Available Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (summary.isBalanceVisible) "${formatCurrency(summary.availableBalanceUsdt)} USDT" else "••••",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Assets in Positions",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (summary.isBalanceVisible) "${formatCurrency(summary.assetsInPositionsUsdt)} USDT" else "••••",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions requested in prompt:
            // Buy Crypto, Sell Crypto, Markets, Trade, Assets, Transaction History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Buy Crypto",
                    color = CryptoGreen,
                    onClick = { onQuickAction("BUY") }
                )
                QuickActionButton(
                    icon = Icons.Default.ArrowDownward,
                    label = "Sell Crypto",
                    color = CryptoRed,
                    onClick = { onQuickAction("SELL") }
                )
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Markets",
                    color = BinanceGold,
                    onClick = { onQuickAction("MARKETS") }
                )
                QuickActionButton(
                    icon = Icons.Default.CandlestickChart,
                    label = "Trade",
                    color = BinanceGold,
                    onClick = { onQuickAction("TRADE") }
                )
                QuickActionButton(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Assets",
                    color = BinanceGold,
                    onClick = { onQuickAction("ASSETS") }
                )
                QuickActionButton(
                    icon = Icons.Default.History,
                    label = "History",
                    color = TextSecondary,
                    onClick = { onQuickAction("HISTORY") }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("action_${label.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(DarkCardElevated)
                .border(1.dp, DarkBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatCurrency(value: Double): String {
    return String.format("%,.2f", value)
}
