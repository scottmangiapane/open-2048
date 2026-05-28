package com.scottmangiapane.open2048.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scottmangiapane.open2048.model.AnimationSpeed
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.Tile
import com.scottmangiapane.open2048.ui.theme.TileColors
import kotlinx.coroutines.delay

@Composable
fun TileView(tile: Tile, tileSize: Dp, currentTheme: AppTheme, animationSpeed: AnimationSpeed) {
    val displayValue = remember(tile.id) { mutableIntStateOf(tile.value) }
    val isVisible = remember(tile.id) { mutableStateOf(!tile.isNew) }
    val backgroundColor = TileColors.getBackgroundColor(displayValue.intValue, currentTheme)
    val textColor = TileColors.getTextColor(displayValue.intValue, currentTheme)
    val scale = remember { Animatable(if (tile.isNew) 0f else 1f) }

    val stiffness = when (animationSpeed) {
        AnimationSpeed.NONE -> 1000000f
        AnimationSpeed.FAST -> Spring.StiffnessHigh
        AnimationSpeed.NORMAL -> Spring.StiffnessMedium
        AnimationSpeed.SLOW -> Spring.StiffnessLow
    }

    LaunchedEffect(tile.id, tile.value, tile.isNew, animationSpeed) {
        if (tile.isNew && !isVisible.value) {
            if (animationSpeed != AnimationSpeed.NONE) delay(100)
            isVisible.value = true
            displayValue.intValue = tile.value
            if (animationSpeed == AnimationSpeed.NONE) {
                scale.snapTo(1f)
            } else {
                scale.animateTo(1f, spring(Spring.DampingRatioNoBouncy, stiffness))
            }
        } else {
            isVisible.value = true
            if (displayValue.intValue != tile.value) {
                if (animationSpeed != AnimationSpeed.NONE) {
                    delay(100)
                    displayValue.intValue = tile.value
                    scale.snapTo(0.85f)
                    scale.animateTo(1f, spring(Spring.DampingRatioHighBouncy, stiffness))
                } else {
                    displayValue.intValue = tile.value
                    scale.snapTo(1f)
                }
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
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            val factor = when {
                displayValue.intValue >= 1024 -> 0.28f
                displayValue.intValue >= 100 -> 0.35f
                else -> 0.45f
            }
            Text(
                text = displayValue.intValue.toString(),
                fontSize = (tileSize.value * factor).sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
