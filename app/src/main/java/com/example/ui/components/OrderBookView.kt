package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketTrade
import com.example.data.model.OrderBookEntry
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoGreenBg
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.CryptoRedBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderBookView(
    bids: List<OrderBookEntry>,
    asks: List<OrderBookEntry>,
    currentPrice: Double,
    onSelectPrice: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCardBg)
            .padding(8.dp)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Price (USDT)", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            Text(text = "Amount", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }

        // Asks (Sells in Red) - show top 6, reversed so closest to market is at bottom
        val displayAsks = asks.take(6).reversed()
        displayAsks.forEach { entry ->
            OrderBookRow(
                entry = entry,
                isBuy = false,
                onClick = { onSelectPrice(entry.price) }
            )
        }

        // Mid-Market Price Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatCryptoPrice(currentPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CryptoGreen
            )
            Text(
                text = "Spot Index",
                fontSize = 10.sp,
                color = TextTertiary
            )
        }

        // Bids (Buys in Green) - show top 6
        val displayBids = bids.take(6)
        displayBids.forEach { entry ->
            OrderBookRow(
                entry = entry,
                isBuy = true,
                onClick = { onSelectPrice(entry.price) }
            )
        }
    }
}

@Composable
private fun OrderBookRow(
    entry: OrderBookEntry,
    isBuy: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isBuy) CryptoGreen else CryptoRed
    val depthBarColor = if (isBuy) CryptoGreenBg else CryptoRedBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .clickable { onClick() }
    ) {
        // Proportional Depth Bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(fraction = entry.relativeDepthPercent.coerceIn(0.05f, 1f))
                .background(depthBarColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatCryptoPrice(entry.price),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = String.format("%.4f", entry.amount),
                fontSize = 11.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun RecentTradesView(
    trades: List<MarketTrade>,
    modifier: Modifier = Modifier
) {
    val timeFmt = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCardBg)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Price (USDT)", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            Text(text = "Amount", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            Text(text = "Time", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }

        trades.take(10).forEach { trade ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCryptoPrice(trade.price),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (trade.isBuy) CryptoGreen else CryptoRed
                )
                Text(
                    text = String.format("%.4f", trade.amount),
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Text(
                    text = timeFmt.format(Date(trade.time)),
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
