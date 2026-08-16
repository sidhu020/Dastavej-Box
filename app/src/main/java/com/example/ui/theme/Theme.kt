package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OrangeWhiteColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = OrangeOnPrimary,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OrangeOnContainer,
    secondary = OrangeSecondary,
    onSecondary = OrangeOnSecondary,
    secondaryContainer = OrangeContainer,
    onSecondaryContainer = OrangeOnContainer,
    background = WhiteBackground,
    onBackground = TextPrimary,
    surface = WhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = OrangeSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = OrangeBorder,
    outlineVariant = TextOutline,
    error = StatusExpiredRed,
    onError = Color.White
)

@Composable
fun DastavejBoxTheme(
    darkTheme: Boolean = false, // Explicitly no dark theme as requested
    content: @Composable () -> Unit
) {
    // Always use the vibrant Orange and White light color scheme
    MaterialTheme(
        colorScheme = OrangeWhiteColorScheme,
        typography = Typography,
        content = content
    )
}
