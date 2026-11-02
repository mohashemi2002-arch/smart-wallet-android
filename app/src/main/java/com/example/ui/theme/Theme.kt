package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SmartWalletLightColors = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.LightPrimarySoft,
    onPrimaryContainer = AppColors.LightTextPrimary,
    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    background = AppColors.LightBackground,
    onBackground = AppColors.LightTextPrimary,
    surface = AppColors.LightSurface,
    onSurface = AppColors.LightTextPrimary,
    surfaceVariant = AppColors.LightSurfaceVariant,
    onSurfaceVariant = AppColors.LightTextSecondary,
    outline = AppColors.LightBorder,
    error = AppColors.Error,
    onError = Color.White
)

private val SmartWalletDarkColors = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.DarkPrimarySoft,
    onPrimaryContainer = AppColors.DarkTextPrimary,
    secondary = AppColors.Secondary,
    onSecondary = Color.White,
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkTextPrimary,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkTextPrimary,
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = AppColors.DarkTextSecondary,
    outline = AppColors.DarkBorder,
    error = AppColors.Error,
    onError = Color.White
)

@Composable
fun SmartWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) SmartWalletDarkColors else SmartWalletLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
