package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.ControlMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.UserPreferences
import com.scottmangiapane.open2048.ui.components.*
import kotlin.math.abs

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBackToMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val hasProgress by remember { derivedStateOf { state.movesCount > 0 && !state.isGameOver } }
    var highestTileSeen by rememberSaveable { mutableIntStateOf(state.highestTile) }
    var showConfetti by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 56.dp.toPx() }
    var showRestartConfirmation by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isGameOver) {
        if (!state.isGameOver) focusRequester.requestFocus()
    }

    LaunchedEffect(state.highestTile) {
        val target = state.gameMode.winCondition
        if (userPreferences.confettiEnabled && state.highestTile >= target && state.highestTile > highestTileSeen) {
            showConfetti = true
            highestTileSeen = state.highestTile
        } else if (state.highestTile < highestTileSeen) {
            // Reset if game was restarted or undone to a lower state
            highestTileSeen = state.highestTile
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val minDimension = minOf(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)

    if (showRestartConfirmation) {
        GameConfirmationDialog(
            title = "Restart Game?",
            message = "Your current game progress will be lost.",
            confirmText = "Restart",
            onConfirm = {
                viewModel.restartGame()
                showRestartConfirmation = false
            },
            onDismiss = { showRestartConfirmation = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .displayCutoutPadding()
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
            .then(
                if (userPreferences.controlMode != ControlMode.ARROWS) {
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
                } else Modifier
            )
    ) {
        GameLayout(
            state = state,
            userPreferences = userPreferences,
            isLandscape = isLandscape,
            minDimension = minDimension,
            onMove = { viewModel.move(it) },
            onRestart = { 
                if (hasProgress) {
                    showRestartConfirmation = true
                } else {
                    viewModel.restartGame()
                }
            },
            onUndo = { viewModel.undo() }
        )

        GameControls(
            onBackToMenu = onBackToMenu
        )

        if (showConfetti) {
            ConfettiOverlay(onAnimationFinished = { showConfetti = false })
        }
    }
}

@Composable
private fun GameLayout(
    state: GameState,
    userPreferences: UserPreferences,
    isLandscape: Boolean,
    minDimension: Dp,
    onMove: (Direction) -> Unit,
    onRestart: () -> Unit,
    onUndo: () -> Unit
) {
    val (hPadding, vPadding) = when {
        minDimension >= 840.dp -> 172.dp to 144.dp
        minDimension >= 600.dp -> 120.dp to 96.dp
        isLandscape -> 0.dp to 8.dp
        else -> 24.dp to 16.dp
    }

    val isLargeScreen = minDimension >= 600.dp
    val contentMaxWidth = if (isLargeScreen) 500.dp else 600.dp
    val showControls = userPreferences.controlMode != ControlMode.GESTURES

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
                onRestart = onRestart,
                onUndo = onUndo,
                isLandscape = true,
                showUndo = userPreferences.showUndo,
                showStopwatch = userPreferences.showStopwatch
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight(0.95f)
                    .aspectRatio(1f)
                    .sizeIn(maxWidth = contentMaxWidth)
            ) {
                BoardContainer(
                    state = state,
                    currentTheme = state.theme ?: userPreferences.theme,
                    animationSpeed = userPreferences.animationSpeed
                )
            }

            if (showControls) {
                DirectionalControls(isLandscape = true, onMove = onMove)
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
                state = state,
                onRestart = onRestart,
                onUndo = onUndo,
                isLandscape = false,
                showUndo = userPreferences.showUndo,
                showStopwatch = userPreferences.showStopwatch
            )
            Spacer(modifier = Modifier.height(if (isLargeScreen) 32.dp else 16.dp))
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .sizeIn(maxWidth = contentMaxWidth)
            ) {
                BoardContainer(
                    state = state,
                    currentTheme = state.theme ?: userPreferences.theme,
                    animationSpeed = userPreferences.animationSpeed
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (showControls) {
                DirectionalControls(isLandscape = false, onMove = onMove)
            }
        }
    }
}

@Composable
private fun GameControls(
    onBackToMenu: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        IconButton(
            onClick = onBackToMenu,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back to Menu",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
