package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.ui.components.*
import kotlin.math.abs

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBackToMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 56.dp.toPx() }
    val currentTheme = state.theme

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
    ) {
        val (hPadding, vPadding) = when {
            minDimension >= 840.dp -> 172.dp to 144.dp
            minDimension >= 600.dp -> 120.dp to 96.dp
            isLandscape -> 0.dp to 8.dp
            else -> 32.dp to 16.dp
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
