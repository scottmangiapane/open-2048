package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.ControlMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.UserPreferences
import com.scottmangiapane.open2048.ui.components.*

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBackToMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val hasProgress by remember { derivedStateOf { (state.movesCount > 0) && !state.isGameOver } }
    var highestTileSeen by rememberSaveable { mutableIntStateOf(state.highestTile) }
    var showConfetti by remember { mutableStateOf(value = false) }
    val focusRequester = remember { FocusRequester() }
    val restartFocusRequester = remember { FocusRequester() }
    val undoFocusRequester = remember { FocusRequester() }
    var showRestartConfirmation by rememberSaveable { mutableStateOf(value = false) }
    var forcePlayModeOnNextFocus by remember { mutableStateOf(value = false) }

    val configuration = LocalConfiguration.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val minDimension = with(density) {
        minOf(windowInfo.containerSize.width.toDp(), windowInfo.containerSize.height.toDp())
    }
    
    val inputModeManager = LocalInputModeManager.current
    val isKeyboardMode = inputModeManager.inputMode == InputMode.Keyboard
    val hasTouch = viewModel.hasTouch

    // We want to auto-capture input on non-touch devices, or when in keyboard mode
    val shouldCaptureInput = !hasTouch || isKeyboardMode

    LaunchedEffect(shouldCaptureInput) {
        if (shouldCaptureInput) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(state.highestTile) {
        val target = state.gameMode.winCondition
        if ((state.highestTile >= target) && (state.highestTile > highestTileSeen)) {
            showConfetti = true
            highestTileSeen = state.highestTile
        } else if (state.highestTile < highestTileSeen) {
            // Reset if game was restarted or undone to a lower state
            highestTileSeen = state.highestTile
        }
    }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            restartFocusRequester.requestFocus()
        }
    }

    // Re-focus board when game is restarted or undone
    LaunchedEffect(state.isGameOver, forcePlayModeOnNextFocus) {
        if (!state.isGameOver && forcePlayModeOnNextFocus) {
            focusRequester.requestFocus()
        }
    }

    if (showRestartConfirmation) {
        GameConfirmationDialog(
            title = "Restart Game?",
            message = "Your current game progress will be lost.",
            confirmText = "Restart",
            onConfirm = {
                viewModel.restartGame()
                showRestartConfirmation = false
                forcePlayModeOnNextFocus = true
            }
        ) {
            showRestartConfirmation = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .swipeGestures(
                enabled = userPreferences.controlMode != ControlMode.ARROWS && 
                         userPreferences.fullScreenGestures && 
                         !state.isGameOver,
                onMove = { viewModel.move(it) }
            )
            .systemBarsPadding()
            .displayCutoutPadding(),
    ) {
        GameLayout(
            state = state,
            userPreferences = userPreferences,
            isLandscape = isLandscape,
            minDimension = minDimension,
            hasTouch = hasTouch,
            isKeyboardMode = isKeyboardMode,
            focusRequester = focusRequester,
            restartFocusRequester = restartFocusRequester,
            undoFocusRequester = undoFocusRequester,
            onMove = { viewModel.move(it) },
            onRestart = { 
                if (hasProgress) {
                    showRestartConfirmation = true
                } else {
                    viewModel.restartGame()
                    forcePlayModeOnNextFocus = true
                    focusRequester.requestFocus()
                }
            },
            onUndo = { viewModel.undo() },
            onMoveFocusToBoard = { 
                forcePlayModeOnNextFocus = true
                focusRequester.requestFocus() 
            },
            onBoardFocusGained = { _ ->
                if (forcePlayModeOnNextFocus) {
                    forcePlayModeOnNextFocus = false
                }
            },
            forcePlayMode = forcePlayModeOnNextFocus,
            fullScreenGestures = userPreferences.fullScreenGestures
        )

        GameControls(
            onBackToMenu = onBackToMenu
        )

        if (showConfetti) {
            ConfettiOverlay { showConfetti = false }
        }
    }
}

@Composable
private fun GameLayout(
    state: GameState,
    userPreferences: UserPreferences,
    isLandscape: Boolean,
    minDimension: Dp,
    hasTouch: Boolean,
    isKeyboardMode: Boolean,
    focusRequester: FocusRequester,
    restartFocusRequester: FocusRequester,
    undoFocusRequester: FocusRequester,
    onMove: (Direction) -> Unit,
    onRestart: () -> Unit,
    onUndo: () -> Unit,
    onMoveFocusToBoard: () -> Unit,
    onBoardFocusGained: (Boolean) -> Unit,
    forcePlayMode: Boolean,
    fullScreenGestures: Boolean,
) {
    val (hPadding, vPadding) = when {
        minDimension >= 840.dp -> 48.dp to 32.dp
        minDimension >= 600.dp -> 32.dp to 24.dp
        isLandscape -> 24.dp to 16.dp
        else -> 16.dp to 16.dp
    }

    val isLargeScreen = minDimension >= 600.dp
    val contentMaxWidth = if (isLargeScreen) 800.dp else 600.dp
    val showControls = hasTouch && (userPreferences.controlMode != ControlMode.GESTURES)

    val header = @Composable {
        HeaderSection(
            state = state,
            onRestart = onRestart,
            onUndo = onUndo,
            isLandscape = isLandscape,
            showUndo = userPreferences.showUndo,
            showStopwatch = userPreferences.showStopwatch,
            restartFocusRequester = restartFocusRequester,
            undoFocusRequester = undoFocusRequester,
            onMoveFocusToBoard = onMoveFocusToBoard
        )
    }

    val board: @Composable (Modifier) -> Unit = { modifier ->
        BoxWithConstraints(
            modifier = modifier.sizeIn(maxWidth = contentMaxWidth)
        ) {
            val boardSize = minOf(this.maxWidth, this.maxHeight)
            Box(
                modifier = Modifier
                    .size(boardSize)
                    .align(Alignment.Center),
            ) {
                BoardContainer(
                    state = state,
                    currentTheme = state.theme ?: userPreferences.theme,
                    animationSpeed = userPreferences.animationSpeed,
                    controlMode = userPreferences.controlMode,
                    fullScreenGestures = fullScreenGestures,
                    focusRequester = focusRequester,
                    autoPlay = !hasTouch || isKeyboardMode || forcePlayMode,
                    hasTouch = hasTouch,
                    onMove = onMove,
                    onFocusGained = onBoardFocusGained
                )
            }
        }
    }

    val controls = @Composable {
        if (showControls) {
            DirectionalControls(isLandscape = isLandscape, onMove = onMove)
        }
    }

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPadding, vertical = vPadding),
            horizontalArrangement = Arrangement.spacedBy(if (isLargeScreen) 48.dp else 24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            header()
            board(Modifier.weight(1f, fill = false).fillMaxHeight(0.95f))
            controls()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPadding, vertical = vPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            header()
            Spacer(modifier = Modifier.height(if (isLargeScreen) 24.dp else 16.dp))
            board(Modifier.weight(1f, fill = false).fillMaxWidth())
            Spacer(modifier = Modifier.height(if (isLargeScreen) 24.dp else 16.dp))
            controls()
        }
    }
}

@Composable
private fun GameControls(
    onBackToMenu: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        MenuIconButton(
            onClick = onBackToMenu,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back to Menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
