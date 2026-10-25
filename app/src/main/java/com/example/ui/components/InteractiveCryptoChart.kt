package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candlestick
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.CryptoRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InteractiveCryptoChart(
    candles: List<Candlestick>,
    isCandleMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(DarkCardBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading real-time market chart...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val activeCandle = selectedIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()

    Column(modifier = modifier.fillMaxWidth()) {
        // Scrubber Inspection Tooltip Bar
        if (activeCandle != null) {
            val isGreen = activeCandle.close >= activeCandle.open
            val chg = if (activeCandle.open > 0) ((activeCandle.close - activeCandle.open) / activeCandle.open) * 100f else 0f
            val timeFmt = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkCardElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFmt.format(Date(activeCandle.openTime)),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "O: ${formatCryptoPrice(activeCandle.open.toDouble())}",
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Text(
                    text = "H: ${formatCryptoPrice(activeCandle.high.toDouble())}",
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Text(
                    text = "L: ${formatCryptoPrice(activeCandle.low.toDouble())}",
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Text(
                    text = "C: ${formatCryptoPrice(activeCandle.close.toDouble())}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGreen) CryptoGreen else CryptoRed
                )
                Text(
                    text = String.format("%s%.2f%%", if (chg >= 0) "+" else "", chg),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGreen) CryptoGreen else CryptoRed
                )
            }
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(DarkCardBg)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectTapGestures(
                            onPress = { offset ->
                                val candleWidth = size.width / candles.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedIndex = index
                                tryAwaitRelease()
                                selectedIndex = null
                            }
                        )
                    }
                    .pointerInput(candles) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val candleWidth = size.width / candles.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedIndex = index
                            },
                            onDrag = { change, _ ->
                                val candleWidth = size.width / candles.size
                                val index = (change.position.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedIndex = index
                            },
                            onDragEnd = {
                                selectedIndex = null
                            },
                            onDragCancel = {
                                selectedIndex = null
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Reserve bottom 22% for volume bars
                val chartHeight = canvasHeight * 0.78f
                val volumeHeight = canvasHeight * 0.22f

                val minPrice = candles.minOfOrNull { it.low } ?: 0f
                val maxPrice = candles.maxOfOrNull { it.high } ?: 1f
                val priceRange = (maxPrice - minPrice).coerceAtLeast(0.0001f)

                val maxVolume = candles.maxOfOrNull { it.volume } ?: 1f
                val volRange = maxVolume.coerceAtLeast(0.0001f)

                val candleCount = candles.size
                val stepX = canvasWidth / candleCount
                val candleBodyWidth = (stepX * 0.7f).coerceIn(2f, 18f)

                // Draw horizontal grid lines (4 lines)
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                for (i in 0..3) {
                    val y = (chartHeight / 3) * i
                    drawLine(
                        color = DarkBorder,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )
                }

                if (isCandleMode) {
                    // Draw Candlesticks & Volume
                    candles.forEachIndexed { i, candle ->
                        val xCenter = i * stepX + (stepX / 2f)
                        val isGreen = candle.close >= candle.open
                        val color = if (isGreen) CryptoGreen else CryptoRed

                        // Normalize Y values for price
                        val yHigh = chartHeight - ((candle.high - minPrice) / priceRange) * chartHeight
                        val yLow = chartHeight - ((candle.low - minPrice) / priceRange) * chartHeight
                        val yOpen = chartHeight - ((candle.open - minPrice) / priceRange) * chartHeight
                        val yClose = chartHeight - ((candle.close - minPrice) / priceRange) * chartHeight

                        // 1. Draw Wick
                        drawLine(
                            color = color,
                            start = Offset(xCenter, yHigh),
                            end = Offset(xCenter, yLow),
                            strokeWidth = 1.5f
                        )

                        // 2. Draw Body
                        val topBody = minOf(yOpen, yClose)
                        val bottomBody = maxOf(yOpen, yClose)
                        val bodyHeight = (bottomBody - topBody).coerceAtLeast(2f)

                        drawRect(
                            color = color,
                            topLeft = Offset(xCenter - (candleBodyWidth / 2f), topBody),
                            size = Size(candleBodyWidth, bodyHeight)
                        )

                        // 3. Draw Volume Bar at bottom
                        val barHeight = (candle.volume / volRange) * volumeHeight
                        drawRect(
                            color = color.copy(alpha = 0.35f),
                            topLeft = Offset(xCenter - (candleBodyWidth / 2f), canvasHeight - barHeight),
                            size = Size(candleBodyWidth, barHeight)
                        )
                    }
                } else {
                    // Line / Area Mode
                    val linePath = Path()
                    val areaPath = Path()

                    areaPath.moveTo(0f, chartHeight)

                    candles.forEachIndexed { i, candle ->
                        val x = i * stepX + (stepX / 2f)
                        val y = chartHeight - ((candle.close - minPrice) / priceRange) * chartHeight
                        if (i == 0) {
                            linePath.moveTo(x, y)
                            areaPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            areaPath.lineTo(x, y)
                        }
                    }

                    areaPath.lineTo(canvasWidth, chartHeight)
                    areaPath.close()

                    // Draw Gradient Fill
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                BinanceGold.copy(alpha = 0.3f),
                                BinanceGold.copy(alpha = 0.0f)
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw Gold Curve
                    drawPath(
                        path = linePath,
                        color = BinanceGold,
                        style = Stroke(width = 2.5f)
                    )

                    // Draw Volume Bars
                    candles.forEachIndexed { i, candle ->
                        val xCenter = i * stepX + (stepX / 2f)
                        val isGreen = candle.close >= candle.open
                        val color = if (isGreen) CryptoGreen else CryptoRed
                        val barHeight = (candle.volume / volRange) * volumeHeight
                        drawRect(
                            color = color.copy(alpha = 0.3f),
                            topLeft = Offset(xCenter - (candleBodyWidth / 2f), canvasHeight - barHeight),
                            size = Size(candleBodyWidth, barHeight)
                        )
                    }
                }

                // Crosshair cursor when scrubbing
                selectedIndex?.let { idx ->
                    val x = idx * stepX + (stepX / 2f)
                    val candle = candles[idx]
                    val y = chartHeight - ((candle.close - minPrice) / priceRange) * chartHeight

                    // Vertical Guideline
                    drawLine(
                        color = TextSecondary,
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )

                    // Horizontal Guideline
                    drawLine(
                        color = TextSecondary,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )

                    // Highlight Dot
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
