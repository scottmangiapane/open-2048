package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState as animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import com.scottmangiapane.open2048.model.GameMode
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBackToMenu: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 56.dp.toPx() }
    val currentTheme = state.theme

    // Re-request focus whenever the screen is composed or game state changes
    // to ensure keyboard support stays active.
    LaunchedEffect(state.isGameOver) {
        if (!state.isGameOver) focusRequester.requestFocus()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val minDimension = minOf(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)

    Box(
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
                    .padding(end = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isLargeScreen) 48.dp else 24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderSection(
                    state = state,
                    onRestart = { viewModel.restartGame() },
                    onUndo = { viewModel.undo() },
                    isLandscape = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .aspectRatio(1f)
                        .sizeIn(maxWidth = contentMaxWidth)
                ) {
                    BoardContainer(state = state, currentTheme = currentTheme)
                }

                DirectionalControls(
                    isLandscape = true,
                    onMove = { viewModel.move(it) }
                )
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
                    state = state,
                    onRestart = { viewModel.restartGame() },
                    onUndo = { viewModel.undo() },
                    isLandscape = false
                )
                Spacer(modifier = Modifier.height(if (isLargeScreen) 32.dp else 16.dp))
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        .sizeIn(maxWidth = contentMaxWidth)
                ) {
                    BoardContainer(state = state, currentTheme = currentTheme)
                }
                Spacer(modifier = Modifier.height(24.dp))
                DirectionalControls(
                    isLandscape = false,
                    onMove = { viewModel.move(it) }
                )
            }
        }

        // Navigation and Theme controls (Drawn last so they stay clickable)
        GameControls(
            currentTheme = currentTheme,
            onBackToMenu = onBackToMenu,
            onToggleTheme = { viewModel.cycleTheme() }
        )
    }
}

@Composable
private fun GameControls(
    currentTheme: AppTheme,
    onBackToMenu: () -> Unit,
    onToggleTheme: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        IconButton(
            onClick = onBackToMenu,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Menu",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            val icon = when (currentTheme) {
                AppTheme.LIGHT -> Icons.Default.LightMode
                AppTheme.DARK -> Icons.Default.DarkMode
                AppTheme.CLASSIC -> Icons.Default.Palette
            }
            Icon(
                imageVector = icon,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun BoardContainer(state: GameState, currentTheme: AppTheme) {
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
private fun HeaderSection(
    state: GameState,
    onRestart: () -> Unit,
    onUndo: () -> Unit,
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
        Column(horizontalAlignment = alignment) {
            Text(
                text = "2048",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            val modeText = when (val mode = state.gameMode) {
                is GameMode.Daily -> "Daily ${mode.month}/${mode.day}"
                is GameMode.Blitz -> "${mode.durationMinutes}m Blitz"
                is GameMode.Classic -> if (mode.size != 4) "${mode.size}x${mode.size}" else ""
            }
            
            if (modeText.isNotEmpty()) {
                Text(
                    text = modeText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.offset(y = if (isLandscape) 0.dp else (-8).dp)
                )
            }
        }

        if (state.gameMode is GameMode.Blitz) {
            TimerDisplay(timeLeftMs = state.timeLeftMs ?: 0L)
        } else {
            StopwatchDisplay(elapsedTimeMs = state.elapsedTimeMs)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScoreCard(label = "SCORE", score = state.score)
            ScoreCard(
                label = if (state.gameMode is GameMode.Daily) "DAY BEST" else "BEST",
                score = state.bestScore
            )
            ScoreCard(label = "MOVES", score = state.movesCount)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val canUndo = state.canUndo && (state.timeLeftMs == null || state.timeLeftMs > 0)
            GameButton(text = "Undo", onClick = onUndo, padding = buttonPadding, enabled = canUndo)
            GameButton(text = "New Game", onClick = onRestart, padding = buttonPadding)
        }
    }
}

@Composable
private fun TimerDisplay(timeLeftMs: Long) {
    val totalSeconds = timeLeftMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeString = "%02d:%02d".format(minutes, seconds)
    
    Text(
        text = timeString,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = if (timeLeftMs < 10000) colorResource(R.color.rose_500) else MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun StopwatchDisplay(elapsedTimeMs: Long) {
    val totalSeconds = elapsedTimeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val timeString = if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
    
    Text(
        text = "TIME: $timeString",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun DirectionalControls(
    isLandscape: Boolean,
    onMove: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLandscape) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ControlButton(Icons.Default.KeyboardArrowUp, { onMove(Direction.UP) })
            ControlButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, { onMove(Direction.LEFT) })
            ControlButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, { onMove(Direction.RIGHT) })
            ControlButton(Icons.Default.KeyboardArrowDown, { onMove(Direction.DOWN) })
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, { onMove(Direction.LEFT) })
            ControlButton(Icons.Default.KeyboardArrowUp, { onMove(Direction.UP) })
            ControlButton(Icons.Default.KeyboardArrowDown, { onMove(Direction.DOWN) })
            ControlButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, { onMove(Direction.RIGHT) })
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(icon, null, tint = Color.White)
    }
}

@Composable
private fun GameButton(
    text: String,
    onClick: () -> Unit,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = padding,
        modifier = modifier
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScoreCard(label: String, score: Int) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = score.toString(),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
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

            // 1. Static Background Grid
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
                        TileView(tile = tile, tileSize = tileSizeDp, currentTheme = currentTheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun TileView(tile: Tile, tileSize: Dp, currentTheme: AppTheme) {
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
                2 -> R.color.slate_800
                4 -> R.color.slate_600
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
