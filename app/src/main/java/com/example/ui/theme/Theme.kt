package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightSmartWalletColors = lightColorScheme(
    primary = AppPalette.LightPrimary,
    onPrimary = AppPalette.LightOnPrimary,
    primaryContainer = AppPalette.LightPrimaryContainer,
    onPrimaryContainer = AppPalette.LightOnPrimaryContainer,
    secondary = AppPalette.LightSecondary,
    onSecondary = AppPalette.LightOnSecondary,
    background = AppPalette.LightBackground,
    onBackground = AppPalette.LightTextPrimary,
    surface = AppPalette.LightSurface,
    onSurface = AppPalette.LightTextPrimary,
    surfaceVariant = AppPalette.LightSurfaceVariant,
    onSurfaceVariant = AppPalette.LightTextSecondary,
    outline = AppPalette.LightBorder,
    error = AppPalette.Error,
    onError = AppPalette.OnError
)

private val DarkSmartWalletColors = darkColorScheme(
    primary = AppPalette.DarkPrimary,
    onPrimary = AppPalette.DarkOnPrimary,
    primaryContainer = AppPalette.DarkPrimaryContainer,
    onPrimaryContainer = AppPalette.DarkOnPrimaryContainer,
    secondary = AppPalette.DarkSecondary,
    onSecondary = AppPalette.DarkOnSecondary,
    background = AppPalette.DarkBackground,
    onBackground = AppPalette.DarkTextPrimary,
    surface = AppPalette.DarkSurface,
    onSurface = AppPalette.DarkTextPrimary,
    surfaceVariant = AppPalette.DarkSurfaceVariant,
    onSurfaceVariant = AppPalette.DarkTextSecondary,
    outline = AppPalette.DarkBorder,
    error = AppPalette.Error,
    onError = AppPalette.OnError
)

@Composable
fun SmartWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkSmartWalletColors else LightSmartWalletColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
