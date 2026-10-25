package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CryptoDarkColorScheme = darkColorScheme(
    primary = BinanceGold,
    onPrimary = DarkBgSecondary,
    primaryContainer = DarkCardElevated,
    onPrimaryContainer = BinanceGold,
    secondary = BinanceGoldHover,
    onSecondary = DarkBgSecondary,
    secondaryContainer = DarkCardBg,
    onSecondaryContainer = TextPrimary,
    background = DarkBgPrimary,
    onBackground = TextPrimary,
    surface = DarkBgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = CryptoRed,
    onError = TextPrimary,
    errorContainer = CryptoRedBg,
    onErrorContainer = CryptoRed
)

@Composable
fun BinExTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CryptoDarkColorScheme,
        typography = Typography,
        content = content
    )
}
