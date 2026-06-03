package com.scottmangiapane.open2048.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.AppTheme

@Composable
fun Open2048Theme(
    theme: AppTheme = AppTheme.LIGHT,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        AppTheme.DARK -> darkColorScheme(
            primary = colorResource(R.color.violet_600),
            onPrimary = Color.White,
            secondary = colorResource(R.color.rose_500),
            onSecondary = Color.White,
            tertiary = colorResource(R.color.tile_modern_2048),
            background = colorResource(R.color.slate_950),
            surface = colorResource(R.color.slate_800),
            onBackground = colorResource(R.color.slate_100),
            onSurface = colorResource(R.color.slate_100),
            surfaceVariant = colorResource(R.color.slate_900),
            onSurfaceVariant = colorResource(R.color.slate_400),
        )
        AppTheme.LIGHT -> lightColorScheme(
            primary = colorResource(R.color.emerald_500),
            onPrimary = Color.White,
            secondary = colorResource(R.color.royal_blue_500),
            onSecondary = Color.White,
            tertiary = colorResource(R.color.tile_modern_2048),
            background = colorResource(R.color.slate_50),
            surface = colorResource(R.color.slate_100),
            onBackground = colorResource(R.color.slate_900),
            onSurface = colorResource(R.color.slate_900),
            surfaceVariant = colorResource(R.color.slate_200),
            onSurfaceVariant = colorResource(R.color.slate_600)
        )
        AppTheme.CLASSIC -> lightColorScheme(
            primary = colorResource(R.color.classic_button_brown),
            onPrimary = colorResource(R.color.classic_text_light),
            secondary = colorResource(R.color.classic_resume_blue),
            onSecondary = Color.White,
            tertiary = colorResource(R.color.tile_classic_8),
            background = colorResource(R.color.classic_bg),
            surface = colorResource(R.color.classic_empty),
            onBackground = colorResource(R.color.classic_text_dark),
            onSurface = colorResource(R.color.classic_text_dark),
            surfaceVariant = colorResource(R.color.classic_board),
            onSurfaceVariant = colorResource(R.color.classic_text_light)
        )
        AppTheme.RETRO -> darkColorScheme(
            primary = colorResource(R.color.retro_primary),
            onPrimary = Color.Black,
            secondary = colorResource(R.color.retro_secondary),
            onSecondary = Color.Black,
            tertiary = colorResource(R.color.retro_tertiary),
            background = colorResource(R.color.retro_bg),
            surface = colorResource(R.color.retro_empty),
            onBackground = colorResource(R.color.retro_text),
            onSurface = colorResource(R.color.retro_text),
            surfaceVariant = colorResource(R.color.retro_board),
            onSurfaceVariant = colorResource(R.color.retro_text)
        )
        AppTheme.OLED -> darkColorScheme(
            primary = colorResource(R.color.hc_primary),
            onPrimary = Color.Black,
            secondary = colorResource(R.color.hc_secondary),
            onSecondary = Color.Black,
            tertiary = Color.White,
            background = colorResource(R.color.hc_bg),
            surface = colorResource(R.color.hc_empty),
            onBackground = colorResource(R.color.hc_text),
            onSurface = colorResource(R.color.hc_text),
            surfaceVariant = colorResource(R.color.hc_board),
            onSurfaceVariant = colorResource(R.color.hc_text)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            val isAppearanceLight = when(theme) {
                AppTheme.LIGHT -> true
                AppTheme.DARK -> false
                AppTheme.CLASSIC -> true
                AppTheme.RETRO -> false
                AppTheme.OLED -> false
            }
            windowInsetsController.isAppearanceLightStatusBars = isAppearanceLight
            windowInsetsController.isAppearanceLightNavigationBars = isAppearanceLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
