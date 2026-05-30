package com.scottmangiapane.open2048.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.AnimationSpeed
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile

@Composable
fun BoardContainer(
    state: GameState,
    currentTheme: AppTheme,
    animationSpeed: AnimationSpeed,
    focusRequester: FocusRequester? = null,
    onMove: (Direction) -> Unit = {}
) {
    // Use a key based on the board/game state to ensure it resets or stays in sync
    var isPlayingMode by remember(state.board.isEmpty()) { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Track if this is the first focus gain for this board instance
    var hasBeenInitiallyFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) {
        if (isFocused) {
            // Only auto-enable play mode on the very first time the board is focused (screen launch)
            if (!hasBeenInitiallyFocused) {
                isPlayingMode = true
                hasBeenInitiallyFocused = true
            }
        } else {
            // When focus moves away, drop out of play mode
            isPlayingMode = false
        }
    }

    // On touch devices, isFocused will be false unless a keyboard/D-pad is used.
    // We only show the "Navigation Mode" visuals if we have focus but are NOT in playing mode.
    val showNavigationVisuals = isFocused && !isPlayingMode

    val borderColor by animateColorAsState(
        targetValue = if (showNavigationVisuals) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "borderColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (state.isGameOver) return@onKeyEvent false

                    if (keyEvent.key == Key.Back || keyEvent.key == Key.Escape) {
                        if (isPlayingMode) {
                            isPlayingMode = false
                            return@onKeyEvent true
                        }
                    }
                    
                    if (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter) {
                        if (!isPlayingMode) {
                            isPlayingMode = true
                            return@onKeyEvent true
                        }
                    }

                    if (isPlayingMode) {
                        val direction = when (keyEvent.key) {
                            Key.DirectionUp -> Direction.UP
                            Key.DirectionDown -> Direction.DOWN
                            Key.DirectionLeft -> Direction.LEFT
                            Key.DirectionRight -> Direction.RIGHT
                            else -> null
                        }
                        direction?.let {
                            onMove(it)
                            return@onKeyEvent true
                        }
                    }
                }
                false
            }
            .clickable(
                enabled = !state.isGameOver,
                interactionSource = interactionSource,
                indication = null
            ) {
                isPlayingMode = true
            }
            .focusable(enabled = !state.isGameOver, interactionSource = interactionSource)
            .appFocusBorder(
                isFocused = isFocused,
                color = borderColor
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            GameBoard(board = state.board, currentTheme = currentTheme, animationSpeed = animationSpeed)
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
private fun GameBoard(board: List<List<Tile?>>, currentTheme: AppTheme, animationSpeed: AnimationSpeed) {
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

        val stiffness = when (animationSpeed) {
            AnimationSpeed.NONE -> 1000000f // effectively instant
            AnimationSpeed.FAST -> Spring.StiffnessHigh
            AnimationSpeed.NORMAL -> Spring.StiffnessMedium
            AnimationSpeed.SLOW -> Spring.StiffnessLow
        }

        val animSpec = if (animationSpeed == AnimationSpeed.NONE) {
            snap<androidx.compose.ui.unit.Dp>()
        } else {
            spring(Spring.DampingRatioNoBouncy, stiffness)
        }

        activeTiles.forEach { (tile, r, c) ->
            key(tile.id) {
                val targetX = (tileSize + spacingDp) * c.toFloat()
                val targetY = (tileSize + spacingDp) * r.toFloat()
                
                val animX by animateDpAsState(
                    targetValue = targetX,
                    animationSpec = animSpec,
                    label = "glideX"
                )
                val animY by animateDpAsState(
                    targetValue = targetY,
                    animationSpec = animSpec,
                    label = "glideY"
                )

                Box(
                    modifier = Modifier
                        .size(tileSize)
                        .offset(x = animX, y = animY)
                ) {
                    TileView(
                        tile = tile,
                        tileSize = tileSize,
                        currentTheme = currentTheme,
                        animationSpeed = animationSpeed
                    )
                }
            }
        }
    }
}
