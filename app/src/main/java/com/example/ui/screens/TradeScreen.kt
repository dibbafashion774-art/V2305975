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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoPairInfo
import com.example.data.model.OrderItem
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.ui.components.CoinAvatarBadge
import com.example.ui.components.OrderBookView
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
import com.example.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(
    tradingViewModel: TradingViewModel,
    availablePairs: List<CryptoPairInfo>,
    onOpenChart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPair by tradingViewModel.currentPair.collectAsState()
    val side by tradingViewModel.side.collectAsState()
    val orderType by tradingViewModel.orderType.collectAsState()
    val priceInput by tradingViewModel.priceInput.collectAsState()
    val quantityInput by tradingViewModel.quantityInput.collectAsState()
    val selectedPercentage by tradingViewModel.selectedPercentage.collectAsState()
    val isSubmitting by tradingViewModel.isSubmitting.collectAsState()
    val feedbackMessage by tradingViewModel.feedbackMessage.collectAsState()
    val isError by tradingViewModel.isError.collectAsState()

    val availableUsdt by tradingViewModel.availableUsdt.collectAsState()
    val availableCoinQty by tradingViewModel.availableCoinQty.collectAsState()
    val openOrders by tradingViewModel.openOrders.collectAsState()
    val orderHistory by tradingViewModel.orderHistory.collectAsState()
    val (bids, asks) = tradingViewModel.orderBook.collectAsState().value

    var showPairSelectorSheet by remember { mutableStateOf(false) }
    var selectedOrderTab by remember { mutableIntStateOf(0) } // 0: Open Orders, 1: Order History

    val baseAsset = currentPair?.baseAsset ?: "BTC"
    val quoteAsset = "USDT"

    // Calculate derived order values
    val currentP = if (orderType == OrderType.MARKET) (currentPair?.currentPrice ?: 0.0) else (priceInput.toDoubleOrNull() ?: 0.0)
    val qty = quantityInput.toDoubleOrNull() ?: 0.0
    val totalOrderValue = qty * currentP
    val estimatedFee = totalOrderValue * 0.001

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        // Pair Selector Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showPairSelectorSheet = true }
                    .padding(vertical = 4.dp, horizontal = 6.dp)
                    .testTag("pair_selector_trigger")
            ) {
                CoinAvatarBadge(asset = baseAsset)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$baseAsset/$quoteAsset",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select pair",
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                val chg = currentPair?.priceChangePercent ?: 0.0
                Text(
                    text = String.format("%s%.2f%%", if (chg >= 0) "+" else "", chg),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (chg >= 0) CryptoGreen else CryptoRed
                )
            }

            IconButton(onClick = { currentPair?.let { onOpenChart(it.symbol) } }) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "View Chart",
                    tint = BinanceGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Feedback Banner
        if (feedbackMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isError) Color(0x33F6465D) else Color(0x330ECB81))
                    .border(1.dp, if (isError) CryptoRed else CryptoGreen, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isError) CryptoRed else CryptoGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = feedbackMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { tradingViewModel.dismissFeedback() },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Main Trading Interface: Left form + Right mini OrderBook
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT COLUMN: Buy/Sell Switcher + Order Form (Weight 1.15)
                    Column(
                        modifier = Modifier.weight(1.15f)
                    ) {
                        // Buy / Sell Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCardBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(if (side == OrderSide.BUY) CryptoGreen else Color.Transparent)
                                    .clickable { tradingViewModel.setSide(OrderSide.BUY) }
                                    .testTag("trade_side_buy"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Buy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (side == OrderSide.BUY) Color.White else TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(if (side == OrderSide.SELL) CryptoRed else Color.Transparent)
                                    .clickable { tradingViewModel.setSide(OrderSide.SELL) }
                                    .testTag("trade_side_sell"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sell",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (side == OrderSide.SELL) Color.White else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Market vs Limit Order Type Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(OrderType.MARKET, OrderType.LIMIT).forEach { type ->
                                val isSel = orderType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) DarkCardElevated else Color.Transparent)
                                        .border(1.dp, if (isSel) BinanceGold else DarkBorder, RoundedCornerShape(6.dp))
                                        .clickable { tradingViewModel.setOrderType(type) }
                                        .padding(vertical = 6.dp)
                                        .testTag("order_type_${type.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (type == OrderType.MARKET) "Market" else "Limit",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) BinanceGold else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Price Input (if Limit order, or readonly if Market)
                        if (orderType == OrderType.LIMIT) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkCardBg)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { tradingViewModel.decrementPrice(1.0) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease price", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                OutlinedTextField(
                                    value = priceInput,
                                    onValueChange = { tradingViewModel.setPrice(it) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    placeholder = { Text("Price", color = TextTertiary, fontSize = 13.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("trade_price_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = { tradingViewModel.incrementPrice(1.0) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase price", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkCardBg)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Market Price", fontSize = 13.sp, color = TextSecondary)
                                    Text(
                                        text = "${formatCryptoPrice(currentPair?.currentPrice ?: 0.0)} USDT",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quantity Input
                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { tradingViewModel.setQuantity(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("Amount ($baseAsset)", color = TextTertiary, fontSize = 13.sp) },
                            trailingIcon = {
                                Text(text = baseAsset, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(end = 12.dp))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trade_amount_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BinanceGold,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkCardBg,
                                unfocusedContainerColor = DarkCardBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Percentage Chips: 25%, 50%, 75%, 100%
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(25, 50, 75, 100).forEach { p ->
                                val isSel = selectedPercentage == p
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) BinanceGold else DarkCardBg)
                                        .clickable { tradingViewModel.selectPercentage(p) }
                                        .padding(vertical = 4.dp)
                                        .testTag("percent_chip_$p"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$p%",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Available Balance & Fee Info
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCardBg)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Avail USDT:", fontSize = 11.sp, color = TextTertiary)
                                Text(
                                    text = "${formatCurrency(availableUsdt)} USDT",
                                    fontSize = 11.sp,
                                    color = BinanceGold,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Avail $baseAsset:", fontSize = 11.sp, color = TextTertiary)
                                Text(
                                    text = String.format("%.4f %s", availableCoinQty, baseAsset),
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Order Value:", fontSize = 11.sp, color = TextTertiary)
                                Text(
                                    text = "${formatCurrency(totalOrderValue)} USDT",
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Fee (0.1%):", fontSize = 11.sp, color = TextTertiary)
                                Text(
                                    text = "${formatCurrency(estimatedFee)} USDT",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Big Action Button
                        Button(
                            onClick = { tradingViewModel.submitOrder() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("trade_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (side == OrderSide.BUY) CryptoGreen else CryptoRed
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (side == OrderSide.BUY) "Buy $baseAsset" else "Sell $baseAsset",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }

                    // RIGHT COLUMN: Mini Order Book (Weight 0.95)
                    Column(modifier = Modifier.weight(0.95f)) {
                        OrderBookView(
                            bids = bids,
                            asks = asks,
                            currentPrice = currentPair?.currentPrice ?: 0.0,
                            onSelectPrice = { tradingViewModel.selectPriceFromBook(it) }
                        )
                    }
                }
            }

            // Open Orders / Order History Tab Bar
            item {
                Spacer(modifier = Modifier.height(14.dp))
                TabRow(
                    selectedTabIndex = selectedOrderTab,
                    containerColor = DarkBgPrimary,
                    contentColor = BinanceGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedOrderTab]),
                            color = BinanceGold,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedOrderTab == 0,
                        onClick = { selectedOrderTab = 0 },
                        text = {
                            Text(
                                text = "Open Orders (${openOrders.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedOrderTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedOrderTab == 0) BinanceGold else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedOrderTab == 1,
                        onClick = { selectedOrderTab = 1 },
                        text = {
                            Text(
                                text = "Order History",
                                fontSize = 13.sp,
                                fontWeight = if (selectedOrderTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedOrderTab == 1) BinanceGold else TextSecondary
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Content
            if (selectedOrderTab == 0) {
                if (openOrders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No open orders.\nPlace a limit order to view it here.",
                                color = TextTertiary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(openOrders, key = { it.id }) { order ->
                        OrderItemRow(
                            order = order,
                            onCancel = { tradingViewModel.cancelOrder(order.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                if (orderHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No trade history yet.",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(orderHistory, key = { it.id }) { order ->
                        OrderItemRow(
                            order = order,
                            onCancel = null,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Selecting Trading Pair
    if (showPairSelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPairSelectorSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = DarkCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Select Trading Pair",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(availablePairs) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tradingViewModel.setPair(p.symbol)
                                    showPairSelectorSheet = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoinAvatarBadge(asset = p.baseAsset)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${p.baseAsset}/USDT",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = p.fullName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCryptoPrice(p.currentPrice),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = p.displayChange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (p.isPositive) CryptoGreen else CryptoRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(
    order: OrderItem,
    onCancel: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val dateFmt = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val isBuy = order.side == OrderSide.BUY

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBuy) "BUY" else "SELL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBuy) CryptoGreen else CryptoRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.symbol,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.type.name,
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }

                if (onCancel != null && order.status == OrderStatus.OPEN) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Cancel", fontSize = 11.sp, color = CryptoRed)
                    }
                } else {
                    Text(
                        text = order.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (order.status) {
                            OrderStatus.FILLED -> CryptoGreen
                            OrderStatus.CANCELLED -> CryptoRed
                            OrderStatus.OPEN -> BinanceGold
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Price: ${formatCryptoPrice(order.price)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Amount: ${String.format("%.4f", order.quantity)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Total: ${formatCurrency(order.totalValue)}",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFmt.format(Date(order.timestamp)),
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}
