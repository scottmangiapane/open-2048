package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.ControlMode
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.UserPreferences
import com.scottmangiapane.open2048.ui.components.GameConfirmationDialog
import com.scottmangiapane.open2048.ui.components.SettingsDialog

@Composable
fun MenuScreen(
    userPreferences: UserPreferences,
    onStartGame: (GameMode) -> Unit,
    onResumeGame: () -> Unit,
    onSetTheme: (AppTheme) -> Unit,
    onSetVibrationEnabled: (Boolean) -> Unit,
    onSetControlMode: (ControlMode) -> Unit,
    onSetShowUndo: (Boolean) -> Unit,
    onSetShowStopwatch: (Boolean) -> Unit,
    canResume: Boolean,
    hasProgress: Boolean,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val amber = colorResource(R.color.amber_500)
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var pendingGameMode by rememberSaveable(stateSaver = GameMode.Saver) { mutableStateOf<GameMode?>(null) }

    val handleStartGame: (GameMode) -> Unit = { mode ->
        if (hasProgress) {
            pendingGameMode = mode
        } else {
            onStartGame(mode)
        }
    }

    if (showSettings) {
        SettingsDialog(
            preferences = userPreferences,
            onDismiss = { showSettings = false },
            onThemeChange = onSetTheme,
            onVibrationToggle = onSetVibrationEnabled,
            onControlModeChange = onSetControlMode,
            onShowUndoToggle = onSetShowUndo,
            onShowStopwatchToggle = onSetShowStopwatch
        )
    }

    if (pendingGameMode != null) {
        GameConfirmationDialog(
            title = "Start New Game?",
            message = "Your current game progress will be lost.",
            confirmText = "Start New Game",
            onConfirm = {
                pendingGameMode?.let { onStartGame(it) }
                pendingGameMode = null
            },
            onDismiss = { pendingGameMode = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .displayCutoutPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = if (isLandscape) 24.dp else 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 16.dp else 24.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "2048",
                fontSize = if (isLandscape) 56.sp else 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top
                ) {
                    MenuColumn(modifier = Modifier.weight(1f)) {
                        ResumeSection(canResume, onResumeGame)
                        ChallengeSection(handleStartGame, amber)
                    }

                    MenuColumn(modifier = Modifier.weight(1f)) {
                        ClassicSection(handleStartGame)
                    }

                    MenuColumn(modifier = Modifier.weight(1f)) {
                        BlitzSection(handleStartGame)
                    }
                }
            } else {
                if (canResume) {
                    MenuButton("Resume Game", Icons.Rounded.PlayArrow, onResumeGame, MaterialTheme.colorScheme.secondary)
                }

                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ChallengeSection(handleStartGame, amber)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ClassicSection(handleStartGame)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BlitzSection(handleStartGame)
                    }
                }
            }
        }

        IconButton(
            onClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(8.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ResumeSection(canResume: Boolean, onResumeGame: () -> Unit) {
    if (canResume) {
        MenuCategoryHeader("CONTINUE")
        MenuButton("Resume", Icons.Rounded.PlayArrow, onResumeGame, MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ChallengeSection(onStartGame: (GameMode) -> Unit, amber: Color) {
    MenuCategoryHeader("CHALLENGE")
    MenuButton("Daily Challenge", Icons.Rounded.EmojiEvents, { onStartGame(GameMode.Daily.today()) }, amber)
}

@Composable
private fun ClassicSection(onStartGame: (GameMode) -> Unit) {
    MenuCategoryHeader("CLASSIC")
    ClassicModes(onStartGame)
}

@Composable
private fun BlitzSection(onStartGame: (GameMode) -> Unit) {
    MenuCategoryHeader("BLITZ")
    BlitzModes(onStartGame)
}

@Composable
private fun ClassicModes(onStartGame: (GameMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MenuButton("Classic 4x4", Icons.Rounded.Grid4x4, { onStartGame(GameMode.Classic(4)) })
        MenuButton("Small 3x3", Icons.Rounded.Grid3x3, { onStartGame(GameMode.Classic(3)) })
        MenuButton("Large 5x5", Icons.Rounded.GridView, { onStartGame(GameMode.Classic(5)) })
    }
}

@Composable
private fun BlitzModes(onStartGame: (GameMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MenuButton("2 Minute Blitz", Icons.Rounded.HourglassBottom, { onStartGame(GameMode.Blitz(2)) })
        MenuButton("5 Minute Blitz", Icons.Rounded.HourglassTop, { onStartGame(GameMode.Blitz(5)) })
    }
}

@Composable
private fun MenuCategoryHeader(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
private fun MenuColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.widthIn(max = 240.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // Spacer to keep text centered relative to the whole button width
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
