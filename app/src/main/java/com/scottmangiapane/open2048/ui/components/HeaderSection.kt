package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.logic.TimeUtils
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState

@Composable
fun HeaderSection(
    state: GameState,
    onRestart: () -> Unit,
    onUndo: () -> Unit,
    isLandscape: Boolean,
    showUndo: Boolean = true,
    showStopwatch: Boolean = true,
    restartFocusRequester: FocusRequester? = null,
    undoFocusRequester: FocusRequester? = null,
    onMoveFocusToBoard: () -> Unit = {}
) {
    val buttonPadding = if (isLandscape) PaddingValues(horizontal = 16.dp, vertical = 6.dp) 
                        else PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    val alignment = if (isLandscape) Alignment.End else Alignment.CenterHorizontally

    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = alignment) {
            Text(
                text = "2048",
                style = if (isLandscape) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            val modeText = when (val mode = state.gameMode) {
                is GameMode.Daily -> "DAILY CHALLENGE"
                is GameMode.Blitz -> "${mode.durationMinutes}M BLITZ"
                is GameMode.Classic -> if (mode.size != 4) "${mode.size}x${mode.size} CLASSIC" else "CLASSIC"
            }
            
            Text(
                text = modeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.offset(y = if (isLandscape) 0.dp else (-8).dp)
            )
        }

        if (state.gameMode is GameMode.Blitz) {
            TimerDisplay(timeLeftMs = state.timeLeftMs ?: 0L)
        } else if (showStopwatch) {
            StopwatchDisplay(elapsedTimeMs = state.elapsedTimeMs)
        }

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScoreCard(label = "SCORE", score = state.score)
            ScoreCard(
                label = if (state.gameMode is GameMode.Daily) "DAY BEST" else "BEST",
                score = state.bestScore
            )
            ScoreCard(label = "MOVES", score = state.movesCount)
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showUndo) {
                val canUndo = state.canUndo && (state.timeLeftMs == null || state.timeLeftMs > 0)

                GameButton(
                    text = "Undo",
                    onClick = {
                        onUndo()
                        onMoveFocusToBoard()
                    },
                    padding = buttonPadding,
                    enabled = canUndo,
                    modifier = if (undoFocusRequester != null) Modifier.focusRequester(undoFocusRequester) else Modifier
                )
            }
            GameButton(
                text = "New Game",
                onClick = onRestart,
                padding = buttonPadding,
                modifier = if (restartFocusRequester != null) Modifier.focusRequester(restartFocusRequester) else Modifier
            )
        }
    }
}

@Composable
private fun TimerDisplay(timeLeftMs: Long) {
    Text(
        text = TimeUtils.formatDuration(timeLeftMs),
        style = MaterialTheme.typography.displaySmall,
        color = if (timeLeftMs < 10000) colorResource(R.color.rose_500) else MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun StopwatchDisplay(elapsedTimeMs: Long) {
    Text(
        text = "TIME: ${TimeUtils.formatDuration(elapsedTimeMs, showHours = true)}",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
