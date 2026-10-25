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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ProfileScreen(
    portfolioViewModel: PortfolioViewModel,
    modifier: Modifier = Modifier
) {
    val summary by portfolioViewModel.portfolioSummary.collectAsState()

    var biometricEnabled by remember { mutableStateOf(true) }
    var twoFactorEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf<String?>(null) } // "Terms" or "Privacy"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        Text(
            text = "Account & Security",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Profile VIP Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(DarkCardElevated)
                                .border(2.dp, BinanceGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "BX", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BinanceGold)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Trader_8820491",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BinanceGold)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(text = "VIP 1", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "User ID: 88204919", fontSize = 11.sp, color = TextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Identity Verified (Tier 2)", fontSize = 11.sp, color = CryptoGreen, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Security Settings Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Security & Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SettingToggleRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric Authentication",
                            subtitle = "Require fingerprint/face to open application",
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it }
                        )

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                        SettingToggleRow(
                            icon = Icons.Default.Security,
                            title = "Two-Factor Authentication",
                            subtitle = "Google Authenticator / Security Keys",
                            checked = twoFactorEnabled,
                            onCheckedChange = { twoFactorEnabled = it }
                        )

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                        SettingToggleRow(
                            icon = Icons.Default.Visibility,
                            title = "Balance Visibility",
                            subtitle = "Show or mask balances across app",
                            checked = summary.isBalanceVisible,
                            onCheckedChange = { portfolioViewModel.toggleBalanceVisibility() }
                        )

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                        SettingToggleRow(
                            icon = Icons.Default.Notifications,
                            title = "Push Notifications",
                            subtitle = "Price alerts & order execution updates",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }

            // Application & System Settings Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Preferences & Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        InfoRow(label = "Base Currency", value = "USDT (Tether USD)")
                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "Market Data Gateway", value = "Binance Public REST & WebSocket")
                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))
                        InfoRow(label = "App Version", value = "BinEx Pro v2.8.0")
                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLegalDialog = "Terms" },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Terms of Service", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                        }

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLegalDialog = "Privacy" },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Privacy Policy", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Simulation Balance Reset Action
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CryptoRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reset_simulation_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CryptoRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Reset Simulation Balance (7,000,000.00 USDT)", color = CryptoRed, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Confirmation Dialog for Resetting Simulation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset Simulation Portfolio?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "This will clear all open orders, trade history, and cryptocurrency holdings, restoring your available balance to the initial 7,000,000.00 USDT.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        portfolioViewModel.resetSimulation()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRed)
                ) {
                    Text(text = "Confirm Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCardBg
        )
    }

    // Legal / Terms Dialog
    if (showLegalDialog != null) {
        AlertDialog(
            onDismissRequest = { showLegalDialog = null },
            title = { Text(text = if (showLegalDialog == "Terms") "Terms of Service" else "Privacy Policy", color = TextPrimary) },
            text = {
                Text(
                    text = if (showLegalDialog == "Terms") {
                        "BinEx is a high-performance cryptocurrency trading interface powered by real-time Binance public market data. All trades executed within the application are processed via the internal spot simulation engine with real-time quote validation. No actual fiat or blockchain transfer occurs without authenticated third-party wallet integration."
                    } else {
                        "Your privacy and security are paramount. BinEx stores local preferences, portfolio states, and simulated orders on-device using encrypted local storage. Market data is fetched directly from Binance public read-only APIs without transmitting personal identification data."
                    },
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLegalDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold)
                ) {
                    Text(text = "Got It", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCardBg
        )
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = BinanceGold, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = BinanceGold,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DarkCardElevated
            )
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
