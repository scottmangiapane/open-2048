package com.scottmangiapane.open2048.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile

@Composable
fun BoardContainer(state: GameState, currentTheme: AppTheme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            GameBoard(board = state.board, currentTheme = currentTheme)
        }

        if (state.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.timeLeftMs == 0L) "Time's Up!" else "Game Over!",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun GameBoard(board: List<List<Tile?>>, currentTheme: AppTheme) {
    var boardWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val size = board.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { boardWidthPx = it.size.width }
    ) {
        if (boardWidthPx > 0 && size > 0) {
            val spacingDp = if (size > 4) 8.dp else 12.dp
            val spacingPx = with(density) { spacingDp.toPx() }
            val tileSizePx = (boardWidthPx - (spacingPx * (size - 1))) / size.toFloat()
            val tileSizeDp = with(density) { tileSizePx.toDp() }

            Column(verticalArrangement = Arrangement.spacedBy(spacingDp)) {
                repeat(size) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacingDp)) {
                        repeat(size) {
                            Box(
                                modifier = Modifier
                                    .size(tileSizeDp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }

            val activeTiles = remember(board) {
                board.asSequence()
                    .flatMapIndexed { r, row ->
                        row.mapIndexedNotNull { c, tile -> tile?.let { Triple(it, r, c) } }
                    }
                    .sortedBy { it.first.id }
                    .toList()
            }

            activeTiles.forEach { (tile, r, c) ->
                key(tile.id) {
                    val targetXPx = (tileSizePx + spacingPx) * c
                    val targetYPx = (tileSizePx + spacingPx) * r
                    
                    val animXPx by animateFloatAsState(
                        targetValue = targetXPx,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "glideX"
                    )
                    val animYPx by animateFloatAsState(
                        targetValue = targetYPx,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "glideY"
                    )

                    Box(
                        modifier = Modifier
                            .size(tileSizeDp)
                            .graphicsLayer {
                                translationX = animXPx
                                translationY = animYPx
                            }
                    ) {
                        TileView(tile = tile, tileSize = tileSizeDp, currentTheme = currentTheme)
                    }
                }
            }
        }
    }
}
