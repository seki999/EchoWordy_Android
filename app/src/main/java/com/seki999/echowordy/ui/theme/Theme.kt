package com.seki999.echowordy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = EchoBlue40,
    onPrimary = EchoSurfaceLight,
    primaryContainer = EchoBlueContainer,
    onPrimaryContainer = EchoBlue40,
    secondary = EchoTeal40,
    onSecondary = EchoSurfaceLight,
    tertiary = EchoAmber40,
    onTertiary = EchoSurfaceLight,
    error = EchoError40,
    background = EchoBackgroundLight,
    onBackground = EchoTextLight,
    surface = EchoSurfaceLight,
    onSurface = EchoTextLight,
    surfaceVariant = EchoSurfaceVariantLight,
    onSurfaceVariant = EchoTextMutedLight,
)

private val DarkColors = darkColorScheme(
    primary = EchoBlue80,
    onPrimary = EchoBackgroundDark,
    primaryContainer = EchoBlue40,
    onPrimaryContainer = EchoBlueContainer,
    secondary = EchoTeal80,
    onSecondary = EchoBackgroundDark,
    tertiary = EchoAmber80,
    onTertiary = EchoBackgroundDark,
    error = EchoError80,
    background = EchoBackgroundDark,
    onBackground = EchoTextDark,
    surface = EchoSurfaceDark,
    onSurface = EchoTextDark,
    surfaceVariant = EchoSurfaceVariantDark,
    onSurfaceVariant = EchoTextMutedDark,
)

@Composable
fun EchoWordyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = EchoWordyTypography,
        content = content,
    )
}
