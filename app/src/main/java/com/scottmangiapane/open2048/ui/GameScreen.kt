package com.scottmangiapane.open2048.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.Tile
import kotlin.math.abs

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8EF))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        HeaderSection(
            score = state.score,
            bestScore = state.bestScore,
            onRestart = { viewModel.restartGame() }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFBBADA0))
                .padding(8.dp)
                .pointerInput(Unit) {
                    var totalDragX = 0f
                    var totalDragY = 0f
                    detectDragGestures(
                        onDragEnd = {
                            val direction = when {
                                abs(totalDragX) > abs(totalDragY) -> {
                                    if (totalDragX > 50) Direction.RIGHT else if (totalDragX < -50) Direction.LEFT else null
                                }
                                else -> {
                                    if (totalDragY > 50) Direction.DOWN else if (totalDragY < -50) Direction.UP else null
                                }
                            }
                            direction?.let { viewModel.move(it) }
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        }
                    )
                }
        ) {
            GameBoard(board = state.board)
            
            if (state.isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88FFFFFF))
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Game Over!",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF776E65)
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(score: Int, bestScore: Int, onRestart: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "2048",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF776E65)
        )
        
        Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreCard(label = "SCORE", score = score)
                ScoreCard(label = "BEST", score = bestScore)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F7A66)),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("New Game", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ScoreCard(label: String, score: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFBBADA0))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFFEEE4DA), fontWeight = FontWeight.Bold)
        Text(text = score.toString(), fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GameBoard(board: List<List<Tile?>>) {
    var boardSize by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                boardSize = with(density) { it.size.width.toDp() }
            }
    ) {
        val tileSize = (maxWidth - 24.dp) / 4 // 8dp padding * 3 gaps = 24dp
        val spacing = 8.dp

        // Background Grid
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            repeat(4) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .size(tileSize)
                                .background(Color(0xFFCDC1B4), RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        // Active Tiles
        board.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, tile ->
                if (tile != null) {
                    val animatedX by animateDpAsState(
                        targetValue = colIndex.toDp(tileSize, spacing),
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "tileX"
                    )
                    val animatedY by animateDpAsState(
                        targetValue = rowIndex.toDp(tileSize, spacing),
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "tileY"
                    )

                    key(tile.id) {
                        Box(
                            modifier = Modifier
                                .offset(x = animatedX, y = animatedY)
                                .size(tileSize)
                        ) {
                            TileView(tile = tile)
                        }
                    }
                }
            }
        }
    }
}

private fun Int.toDp(tileSize: Dp, spacing: Dp): Dp {
    return (this.toFloat().dp * tileSize.value) + (this.toFloat().dp * spacing.value)
}

@Composable
fun TileView(tile: Tile) {
    val backgroundColor = when (tile.value) {
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFFEDC850)
        1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFFEDC22E)
        else -> Color(0xFF3C3A32)
    }
    
    val textColor = when (tile.value) {
        2, 4 -> Color(0xFF776E65)
        else -> Color.White
    }

    // Scale animation for new/merged tiles
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tileScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            fontSize = if (tile.value >= 1024) 24.sp else 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
