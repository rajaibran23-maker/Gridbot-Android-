package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SymbolCategory
import com.example.model.TradingSymbol
import com.example.ui.theme.AccentCrypto
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentOil
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingRobotViewModel

@Composable
fun LotSizeSymbolsScreen(
    viewModel: TradingRobotViewModel
) {
    val lotSize by viewModel.lotSize.collectAsState()
    val symbols by viewModel.symbols.collectAsState()

    var customLotText by remember(lotSize) { mutableStateOf(String.format("%.2f", lotSize)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "TRADING PARAMETERS",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Lot Size and Symbols",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Configure volume per trade (default 0.05) and toggle individual trading instruments.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // 1. LOT SIZE INPUT & QUICK STEP SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Automated Trade Lot Size",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Default: 0.05",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.incrementLotSize(-0.01f) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyCard)
                            .testTag("lot_size_minus_button")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Lot", tint = TextPrimary)
                    }

                    OutlinedTextField(
                        value = customLotText,
                        onValueChange = { input ->
                            customLotText = input
                            val parsed = input.toFloatOrNull()
                            if (parsed != null && parsed > 0) {
                                viewModel.updateLotSize(parsed)
                            }
                        },
                        label = { Text("Lot Size (0.01 - 100.0)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lot_size_input"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = NavyCardBorder,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = NavyCard,
                            unfocusedContainerColor = NavyCard
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = { viewModel.incrementLotSize(0.01f) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyCard)
                            .testTag("lot_size_plus_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Lot", tint = TextPrimary)
                    }
                }

                // Preset Lot Size Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.01f, 0.05f, 0.10f, 0.20f, 0.50f).forEach { preset ->
                        val isSelected = Math.abs(lotSize - preset) < 0.005f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else NavyCard)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else NavyCardBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = { viewModel.updateLotSize(preset) },
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = String.format("%.2f", preset),
                                    color = if (isSelected) NeonCyan else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. TRADING SYMBOLS SELECTION LIST
        Text(
            text = "Trading Symbols (Individual Switches)",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(symbols, key = { it.id }) { symbol ->
                SymbolCard(
                    symbol = symbol,
                    onToggle = { isChecked ->
                        viewModel.toggleSymbol(symbol.id, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun SymbolCard(
    symbol: TradingSymbol,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("symbol_card_${symbol.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (symbol.isEnabled) EmeraldGreen.copy(alpha = 0.5f) else NavyCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (symbol.category) {
                                SymbolCategory.COMMODITIES -> AccentGold.copy(alpha = 0.15f)
                                SymbolCategory.CRYPTO -> AccentCrypto.copy(alpha = 0.15f)
                                SymbolCategory.FOREX -> NeonCyan.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getSymbolIcon(symbol.id),
                        contentDescription = null,
                        tint = when (symbol.category) {
                            SymbolCategory.COMMODITIES -> AccentGold
                            SymbolCategory.CRYPTO -> AccentCrypto
                            SymbolCategory.FOREX -> NeonCyan
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = symbol.displayName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${symbol.code})",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Bid: ${String.format("%.${symbol.digits}f", symbol.bid)}",
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Ask: ${String.format("%.${symbol.digits}f", symbol.ask)}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (symbol.isEnabled) "ON" else "OFF",
                    color = if (symbol.isEnabled) EmeraldGreen else CrimsonRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Switch(
                    checked = symbol.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("symbol_switch_${symbol.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldGreen,
                        checkedTrackColor = EmeraldGreen.copy(alpha = 0.35f),
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = NavyCard
                    )
                )
            }
        }
    }
}

fun getSymbolIcon(id: String): ImageVector {
    return when (id) {
        "gold" -> Icons.Default.MonetizationOn
        "bitcoin" -> Icons.Default.CurrencyBitcoin
        "oil" -> Icons.Default.OilBarrel
        else -> Icons.Default.LocalAtm
    }
}
