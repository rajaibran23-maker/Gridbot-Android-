package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TradingColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = NavyDark,
    primaryContainer = NavyCardBorder,
    onPrimaryContainer = NeonCyan,
    secondary = EmeraldGreen,
    onSecondary = NavyDark,
    secondaryContainer = NavyCard,
    onSecondaryContainer = EmeraldGreen,
    tertiary = AmberWarning,
    onTertiary = NavyDark,
    background = NavyDark,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    outline = NavyCardBorder,
    error = CrimsonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TradingColorScheme,
        typography = Typography,
        content = content
    )
}
