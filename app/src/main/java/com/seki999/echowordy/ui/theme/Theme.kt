package com.seki999.echowordy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.seki999.echowordy.domain.model.ReadingPreferences
import com.seki999.echowordy.domain.model.ReadingTheme

val LocalReadingPreferences = staticCompositionLocalOf { ReadingPreferences() }
private val SoftColors = lightColorScheme(
    primary = ReadingAccent, onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E8EB), onPrimaryContainer = ReadingAccent,
    secondary = ReadingTextSecondary, background = ReadingBackground,
    onBackground = ReadingTextPrimary, surface = ReadingSurface,
    onSurface = ReadingTextPrimary, onSurfaceVariant = ReadingTextSecondary,
    surfaceVariant = Color(0xFFEAEAE3), outline = Color(0xFF7A8389),
    outlineVariant = ReadingDivider, error = Color(0xFFA52A2A),
)
private val WarmColors = SoftColors.copy(
    primary = WarmAccent, background = WarmBackground, surface = WarmSurface,
    onBackground = WarmTextPrimary, onSurface = WarmTextPrimary,
    onSurfaceVariant = WarmTextSecondary, secondary = WarmTextSecondary,
    surfaceVariant = Color(0xFFE5DDC9),
)
private val DarkColors = darkColorScheme(
    primary = DarkAccent, onPrimary = DarkBackground,
    primaryContainer = Color(0xFF344755), onPrimaryContainer = DarkTextPrimary,
    background = DarkBackground, surface = DarkSurface,
    onBackground = DarkTextPrimary, onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary, secondary = DarkTextSecondary,
    surfaceVariant = Color(0xFF343B40), outline = Color(0xFF929BA1),
    outlineVariant = Color(0xFF495157), error = Color(0xFFE9A3A0),
)

@Composable
fun EchoWordyTheme(preferences: ReadingPreferences = ReadingPreferences(), content: @Composable () -> Unit) {
    val colors = when (preferences.theme) {
        ReadingTheme.SOFT -> SoftColors
        ReadingTheme.WARM -> WarmColors
        ReadingTheme.DARK -> DarkColors
    }
    CompositionLocalProvider(LocalReadingPreferences provides preferences) {
        MaterialTheme(colorScheme = colors, typography = EchoWordyTypography, content = content)
    }
}
