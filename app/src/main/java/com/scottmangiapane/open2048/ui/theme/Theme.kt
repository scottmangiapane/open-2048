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
            primaryContainer = colorResource(R.color.violet_600),
            onPrimaryContainer = Color.White,
            secondary = colorResource(R.color.rose_500),
            onSecondary = Color.White,
            secondaryContainer = colorResource(R.color.rose_500),
            onSecondaryContainer = Color.White,
            tertiary = colorResource(R.color.cyan_600),
            onTertiary = Color.White,
            tertiaryContainer = colorResource(R.color.cyan_600),
            onTertiaryContainer = Color.White,
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
            primaryContainer = colorResource(R.color.emerald_500),
            onPrimaryContainer = Color.White,
            secondary = colorResource(R.color.sky_600),
            onSecondary = Color.White,
            secondaryContainer = colorResource(R.color.sky_600),
            onSecondaryContainer = Color.White,
            tertiary = colorResource(R.color.teal_700),
            onTertiary = Color.White,
            tertiaryContainer = colorResource(R.color.teal_700),
            onTertiaryContainer = Color.White,
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
            primaryContainer = colorResource(R.color.classic_button_brown),
            onPrimaryContainer = colorResource(R.color.classic_text_light),
            secondary = colorResource(R.color.classic_resume_blue),
            onSecondary = Color.White,
            secondaryContainer = colorResource(R.color.classic_resume_blue),
            onSecondaryContainer = Color.White,
            tertiary = colorResource(R.color.tile_classic_32),
            onTertiary = Color.White,
            tertiaryContainer = colorResource(R.color.tile_classic_32),
            onTertiaryContainer = Color.White,
            background = colorResource(R.color.classic_bg),
            surface = colorResource(R.color.classic_empty),
            onBackground = colorResource(R.color.classic_text_dark),
            onSurface = colorResource(R.color.classic_text_dark),
            surfaceVariant = colorResource(R.color.classic_board),
            onSurfaceVariant = colorResource(R.color.classic_text_light)
        )
        AppTheme.OLED -> darkColorScheme(
            primary = colorResource(R.color.hc_primary),
            onPrimary = Color.Black,
            primaryContainer = Color.Black,
            onPrimaryContainer = colorResource(R.color.hc_primary),
            secondary = colorResource(R.color.hc_secondary),
            onSecondary = Color.Black,
            secondaryContainer = Color.Black,
            onSecondaryContainer = colorResource(R.color.hc_secondary),
            tertiary = colorResource(R.color.royal_blue_500),
            onTertiary = Color.White,
            tertiaryContainer = Color.Black,
            onTertiaryContainer = colorResource(R.color.royal_blue_500),
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
