package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingRobotViewModel

@Composable
fun HomeScreen(
    viewModel: TradingRobotViewModel,
    onNavigateToPage: (Int) -> Unit
) {
    val isRobotOn by viewModel.isRobotOn.collectAsState()
    val brokerConfig by viewModel.brokerConfig.collectAsState()
    val vpsConfig by viewModel.vpsConfig.collectAsState()
    val symbols by viewModel.symbols.collectAsState()
    val lotSize by viewModel.lotSize.collectAsState()
    val activeTrades by viewModel.activeTrades.collectAsState()

    val enabledSymbols = symbols.filter { it.isEnabled }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title & Status Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUTOMATIC TRADING ROBOT",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Home Dashboard",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Quick Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isRobotOn) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        if (isRobotOn) EmeraldGreen else CrimsonRed,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isRobotOn) EmeraldGreen else CrimsonRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRobotOn) "ROBOT: ON" else "ROBOT: OFF",
                        color = if (isRobotOn) EmeraldGreen else CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 1. MAIN ROBOT STATUS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("robot_status_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRobotOn) EmeraldGreen.copy(alpha = 0.6f) else NavyCardBorder
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isRobotOn) EmeraldGreen.copy(alpha = 0.2f) else NavyCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRobotOn) Icons.Default.PlayArrow else Icons.Default.Stop,
                                contentDescription = null,
                                tint = if (isRobotOn) EmeraldGreen else CrimsonRed,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Robot Master Status",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isRobotOn) "ACTIVE & TRADING" else "STOPPED / STANDBY",
                                color = if (isRobotOn) EmeraldGreen else CrimsonRed,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Switch(
                        checked = isRobotOn,
                        onCheckedChange = { viewModel.toggleRobot(it) },
                        modifier = Modifier.testTag("robot_master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGreen,
                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = NavyCard
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Active Lot Size: ${String.format("%.2f", lotSize)}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Open Positions: ${activeTrades.size}",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.startRobot() },
                        enabled = !isRobotOn,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_start_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            disabledContainerColor = NavyCard
                        )
                    ) {
                        Text("Start Robot", color = if (!isRobotOn) Color.Black else TextSecondary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.stopRobot() },
                        enabled = isRobotOn,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_stop_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonRed,
                            disabledContainerColor = NavyCard
                        )
                    ) {
                        Text("Stop Robot", color = if (isRobotOn) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. BROKER CONNECTION STATUS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPage(1) }
                .testTag("broker_status_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Broker Connection",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (brokerConfig.isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (brokerConfig.isConnected) EmeraldGreen else CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (brokerConfig.isConnected) "Connected (${brokerConfig.pingMs}ms)" else "Disconnected",
                            color = if (brokerConfig.isConnected) EmeraldGreen else CrimsonRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (brokerConfig.isConnected) {
                    Text(
                        text = "Account: ${brokerConfig.brokerType.title} • Login: ${brokerConfig.login}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Balance: $${String.format("%.2f", brokerConfig.balance)} USD • Lev: ${brokerConfig.leverage}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "MT4, MT5, and Exness account integration required to place trades.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 3. VPS CONNECTION STATUS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPage(2) }
                .testTag("vps_status_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VPS Connection",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (vpsConfig.isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (vpsConfig.isConnected) EmeraldGreen else CrimsonRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (vpsConfig.isConnected) "VPS Active (${vpsConfig.pingMs}ms)" else "VPS Offline",
                            color = if (vpsConfig.isConnected) EmeraldGreen else CrimsonRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (vpsConfig.isConnected) {
                    Text(
                        text = "Host: ${vpsConfig.ipAddress}:${vpsConfig.port} • ${vpsConfig.osInfo}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "MetaEditor Environment: ${if (vpsConfig.metaEditorReady) "Ready" else "Pending"}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Connect a compatible VPS to execute automated trades and run robot EAs 24/7.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 4. SELECTED TRADING SYMBOLS OVERVIEW CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPage(4) }
                .testTag("symbols_overview_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selected Trading Symbols",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${enabledSymbols.size} of ${symbols.size} Active",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chips for the 7 symbols
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    symbols.chunked(2).forEach { rowSymbols ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSymbols.forEach { sym ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sym.isEnabled) NavyCard else NavyCard.copy(alpha = 0.4f))
                                        .border(
                                            1.dp,
                                            if (sym.isEnabled) EmeraldGreen.copy(alpha = 0.5f) else NavyCardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = sym.displayName,
                                                color = if (sym.isEnabled) TextPrimary else TextSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = sym.code,
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Text(
                                            text = if (sym.isEnabled) "ON" else "OFF",
                                            color = if (sym.isEnabled) EmeraldGreen else CrimsonRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            if (rowSymbols.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
