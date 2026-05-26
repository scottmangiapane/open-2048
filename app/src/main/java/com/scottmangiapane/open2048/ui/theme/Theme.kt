package com.scottmangiapane.open2048.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8F7A66),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF181714),
    surface = Color(0xFF242320),
    onBackground = Color(0xFFF9F6F2),
    onSurface = Color(0xFFF9F6F2),
    surfaceVariant = Color(0xFF3C3A32),
    onSurfaceVariant = Color(0xFF4E4B42)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8F7A66),
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFAF8EF),
    surface = Color(0xFFEEE4DA),
    onBackground = Color(0xFF776E65),
    onSurface = Color(0xFF776E65),
    surfaceVariant = Color(0xFFBBADA0),
    onSurfaceVariant = Color(0xFFCDC1B4)
)

@Composable
fun Open2048Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Default to false to keep classic look
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
