package com.scottmangiapane.open2048.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.unit.dp
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.AnimationSpeed
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.ControlMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import kotlin.math.abs

@Composable
fun BoardContainer(
    state: GameState,
    currentTheme: AppTheme,
    animationSpeed: AnimationSpeed,
    controlMode: ControlMode = ControlMode.BOTH,
    focusRequester: FocusRequester? = null,
    autoPlay: Boolean = false,
    hasTouch: Boolean = true,
    onMove: (Direction) -> Unit = {},
    onFocusGained: ((isPlaying: Boolean) -> Unit)? = null,
) {
    // Force a reset of isPlayingMode when the board is reset
    var isPlayingMode by remember(state.board.isEmpty()) { 
        mutableStateOf(autoPlay) 
    }

    val inputModeManager = LocalInputModeManager.current

    // Sync autoPlay -> isPlayingMode when it becomes true (e.g. forced focus)
    LaunchedEffect(autoPlay) {
        if (autoPlay) isPlayingMode = true
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val density = LocalDensity.current
    val swipeThreshold = remember(density) { with(density) { 56.dp.toPx() } }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            // If autoPlay is set, we default to playing mode on focus gain
            if (autoPlay) isPlayingMode = true
            onFocusGained?.invoke(isPlayingMode)
        } else {
            // When focus moves away, drop out of play mode
            isPlayingMode = false
        }
    }

    // Navigation mode visuals are for Keyboard/D-pad users to see they are "selecting" the board 
    // but not yet "interacting" with it.
    val showNavigationVisuals = isFocused && !isPlayingMode

    val borderColor by animateColorAsState(
        targetValue = if (showNavigationVisuals) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "borderColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onKeyEvent { keyEvent ->
                if ((keyEvent.type != KeyEventType.KeyDown) || state.isGameOver) return@onKeyEvent false

                when (keyEvent.key) {
                    Key.Back, Key.Escape -> {
                        // We swallow Back/Escape only if we are in "playing mode" AND
                        // the device is either non-touch OR we are actively in Keyboard mode.
                        if (isPlayingMode && (!hasTouch || inputModeManager.inputMode == InputMode.Keyboard)) {
                            isPlayingMode = false
                            return@onKeyEvent true
                        }
                    }
                    Key.DirectionCenter, Key.Enter -> {
                        if (!isPlayingMode) {
                            isPlayingMode = true
                            return@onKeyEvent true
                        }
                    }
                    Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight -> {
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
                }
                false
            }
            .then(
                if (controlMode != ControlMode.ARROWS && !state.isGameOver) {
                    Modifier.pointerInput(Unit) {
                        var totalDragX = 0f
                        var totalDragY = 0f
                        detectDragGestures(
                            onDragEnd = {
                                val direction = when {
                                    (abs(totalDragX) > abs(totalDragY)) && (abs(totalDragX) > swipeThreshold) -> {
                                        if (totalDragX > 0) Direction.RIGHT else Direction.LEFT
                                    }
                                    abs(totalDragY) > swipeThreshold -> {
                                        if (totalDragY > 0) Direction.DOWN else Direction.UP
                                    }
                                    else -> null
                                }
                                direction?.let { onMove(it) }
                                totalDragX = 0f
                                totalDragY = 0f
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        }
                    }
                } else Modifier
            )
            .clickable(
                enabled = !state.isGameOver,
                interactionSource = interactionSource,
                indication = null
            ) {
                isPlayingMode = true
                focusRequester?.requestFocus()
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
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.timeLeftMs == 0L) "Time's Up!" else "Game Over!",
                    style = MaterialTheme.typography.displayMedium,
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
