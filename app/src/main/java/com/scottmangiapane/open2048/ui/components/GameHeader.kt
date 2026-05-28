package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.layout.*
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
    val titleSize = if (isLandscape) 48.sp else 56.sp
    val buttonPadding = if (isLandscape) PaddingValues(horizontal = 16.dp, vertical = 8.dp) 
                        else PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    val alignment = if (isLandscape) Alignment.End else Alignment.CenterHorizontally

    Column(
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = alignment) {
            Text(
                text = "2048",
                fontSize = titleSize,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-3).sp
            )
            
            val modeText = when (val mode = state.gameMode) {
                is GameMode.Daily -> "DAILY CHALLENGE"
                is GameMode.Blitz -> "${mode.durationMinutes}M BLITZ"
                is GameMode.Classic -> if (mode.size != 4) "${mode.size}x${mode.size} CLASSIC" else "CLASSIC"
            }
            
            Text(
                text = modeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
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
                GameButton(text = "Undo", onClick = onUndo, padding = buttonPadding, enabled = canUndo)
            }
            GameButton(text = "New Game", onClick = onRestart, padding = buttonPadding)
        }
    }
}

@Composable
private fun TimerDisplay(timeLeftMs: Long) {
    Text(
        text = formatTime(timeLeftMs),
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = if (timeLeftMs < 10000) colorResource(R.color.rose_500) else MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun StopwatchDisplay(elapsedTimeMs: Long) {
    Text(
        text = "TIME: ${formatTime(elapsedTimeMs, showHours = true)}",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

private fun formatTime(ms: Long, showHours: Boolean = false): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    val locale = java.util.Locale.getDefault()
    return when {
        showHours && hours > 0 -> String.format(locale, "%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format(locale, "%02d:%02d", minutes, seconds)
    }
}
