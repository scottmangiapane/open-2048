package com.scottmangiapane.open2048.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState as animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 56.dp.toPx() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val direction = when (keyEvent.key) {
                        Key.DirectionUp -> Direction.UP
                        Key.DirectionDown -> Direction.DOWN
                        Key.DirectionLeft -> Direction.LEFT
                        Key.DirectionRight -> Direction.RIGHT
                        else -> null
                    }
                    direction?.let {
                        viewModel.move(it)
                        return@onKeyEvent true
                    }
                }
                false
            }
            .pointerInput(Unit) {
                var totalDragX = 0f
                var totalDragY = 0f
                detectDragGestures(
                    onDragEnd = {
                        val direction = when {
                            abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > swipeThreshold -> {
                                if (totalDragX > 0) Direction.RIGHT else Direction.LEFT
                            }
                            abs(totalDragY) > swipeThreshold -> {
                                if (totalDragY > 0) Direction.DOWN else Direction.UP
                            }
                            else -> null
                        }
                        direction?.let { viewModel.move(it) }
                        totalDragX = 0f
                        totalDragY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    totalDragX += dragAmount.x
                    totalDragY += dragAmount.y
                }
            }
    ) {
        val isLandscape = maxWidth > maxHeight
        val minDimension = minOf(maxWidth, maxHeight)
        val isDarkMode = state.isDarkMode ?: isSystemInDarkTheme()

        // Theme Toggle in top-right
        IconButton(
            onClick = { viewModel.toggleDarkMode() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Dark Mode",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        val (hPadding, vPadding) = when {
            minDimension >= 840.dp -> 172.dp to 144.dp // Large Tablets
            minDimension >= 600.dp -> 120.dp to 96.dp  // Foldables
            isLandscape -> 0.dp to 8.dp                // Landscape phones: minimal padding
            else -> 32.dp to 16.dp                     // Portrait phones: breathing room
        }

        val isLargeScreen = minDimension >= 600.dp
        val contentMaxWidth = if (isLargeScreen) 500.dp else 600.dp

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = hPadding, vertical = vPadding)
                    .padding(end = 48.dp), // Extra padding for theme toggle
                horizontalArrangement = Arrangement.spacedBy(if (isLargeScreen) 64.dp else 32.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderSection(
                    score = state.score,
                    bestScore = state.bestScore,
                    onRestart = { viewModel.restartGame() },
                    onUndo = { viewModel.undo() },
                    canUndo = state.canUndo,
                    isLandscape = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .aspectRatio(1f)
                        .sizeIn(maxWidth = contentMaxWidth)
                ) {
                    BoardContainer(state = state, isDark = isDarkMode)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = hPadding, vertical = vPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HeaderSection(
                    score = state.score,
                    bestScore = state.bestScore,
                    onRestart = { viewModel.restartGame() },
                    onUndo = { viewModel.undo() },
                    canUndo = state.canUndo,
                    isLandscape = false
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 48.dp else 16.dp))
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        .sizeIn(maxWidth = contentMaxWidth)
                ) {
                    BoardContainer(state = state, isDark = isDarkMode)
                }
            }
        }
    }
}

@Composable
fun BoardContainer(state: GameState, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // We wrap the board in a padded box, but keep the overlay outside the padding
        // so it covers the entire board including the gutters where shadows might spill.
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            GameBoard(board = state.board, isDark = isDark)
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
                    text = "Game Over!",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun HeaderSection(
    score: Int,
    bestScore: Int,
    onRestart: () -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean,
    isLandscape: Boolean
) {
    val titleSize = if (isLandscape) 56.sp else 64.sp
    val buttonPadding = if (isLandscape) PaddingValues(horizontal = 16.dp, vertical = 8.dp) 
                        else PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    val alignment = if (isLandscape) Alignment.End else Alignment.CenterHorizontally

    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "2048",
            fontSize = titleSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScoreCard(label = "SCORE", score = score)
            ScoreCard(label = "BEST", score = bestScore)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameButton(text = "Undo", onClick = onUndo, enabled = canUndo, padding = buttonPadding)
            GameButton(text = "New Game", onClick = onRestart, padding = buttonPadding)
        }
    }
}

