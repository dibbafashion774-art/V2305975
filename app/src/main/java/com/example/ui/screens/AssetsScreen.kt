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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HoldingItem
import com.example.data.model.WalletType
import com.example.ui.components.CoinAvatarBadge
import com.example.ui.components.DepositDialog
import com.example.ui.components.TransferDialog
import com.example.ui.components.WithdrawDialog
import com.example.ui.components.formatCryptoPrice
import com.example.ui.components.formatCurrency
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
import com.example.viewmodel.PortfolioViewModel

@Composable
fun AssetsScreen(
    portfolioViewModel: PortfolioViewModel,
    onNavigateToTrade: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by portfolioViewModel.portfolioSummary.collectAsState()
    val selectedWallet by portfolioViewModel.selectedWallet.collectAsState()

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var selectedWalletTabIndex by remember { mutableIntStateOf(0) } // 0: Overview, 1: Spot, 2: Funding

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Assets & Wallet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = onNavigateToHistory) {
                Icon(Icons.Default.History, contentDescription = "History", tint = TextSecondary)
            }
        }

        // Wallet Tabs: Overview, Spot, Funding
        TabRow(
            selectedTabIndex = selectedWalletTabIndex,
            containerColor = DarkBgPrimary,
            contentColor = BinanceGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedWalletTabIndex]),
                    color = BinanceGold,
                    height = 2.dp
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            listOf("Overview", "Spot Wallet", "Funding Wallet").forEachIndexed { idx, title ->
                Tab(
                    selected = selectedWalletTabIndex == idx,
                    onClick = {
                        selectedWalletTabIndex = idx
                        if (idx == 1) portfolioViewModel.setSelectedWallet(WalletType.SPOT)
                        if (idx == 2) portfolioViewModel.setSelectedWallet(WalletType.FUNDING)
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedWalletTabIndex == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedWalletTabIndex == idx) BinanceGold else TextSecondary
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Main Asset Valuation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedWalletTabIndex) {
                                    1 -> "Spot Balance (USDT)"
                                    2 -> "Funding Balance (USDT)"
                                    else -> "Total Asset Value (USDT)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = if (summary.isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = TextTertiary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { portfolioViewModel.toggleBalanceVisibility() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val displayTotal = when (selectedWalletTabIndex) {
                            1 -> summary.availableBalanceUsdt + summary.assetsInPositionsUsdt
                            2 -> summary.fundingBalanceUsdt
                            else -> summary.totalPortfolioValueUsdt
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (summary.isBalanceVisible) formatCurrency(displayTotal) else "••••••••",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "USDT",
                                style = MaterialTheme.typography.titleMedium,
                                color = BinanceGold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Profit & Loss info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Unrealized PnL: ", fontSize = 12.sp, color = TextTertiary)
                            val pnlColor = if (summary.todayPnlUsdt >= 0) CryptoGreen else CryptoRed
                            val sign = if (summary.todayPnlUsdt >= 0) "+" else ""
                            Text(
                                text = if (summary.isBalanceVisible) "$sign${formatCurrency(summary.todayPnlUsdt)} USDT ($sign${String.format("%.2f", summary.todayPnlPercent)}%)" else "••••",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = pnlColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Row: Deposit, Withdraw, Transfer, Receive
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AssetActionButton(
                                icon = Icons.Default.ArrowDownward,
                                label = "Deposit",
                                onClick = { showDepositDialog = true }
                            )
                            AssetActionButton(
                                icon = Icons.Default.ArrowUpward,
                                label = "Withdraw",
                                onClick = { showWithdrawDialog = true }
                            )
                            AssetActionButton(
                                icon = Icons.Default.SwapHoriz,
                                label = "Transfer",
                                onClick = { showTransferDialog = true }
                            )
                            AssetActionButton(
                                icon = Icons.Default.QrCode,
                                label = "Receive",
                                onClick = { showDepositDialog = true }
                            )
                        }
                    }
                }
            }

            // Asset Allocation Visualizer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Asset Allocation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                AssetAllocationBar(summary = summary)
            }

            // Holdings Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cryptocurrency Holdings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${summary.holdings.size} Positions",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Cash USDT Row
            item {
                HoldingCashRow(
                    availableUsdt = summary.availableBalanceUsdt,
                    isVisible = summary.isBalanceVisible
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Holdings List
            if (summary.holdings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No crypto holdings in position.\nBuy crypto on Spot to build your portfolio.",
                            color = TextTertiary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(summary.holdings, key = { it.symbol }) { holding ->
                    HoldingItemCard(
                        holding = holding,
                        isVisible = summary.isBalanceVisible,
                        onTradeClick = { onNavigateToTrade("${holding.symbol}USDT") }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }

    // Dialogs
    if (showDepositDialog) {
        DepositDialog(
            onDismiss = { showDepositDialog = false },
            onConfirm = { amount, asset, net ->
                portfolioViewModel.deposit(amount, asset, net)
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            availableUsdt = summary.availableBalanceUsdt,
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount, asset, addr, net ->
                portfolioViewModel.withdraw(amount, asset, addr, net)
            }
        )
    }

    if (showTransferDialog) {
        TransferDialog(
            spotAvailable = summary.availableBalanceUsdt,
            fundingAvailable = summary.fundingBalanceUsdt,
            onDismiss = { showTransferDialog = false },
            onConfirm = { amount, from, to ->
                portfolioViewModel.transfer(amount, from, to)
            }
        )
    }
}

@Composable
fun AssetActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("asset_btn_${label.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DarkCardElevated)
                .border(1.dp, DarkBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BinanceGold,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AssetAllocationBar(summary: com.example.data.model.PortfolioSummary) {
    val total = summary.totalPortfolioValueUsdt.coerceAtLeast(1.0)
    val usdtRatio = (summary.availableBalanceUsdt / total).toFloat().coerceIn(0f, 1f)
    val cryptoRatio = (summary.assetsInPositionsUsdt / total).toFloat().coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Stacked progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(DarkCardElevated)
            ) {
                if (usdtRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = usdtRatio)
                            .background(BinanceGold)
                    )
                }
                if (cryptoRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .background(CryptoGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BinanceGold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "USDT: ${String.format("%.1f%%", usdtRatio * 100f)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CryptoGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Crypto Assets: ${String.format("%.1f%%", cryptoRatio * 100f)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun HoldingCashRow(
    availableUsdt: Double,
    isVisible: Boolean
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinAvatarBadge(asset = "USDT")
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Tether USD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "USDT", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isVisible) "${formatCurrency(availableUsdt)} USDT" else "••••",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Cash Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = BinanceGold
                )
            }
        }
    }
}

@Composable
fun HoldingItemCard(
    holding: HoldingItem,
    isVisible: Boolean,
    onTradeClick: () -> Unit
) {
    val pnlColor = if (holding.unrealizedPnl >= 0) CryptoGreen else CryptoRed
    val sign = if (holding.unrealizedPnl >= 0) "+" else ""

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinAvatarBadge(asset = holding.baseAsset)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = holding.baseAsset, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = holding.coinName, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }

                Button(
                    onClick = onTradeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(text = "Trade", fontSize = 11.sp, color = BinanceGold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Holdings", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        text = if (isVisible) String.format("%.4f %s", holding.quantity, holding.baseAsset) else "••••",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Avg Buy Price", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        text = if (isVisible) formatCryptoPrice(holding.averageBuyPrice) else "••••",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Market Value", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        text = if (isVisible) "${formatCurrency(holding.currentMarketValue)} USDT" else "••••",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Unrealized PnL:", fontSize = 11.sp, color = TextTertiary)
                Text(
                    text = if (isVisible) "$sign${formatCurrency(holding.unrealizedPnl)} USDT ($sign${String.format("%.2f", holding.unrealizedPnlPercent)}%)" else "••••",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
            }
        }
    }
}
