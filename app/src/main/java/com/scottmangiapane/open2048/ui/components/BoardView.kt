package com.scottmangiapane.open2048.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            GameBoard(board = state.board, currentTheme = currentTheme)
        }

        if (state.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .clip(RoundedCornerShape(12.dp)),
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
    val size = board.size
    if (size == 0) return

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val boardWidth = this.maxWidth
        val spacingDp = if (size > 4) 8.dp else 12.dp
        val tileSize = (boardWidth - (spacingDp * (size - 1).toFloat())) / size.toFloat()

        // Render empty grid slots
        Column(verticalArrangement = Arrangement.spacedBy(spacingDp)) {
            repeat(size) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacingDp)) {
                    repeat(size) {
                        Box(
                            modifier = Modifier
                                .size(tileSize)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            }
        }

        // Render active tiles
        val activeTiles = remember(board) {
            board.flatMapIndexed { r, row ->
                row.mapIndexedNotNull { c, tile -> tile?.let { Triple(it, r, c) } }
            }.sortedBy { it.first.id }
        }

        activeTiles.forEach { (tile, r, c) ->
            key(tile.id) {
                val targetX = (tileSize + spacingDp) * c.toFloat()
                val targetY = (tileSize + spacingDp) * r.toFloat()
                
                val animX by animateDpAsState(
                    targetValue = targetX,
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
                    label = "glideX"
                )
                val animY by animateDpAsState(
                    targetValue = targetY,
                    animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
                    label = "glideY"
                )

                Box(
                    modifier = Modifier
                        .size(tileSize)
                        .offset(x = animX, y = animY)
                ) {
                    TileView(tile = tile, tileSize = tileSize, currentTheme = currentTheme)
                }
            }
        }
    }
}
