package com.devcore.ticketflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = PrimaryNavyLight,
    onPrimaryContainer = Color.White,
    secondary = StatusInfoBlue,
    onSecondary = Color.White,
    background = BgScreen,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = CardStroke,
    error = StatusCritical,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7099C2),
    onPrimary = PrimaryNavyDark,
    primaryContainer = PrimaryNavy,
    onPrimaryContainer = Color(0xFFD6E4F0),
    secondary = Color(0xFF64B5F6),
    onSecondary = PrimaryNavyDark,
    background = BgScreenDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardStrokeDark,
    error = Color(0xFFEF5350),
    onError = Color.White
)

@Composable
fun TicketFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TicketFlowTypography,
        content = content
    )
}
