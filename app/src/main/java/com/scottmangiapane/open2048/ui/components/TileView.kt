package com.scottmangiapane.open2048.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.Tile
import kotlinx.coroutines.delay

@Composable
fun TileView(tile: Tile, tileSize: Dp, currentTheme: AppTheme) {
    val displayValue = remember(tile.id) { mutableIntStateOf(tile.value) }
    val isVisible = remember(tile.id) { mutableStateOf(!tile.isNew) }
    val backgroundColor = getTileBackgroundColor(displayValue.intValue, currentTheme)
    val textColor = getTileTextColor(displayValue.intValue, currentTheme)
    val scale = remember { Animatable(if (tile.isNew) 0f else 1f) }

    LaunchedEffect(tile.id, tile.value, tile.isNew) {
        if (tile.isNew && !isVisible.value) {
            delay(100) // Wait for other tiles to slide
            isVisible.value = true
            displayValue.intValue = tile.value
            scale.animateTo(1f, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
        } else {
            isVisible.value = true
            if (displayValue.intValue != tile.value) {
                delay(100) // Wait for slide to complete before changing color/popping
                displayValue.intValue = tile.value
                scale.snapTo(0.85f)
                scale.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium))
            } else {
                scale.snapTo(1f)
            }
        }
    }

    if (isVisible.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .shadow(2.dp, RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor)
                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            val fontSize = when {
                displayValue.intValue >= 1024 -> tileSize.value * 0.28f
                displayValue.intValue >= 100 -> tileSize.value * 0.35f
                else -> tileSize.value * 0.45f
            }
            Text(
                text = displayValue.intValue.toString(),
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun getTileBackgroundColor(value: Int, theme: AppTheme): Color {
    val isDark = theme == AppTheme.DARK
    if (theme == AppTheme.CLASSIC) {
        return colorResource(
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
            }
        )
    }
    
    return if (isDark) {
        colorResource(
            when (value) {
                2 -> R.color.slate_600
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
                else -> R.color.slate_900
            }
        )
    } else {
        colorResource(
            when (value) {
                2 -> R.color.white
                4 -> R.color.slate_50
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
            }
        )
    }
}

@Composable
private fun getTileTextColor(value: Int, theme: AppTheme): Color {
    if (theme == AppTheme.CLASSIC) {
        return colorResource(
            when (value) {
                2, 4 -> R.color.classic_text_dark
                else -> R.color.white
            }
        )
    }
    val isDark = theme == AppTheme.DARK
    return if (isDark) {
        when (value) {
            2, 4 -> Color.White.copy(alpha = 0.9f)
            else -> Color.White
        }
    } else {
        colorResource(
            when (value) {
                2, 4 -> R.color.classic_text_dark
                else -> R.color.white
            }
        )
    }
}
