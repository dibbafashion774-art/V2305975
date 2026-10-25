package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoPairInfo
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun CryptoPriceRow(
    pair: CryptoPairInfo,
    onClick: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    showSparkline: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isPos = pair.isPositive
    val changeColor = if (isPos) CryptoGreen else CryptoRed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("pair_row_${pair.symbol}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Coin Icon + Symbol + Full Name + Volume
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.2f)
        ) {
            CoinAvatarBadge(asset = pair.baseAsset)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pair.baseAsset,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "/USDT",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Vol ${formatVolume(pair.turnover24h)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        // Center: Mini Sparkline
        if (showSparkline && pair.sparklinePoints.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(28.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                MiniSparkline(
                    points = pair.sparklinePoints,
                    lineColor = changeColor
                )
            }
        }

        // Right: Price + 24h Change Pill + Favorite Star
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCryptoPrice(pair.currentPrice),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(changeColor)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = pair.displayChange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (onToggleFavorite != null) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(start = 6.dp)
                        .testTag("fav_button_${pair.symbol}")
                ) {
                    Icon(
                        imageVector = if (pair.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (pair.isFavorite) BinanceGold else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CoinAvatarBadge(asset: String) {
    val bg = when (asset) {
        "BTC" -> Color(0xFFF7931A)
        "ETH" -> Color(0xFF627EEA)
        "BNB" -> BinanceGold
        "SOL" -> Color(0xFF14F195)
        "XRP" -> Color(0xFF23292F)
        "DOGE" -> Color(0xFFC2A633)
        "ADA" -> Color(0xFF0033AD)
        "AVAX" -> Color(0xFFE84142)
        "LINK" -> Color(0xFF375BD2)
        "SUI" -> Color(0xFF4CA2FE)
        "TON" -> Color(0xFF0098EA)
        else -> DarkCardElevated
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = asset.take(3),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (asset == "BNB") BinanceGold else bg
        )
    }
}

@Composable
fun MiniSparkline(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    Canvas(modifier = modifier.size(width = 60.dp, height = 24.dp)) {
        val min = points.minOrNull() ?: 0f
        val max = points.maxOrNull() ?: 1f
        val range = (max - min).coerceAtLeast(0.0001f)

        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)

        val path = Path()
        points.forEachIndexed { i, p ->
            val x = i * stepX
            val normalizedY = (p - min) / range
            val y = height - (normalizedY * height)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

fun formatCryptoPrice(price: Double): String {
    return when {
        price >= 1000.0 -> String.format("%,.2f", price)
        price >= 1.0 -> String.format("%.3f", price)
        price >= 0.01 -> String.format("%.4f", price)
        else -> String.format("%.6f", price)
    }
}

fun formatVolume(vol: Double): String {
    return when {
        vol >= 1_000_000_000.0 -> String.format("%.2fB", vol / 1_000_000_000.0)
        vol >= 1_000_000.0 -> String.format("%.2fM", vol / 1_000_000.0)
        vol >= 1_000.0 -> String.format("%.2fK", vol / 1_000.0)
        else -> String.format("%.0f", vol)
    }
}
