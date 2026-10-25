package com.example.ui.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WalletType
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.CryptoGreen
import com.example.ui.theme.DarkBgPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DepositDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var selectedNetwork by remember { mutableStateOf("TRC20") }
    var amountText by remember { mutableStateOf("100000.00") }
    val networks = listOf("TRC20", "BEP20", "ERC20")
    val depositAddress = "TQn9Y2khEsLJW1ChVWFMSMeRDow5KNuX5T"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deposit USDT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Choose Network", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    networks.forEach { net ->
                        val isSel = selectedNetwork == net
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) BinanceGold else DarkCardElevated)
                                .clickable { selectedNetwork = net }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = net,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.Black else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Deposit Address Display
                Text(text = "Deposit Address (Simulation)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBgPrimary)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${depositAddress.take(14)}...${depositAddress.takeLast(6)}",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy address",
                        tint = BinanceGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Deposit Amount (USDT)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBgPrimary,
                        unfocusedContainerColor = DarkBgPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onConfirm(amount, "USDT", selectedNetwork)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("deposit_confirm_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold)
                ) {
                    Text(text = "Confirm Simulation Deposit", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WithdrawDialog(
    availableUsdt: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, String) -> Unit
) {
    var addressText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf("TRC20") }
    val networkFee = 1.00 // 1 USDT

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Withdraw USDT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Destination Address", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    placeholder = { Text("Enter recipient address", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBgPrimary,
                        unfocusedContainerColor = DarkBgPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Amount (USDT)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        text = "Avail: ${formatCurrency(availableUsdt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BinanceGold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text("Minimum 10 USDT", color = TextTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        Text(
                            text = "MAX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BinanceGold,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable {
                                    val maxSend = (availableUsdt - networkFee).coerceAtLeast(0.0)
                                    amountText = String.format("%.2f", maxSend)
                                }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBgPrimary,
                        unfocusedContainerColor = DarkBgPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Network Fee", fontSize = 11.sp, color = TextTertiary)
                    Text(text = "1.00 USDT", fontSize = 11.sp, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && addressText.isNotBlank()) {
                            onConfirm(amount, "USDT", addressText, selectedNetwork)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold)
                ) {
                    Text(text = "Submit Withdrawal", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TransferDialog(
    spotAvailable: Double,
    fundingAvailable: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, WalletType, WalletType) -> Unit
) {
    var fromWallet by remember { mutableStateOf(WalletType.SPOT) }
    val toWallet = if (fromWallet == WalletType.SPOT) WalletType.FUNDING else WalletType.SPOT
    val maxAvailable = if (fromWallet == WalletType.SPOT) spotAvailable else fundingAvailable
    var amountText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transfer USDT",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Direction switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkBgPrimary)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "From: ${fromWallet.name} Wallet", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "To: ${toWallet.name} Wallet", style = MaterialTheme.typography.bodyMedium, color = BinanceGold, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { fromWallet = toWallet }
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Switch direction", tint = BinanceGold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Transfer Amount", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(text = "Avail: ${formatCurrency(maxAvailable)} USDT", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        Text(
                            text = "MAX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BinanceGold,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { amountText = String.format("%.2f", maxAvailable) }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BinanceGold,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBgPrimary,
                        unfocusedContainerColor = DarkBgPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onConfirm(amount, fromWallet, toWallet)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGold)
                ) {
                    Text(text = "Confirm Transfer", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
