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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogType
import com.example.ui.theme.AmberWarning
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RobotControlScreen(
    viewModel: TradingRobotViewModel
) {
    val isRobotOn by viewModel.isRobotOn.collectAsState()
    val brokerConfig by viewModel.brokerConfig.collectAsState()
    val vpsConfig by viewModel.vpsConfig.collectAsState()
    val lotSize by viewModel.lotSize.collectAsState()
    val symbols by viewModel.symbols.collectAsState()
    val activeTrades by viewModel.activeTrades.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val totalProfit by viewModel.totalProfit.collectAsState()

    val enabledSymbolsCount = symbols.count { it.isEnabled }
    val readyToRun = brokerConfig.isConnected && vpsConfig.isConnected && enabledSymbolsCount > 0

    val timeFormatter = rememberDateFormatter()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(
                text = "AUTOMATED EXECUTION",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Robot Control",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Grid strategy rules are prepared. Live/demo order execution remains locked until a verified MT5 bridge is connected.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // 1. ONE MAIN ON/OFF SWITCH CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("robot_master_control_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (isRobotOn) EmeraldGreen else NavyCardBorder
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isRobotOn) EmeraldGreen.copy(alpha = 0.2f) else CrimsonRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = if (isRobotOn) EmeraldGreen else CrimsonRed,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Automated Robot State",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isRobotOn) "ROBOT IS ON" else "ROBOT IS OFF",
                                color = if (isRobotOn) EmeraldGreen else CrimsonRed,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Switch(
                        checked = isRobotOn,
                        onCheckedChange = { viewModel.toggleRobot(it) },
                        modifier = Modifier
                            .size(60.dp, 36.dp)
                            .testTag("robot_main_toggle_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGreen,
                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.35f),
                            uncheckedThumbColor = CrimsonRed,
                            uncheckedTrackColor = NavyCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connection Requirements Checklist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConditionPill(
                        label = "Broker",
                        isMet = brokerConfig.isConnected,
                        modifier = Modifier.weight(1f)
                    )
                    ConditionPill(
                        label = "VPS",
                        isMet = vpsConfig.isConnected,
                        modifier = Modifier.weight(1f)
                    )
                    ConditionPill(
                        label = "Symbols ($enabledSymbolsCount)",
                        isMet = enabledSymbolsCount > 0,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start and Stop Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startRobot() },
                        enabled = !isRobotOn,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("robot_start_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            disabledContainerColor = NavyCard
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (!isRobotOn) NavyDark else TextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START",
                            color = if (!isRobotOn) NavyDark else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.stopRobot() },
                        enabled = isRobotOn,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("robot_stop_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonRed,
                            disabledContainerColor = NavyCard
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = if (isRobotOn) Color.White else TextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STOP",
                            color = if (isRobotOn) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // 2. LIVE EXECUTED TRADES & LOGS TABS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real Trades & Execution Log",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Realized P/L: ${if (totalProfit >= 0) "+" else ""}$${String.format("%.2f", totalProfit)}",
                        color = if (totalProfit >= 0) EmeraldGreen else CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (activeTrades.isNotEmpty()) {
                    Text(
                        text = "Active Trades (${activeTrades.size})",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        activeTrades.take(2).forEach { trade ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NavyCard)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (trade.type == "BUY") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (trade.type == "BUY") EmeraldGreen else CrimsonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "#${trade.ticket} ${trade.type} ${trade.symbol} (${trade.lots})",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${if (trade.profit >= 0) "+" else ""}$${String.format("%.2f", trade.profit)}",
                                    color = if (trade.profit >= 0) EmeraldGreen else CrimsonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = "System Activity Logs",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No trading logs yet. Connect broker and VPS to start.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(logs) { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = timeFormatter.format(Date(log.timestamp)),
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.message,
                                    color = when (log.type) {
                                        LogType.SUCCESS -> EmeraldGreen
                                        LogType.ERROR -> CrimsonRed
                                        LogType.WARNING -> AmberWarning
                                        LogType.TRADE -> NeonCyan
                                        LogType.INFO -> TextPrimary
                                    },
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
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
fun ConditionPill(label: String, isMet: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isMet) EmeraldGreen.copy(alpha = 0.15f) else NavyCard)
            .border(1.dp, if (isMet) EmeraldGreen.copy(alpha = 0.5f) else NavyCardBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isMet) EmeraldGreen else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isMet) EmeraldGreen else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun rememberDateFormatter(): SimpleDateFormat {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault())
}
