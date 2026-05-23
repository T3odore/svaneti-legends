package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GeorgianGold,
    secondary = GeorgianCrimson,
    tertiary = CaucasusSnow,
    background = SvanObsidian,
    surface = SvanCharcoal,
    onPrimary = SvanObsidian,
    onSecondary = Color.White,
    onTertiary = SvanObsidian,
    onBackground = CaucasusSnow,
    onSurface = GeorgianBeige
)

private val LightColorScheme = lightColorScheme(
    primary = GeorgianCrimson,
    secondary = GeorgianGold,
    tertiary = SvanTowerGrey,
    background = GeorgianBeige,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = SvanObsidian,
    onTertiary = Color.White,
    onBackground = SvanObsidian,
    onSurface = SvanCharcoal
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We will default to Dark Theme for that immersive, rich game atmosphere
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful Georgia cultural atmosphere
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