@Composable
private fun GameButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    padding: PaddingValues
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = padding
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ScoreCard(label: String, score: Int) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = score.toString(),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GameBoard(board: List<List<Tile?>>, isDark: Boolean) {
    var boardWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val size = board.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { boardWidthPx = it.size.width }
    ) {
        if (boardWidthPx > 0 && size > 0) {
            val spacingDp = 12.dp
            val spacingPx = with(density) { spacingDp.toPx() }
            val tileSizePx = (boardWidthPx - (spacingPx * (size - 1))) / size.toFloat()
            val tileSizeDp = with(density) { tileSizePx.toDp() }

            // 1. Static Background Grid
            Column(verticalArrangement = Arrangement.spacedBy(spacingDp)) {
                repeat(size) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacingDp)) {
                        repeat(size) {
                            Box(
                                modifier = Modifier
                                    .size(tileSizeDp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }

            // 2. Persistent Active Tiles
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
                    
                    val animXPx by animateFloat(
                        targetValue = targetXPx,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "glideX"
                    )
                    val animYPx by animateFloat(
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
                        TileView(tile = tile, isDark = isDark)
                    }
                }
            }
        }
    }
}

@Composable
fun TileView(tile: Tile, isDark: Boolean = isSystemInDarkTheme()) {
    // Use a local state for the value so we can delay the color/text change until after the slide
    val displayValue = remember(tile.id) { mutableIntStateOf(tile.value) }
    val isVisible = remember(tile.id) { mutableStateOf(!tile.isNew) }

    val backgroundColor = getTileBackgroundColor(displayValue.intValue, isDark)
    val textColor = getTileTextColor(displayValue.intValue, isDark)

    val scale = remember { Animatable(if (tile.isNew) 0f else 1f) }

    LaunchedEffect(tile.id, tile.value, tile.isNew) {
        if (tile.isNew && !isVisible.value) {
            delay(100) // Wait for other tiles to slide
            isVisible.value = true
            displayValue.intValue = tile.value
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            // Ensure tile is visible if it's no longer marked as new
            isVisible.value = true
            
            if (displayValue.intValue != tile.value) {
                delay(100) // Wait for slide to complete before changing color/popping
                displayValue.intValue = tile.value
                scale.snapTo(0.85f)
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            } else {
                scale.snapTo(1f)
            }
        }
    }

    if (isVisible.value) {
        BoxWithConstraints(
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
                displayValue.intValue >= 1024 -> maxWidth.value * 0.28f
                displayValue.intValue >= 100 -> maxWidth.value * 0.35f
                else -> maxWidth.value * 0.45f
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

private fun getTileBackgroundColor(value: Int, isDark: Boolean): Color = when (value) {
    2 -> if (isDark) Color(0xFF1E293B) else Color(0xFFEEE4DA)
    4 -> if (isDark) Color(0xFF334155) else Color(0xFFEDE0C8)
    8 -> if (isDark) Color(0xFF0EA5E9) else Color(0xFFF2B179)
    16 -> if (isDark) Color(0xFF0284C7) else Color(0xFFF59563)
    32 -> if (isDark) Color(0xFF2563EB) else Color(0xFFF67C5F)
    64 -> if (isDark) Color(0xFF4F46E5) else Color(0xFFF65E3B)
    128 -> if (isDark) Color(0xFF7C3AED) else Color(0xFFEDCF72)
    256 -> if (isDark) Color(0xFF8B5CF6) else Color(0xFFEDCC61)
    512 -> if (isDark) Color(0xFFA78BFA) else Color(0xFFEDC850)
    1024 -> if (isDark) Color(0xFFC084FC) else Color(0xFFEDC53F)
    2048 -> if (isDark) Color(0xFFE879F9) else Color(0xFFEDC22E)
    else -> if (isDark) Color(0xFF0F172A) else Color(0xFF3C3A32)
}

private fun getTileTextColor(value: Int, isDark: Boolean): Color = when (value) {
    2, 4 -> if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF776E65)
    else -> Color.White
}
