package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BrokerConnectionScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LotSizeSymbolsScreen
import com.example.ui.screens.RobotControlScreen
import com.example.ui.screens.VpsConnectionScreen
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingRobotViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TradingRobotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val index: Int,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

val APP_NAV_PAGES = listOf(
    NavItem(0, "Home", Icons.Default.Home, "nav_home"),
    NavItem(1, "Broker", Icons.Default.AccountBalance, "nav_broker"),
    NavItem(2, "VPS", Icons.Default.Computer, "nav_vps"),
    NavItem(3, "Robot", Icons.Default.PowerSettingsNew, "nav_robot"),
    NavItem(4, "Lot & Symbols", Icons.Default.Tune, "nav_symbols"),
    NavItem(5, "Files", Icons.Default.FolderOpen, "nav_files")
)

@Composable
fun MainAppScreen(viewModel: TradingRobotViewModel) {
    val currentPage by viewModel.currentPage.collectAsState()
    val alertMessage by viewModel.alertMessage.collectAsState()
    val isRobotOn by viewModel.isRobotOn.collectAsState()

    // Handle Back Press to return to Home dashboard if on sub-screens
    BackHandler(enabled = currentPage != 0) {
        viewModel.selectPage(0)
    }

    // Alert Dialog for notifications/warnings
    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearAlert() },
            title = {
                Text(
                    text = "System Notice",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = alertMessage ?: "",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearAlert() },
                    modifier = Modifier.testTag("alert_dismiss_button")
                ) {
                    Text("OK", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            TradingBottomNavigationBar(
                items = APP_NAV_PAGES,
                selectedIndex = currentPage,
                isRobotOn = isRobotOn,
                onSelect = { viewModel.selectPage(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Crossfade(targetState = currentPage, label = "PageTransition") { pageIndex ->
                when (pageIndex) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPage = { viewModel.selectPage(it) }
                    )
                    1 -> BrokerConnectionScreen(viewModel = viewModel)
                    2 -> VpsConnectionScreen(viewModel = viewModel)
                    3 -> RobotControlScreen(viewModel = viewModel)
                    4 -> LotSizeSymbolsScreen(viewModel = viewModel)
                    5 -> FileManagerScreen(viewModel = viewModel)
                    else -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToPage = { viewModel.selectPage(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TradingBottomNavigationBar(
    items: List<NavItem>,
    selectedIndex: Int,
    isRobotOn: Boolean,
    onSelect: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = NavySurface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedIndex == item.index

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NavyCard else androidx.compose.ui.graphics.Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(item.index) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) NeonCyan else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            if (item.index == 3 && isRobotOn) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            color = if (isSelected) NeonCyan else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
