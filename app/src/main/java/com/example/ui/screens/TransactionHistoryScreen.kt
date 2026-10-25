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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistoryScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by portfolioViewModel.transactions.collectAsState()
    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = if (selectedFilter == null) {
        allTransactions
    } else {
        allTransactions.filter { it.type == selectedFilter }
    }

    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Filter Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                Pair(null, "All Transactions"),
                Pair(TransactionType.BUY_TRADE, "Spot Buy"),
                Pair(TransactionType.SELL_TRADE, "Spot Sell"),
                Pair(TransactionType.DEPOSIT, "Deposits"),
                Pair(TransactionType.WITHDRAW, "Withdrawals"),
                Pair(TransactionType.TRANSFER, "Transfers")
            )
            items(filters) { (type, label) ->
                val isSel = selectedFilter == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) BinanceGold else DarkCardBg)
                        .clickable { selectedFilter = type }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) Color.Black else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions recorded yet.\nExecute trades or deposit funds to view ledger.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemCard(tx = tx, dateFmt = dateFmt)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    tx: TransactionRecord,
    dateFmt: SimpleDateFormat
) {
    val (icon, iconColor) = when (tx.type) {
        TransactionType.BUY_TRADE -> Pair(Icons.Default.ArrowDownward, CryptoGreen)
        TransactionType.SELL_TRADE -> Pair(Icons.Default.ArrowUpward, CryptoRed)
        TransactionType.DEPOSIT -> Pair(Icons.Default.ArrowDownward, BinanceGold)
        TransactionType.WITHDRAW -> Pair(Icons.Default.ArrowUpward, CryptoRed)
        TransactionType.TRANSFER -> Pair(Icons.Default.SwapHoriz, BinanceGold)
    }

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${dateFmt.format(Date(tx.timestamp))} • ${tx.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                if (tx.details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tx.details,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (tx.type) {
                    TransactionType.BUY_TRADE, TransactionType.DEPOSIT -> "+"
                    TransactionType.SELL_TRADE, TransactionType.WITHDRAW -> "-"
                    TransactionType.TRANSFER -> ""
                }
                Text(
                    text = "$prefix${formatCurrency(tx.amount)} ${tx.asset}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (tx.type) {
                        TransactionType.BUY_TRADE, TransactionType.DEPOSIT -> CryptoGreen
                        TransactionType.SELL_TRADE, TransactionType.WITHDRAW -> CryptoRed
                        TransactionType.TRANSFER -> BinanceGold
                    }
                )
                Text(
                    text = tx.status,
                    fontSize = 11.sp,
                    color = CryptoGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
