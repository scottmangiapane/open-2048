package com.scottmangiapane.open2048.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.AppTheme

object TileColors {
    @Composable
    fun getBackgroundColor(value: Int, theme: AppTheme): Color {
        return when (theme) {
            AppTheme.CLASSIC -> colorResource(
                when (value) {
                    2 -> R.color.tile_classic_2
                    4 -> R.color.tile_classic_4
                    8 -> R.color.tile_classic_8
                    16 -> R.color.tile_classic_16
                    32 -> R.color.tile_classic_32
                    64 -> R.color.tile_classic_64
                    128 -> R.color.tile_classic_128
                    256 -> R.color.tile_classic_256
                    512 -> R.color.tile_classic_512
                    1024 -> R.color.tile_classic_1024
                    2048 -> R.color.tile_classic_2048
                    4096 -> R.color.tile_classic_4096
                    else -> R.color.tile_classic_super
                },
            )
            AppTheme.DARK -> colorResource(
                when (value) {
                    2 -> R.color.slate_700
                    4 -> R.color.slate_500
                    8 -> R.color.tile_modern_8
                    16 -> R.color.tile_modern_16
                    32 -> R.color.tile_modern_32
                    64 -> R.color.tile_modern_64
                    128 -> R.color.tile_modern_128
                    256 -> R.color.tile_modern_256
                    512 -> R.color.tile_modern_512
                    1024 -> R.color.tile_modern_1024
                    2048 -> R.color.tile_modern_2048
                    else -> R.color.slate_50
                },
            )
            AppTheme.RETRO -> colorResource(
                when (value) {
                    2 -> R.color.tile_retro_2
                    4 -> R.color.tile_retro_4
                    8 -> R.color.tile_retro_8
                    16 -> R.color.tile_retro_16
                    32 -> R.color.tile_retro_32
                    64 -> R.color.tile_retro_64
                    128 -> R.color.tile_retro_128
                    256 -> R.color.tile_retro_256
                    512 -> R.color.tile_retro_512
                    1024 -> R.color.tile_retro_1024
                    2048 -> R.color.tile_retro_2048
                    else -> R.color.white
                },
            )
            AppTheme.OLED -> colorResource(
                when (value) {
                    2 -> R.color.tile_hc_2
                    4 -> R.color.tile_hc_4
                    8 -> R.color.tile_hc_8
                    16 -> R.color.tile_hc_16
                    32 -> R.color.tile_hc_32
                    64 -> R.color.tile_hc_64
                    128 -> R.color.tile_hc_128
                    256 -> R.color.tile_hc_256
                    512 -> R.color.tile_hc_512
                    1024 -> R.color.tile_hc_1024
                    2048 -> R.color.tile_hc_2048
                    else -> R.color.amber_500
                },
            )
            AppTheme.LIGHT -> colorResource(
                when (value) {
                    2 -> R.color.white
                    4 -> R.color.emerald_100
                    8 -> R.color.tile_classic_8
                    16 -> R.color.tile_classic_16
                    32 -> R.color.tile_classic_32
                    64 -> R.color.tile_classic_64
                    128 -> R.color.tile_classic_128
                    256 -> R.color.tile_classic_256
                    512 -> R.color.tile_classic_512
                    1024 -> R.color.tile_classic_1024
                    2048 -> R.color.tile_classic_2048
                    else -> R.color.slate_900
                },
            )
        }
    }

    @Composable
    fun getTextColor(value: Int, theme: AppTheme): Color {
        return when (theme) {
            AppTheme.CLASSIC -> colorResource(if (value <= 4) R.color.classic_text_dark else R.color.classic_text_light)
            AppTheme.DARK -> when {
                value <= 4 -> Color.White.copy(alpha = 0.9f)
                value > 2048 -> colorResource(R.color.slate_900)
                else -> Color.White
            }
            AppTheme.RETRO -> when {
                value == 1024 || value > 2048 -> Color.Black
                else -> Color.White
            }
            AppTheme.OLED -> when {
                value >= 512 -> Color.Black
                else -> Color.White
            }
            AppTheme.LIGHT -> colorResource(if (value <= 4) R.color.classic_text_dark else R.color.classic_text_light)
        }
    }
}
