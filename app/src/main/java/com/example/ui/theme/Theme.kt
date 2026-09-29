package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PmuGold,
    onPrimary = PmuDarkGreen,
    primaryContainer = PmuGreenSecondary,
    onPrimaryContainer = PmuGoldLight,
    secondary = PmuGoldLight,
    onSecondary = PmuDarkGreen,
    secondaryContainer = DarkTurfSurfaceVariant,
    onSecondaryContainer = PmuGoldContainer,
    tertiary = PmuGoldLight,
    background = DarkTurfBackground,
    onBackground = DarkTurfOnSurface,
    surface = DarkTurfSurface,
    onSurface = DarkTurfOnSurface,
    surfaceVariant = DarkTurfSurfaceVariant,
    onSurfaceVariant = DarkTurfOnSurfaceVariant,
    outline = DarkTurfOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = PmuGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = PmuGreenContainer,
    onPrimaryContainer = PmuOnGreenContainer,
    secondary = PmuGoldDark,
    onSecondary = Color.White,
    secondaryContainer = PmuGoldContainer,
    onSecondaryContainer = PmuOnGoldContainer,
    tertiary = PmuGold,
    background = TurfBackground,
    onBackground = TurfOnSurface,
    surface = TurfSurface,
    onSurface = TurfOnSurface,
    surfaceVariant = TurfSurfaceVariant,
    onSurfaceVariant = TurfOnSurfaceVariant,
    outline = TurfOutline,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // We enforce the custom PMU Dark Green & Gold brand theme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
