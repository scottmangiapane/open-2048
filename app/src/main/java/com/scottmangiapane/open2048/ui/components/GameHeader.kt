package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState

@Composable
fun HeaderSection(
    state: GameState,
    onRestart: () -> Unit,
    onUndo: () -> Unit,
    isLandscape: Boolean,
    showUndo: Boolean = true,
    showStopwatch: Boolean = true
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
        } else if (showStopwatch) {
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
            if (showUndo) {
                val canUndo = state.canUndo && (state.timeLeftMs == null || state.timeLeftMs > 0)
                GameButton(text = "Undo", onClick = onUndo, padding = buttonPadding, enabled = canUndo)
            }
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
