package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CustomColorScheme = darkColorScheme(
    primary = ColorGreen,
    onPrimary = TextPrimary,
    primaryContainer = ColorGreen.copy(alpha = 0.2f),
    onPrimaryContainer = TextPrimary,
    secondary = ColorPurple,
    onSecondary = TextPrimary,
    secondaryContainer = ColorPurple.copy(alpha = 0.2f),
    onSecondaryContainer = TextPrimary,
    background = BgMain,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgCardDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = BorderActive,
    error = ColorOffline,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to respect the user's custom color palette
    content: @Composable () -> Unit
) {
    // We always use the custom developer console theme to honor the requested design tokens
    val colorScheme = CustomColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
