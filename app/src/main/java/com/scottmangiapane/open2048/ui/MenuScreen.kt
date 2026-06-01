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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Grid4x4
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.ui.components.GameButton
import com.scottmangiapane.open2048.ui.components.GameConfirmationDialog
import com.scottmangiapane.open2048.ui.components.MenuIconButton

@Composable
fun MenuScreen(
    onStartGame: (GameMode) -> Unit,
    onResumeGame: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    canResume: Boolean,
    hasProgress: Boolean,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val amber = colorResource(R.color.amber_500)
    var pendingGameMode by rememberSaveable(stateSaver = GameMode.Saver) { mutableStateOf(null) }

    val initialFocusRequester = remember { FocusRequester() }

    val handleStartGame: (GameMode) -> Unit = { mode ->
        if (hasProgress) {
            pendingGameMode = mode
        } else {
            onStartGame(mode)
        }
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
        ) {
            pendingGameMode = null
        }
    }

    // Request focus on the initial button when the screen is shown
    LaunchedEffect(Unit) {
        initialFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // We use a single Column for the entire layout to ensure predictable D-pad navigation.
        // The top icons and the main content are siblings in this Column.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        ) {
            // 1. Settings/Stats Row (Fixed at the top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuIconButton(onClick = onNavigateToStats) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = "Statistics",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }

                MenuIconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // 2. Main Content Area
            // We use weight(1f) to take up remaining space, and verticalScroll for small screens/landscape.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Shared composable for the Resume/Daily buttons
                    val mainButtons = @Composable {
                        if (canResume) {
                            MenuSection("CONTINUE") {
                                GameButton(
                                    text = "Resume",
                                    icon = Icons.Rounded.PlayArrow,
                                    onClick = onResumeGame,
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    fullWidth = true,
                                    modifier = Modifier.focusRequester(initialFocusRequester)
                                )
                            }
                        }
                        MenuSection("CHALLENGE") {
                            GameButton(
                                text = "Daily Challenge",
                                icon = Icons.Rounded.EmojiEvents,
                                onClick = { handleStartGame(GameMode.Daily.today()) },
                                containerColor = amber,
                                fullWidth = true,
                                modifier = if (!canResume) Modifier.focusRequester(initialFocusRequester) else Modifier
                            )
                        }
                    }

                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MenuColumn(modifier = Modifier.weight(1f)) {
                                Logo2048(isLandscape)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    mainButtons()
                                }
                            }

                            MenuColumn(modifier = Modifier.weight(1f)) {
                                MenuSection("CLASSIC") {
                                    ClassicModes(handleStartGame)
                                }
                            }

                            MenuColumn(modifier = Modifier.weight(1f)) {
                                MenuSection("BLITZ") {
                                    BlitzModes(handleStartGame)
                                }
                            }
                        }
                    } else {
                        Logo2048(isLandscape)
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.widthIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            mainButtons()
                            MenuSection("CLASSIC") {
                                ClassicModes(handleStartGame)
                            }
                            MenuSection("BLITZ") {
                                BlitzModes(handleStartGame)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun Logo2048(isLandscape: Boolean) {
    Text(
        text = "2048",
        style = if (isLandscape) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun MenuSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun ClassicModes(onStartGame: (GameMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GameButton("Classic 4x4", { onStartGame(GameMode.Classic(4)) }, fullWidth = true, icon = Icons.Rounded.Grid4x4)
        GameButton("Small 3x3", { onStartGame(GameMode.Classic(3)) }, fullWidth = true, icon = Icons.Rounded.Grid3x3)
        GameButton("Large 5x5", { onStartGame(GameMode.Classic(5)) }, fullWidth = true, icon = Icons.Rounded.GridView)
    }
}

@Composable
private fun BlitzModes(onStartGame: (GameMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GameButton("2 Minute Blitz", { onStartGame(GameMode.Blitz(2)) }, fullWidth = true, icon = Icons.Rounded.HourglassBottom)
        GameButton("5 Minute Blitz", { onStartGame(GameMode.Blitz(5)) }, fullWidth = true, icon = Icons.Rounded.HourglassTop)
    }
}

@Composable
private fun MenuColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
