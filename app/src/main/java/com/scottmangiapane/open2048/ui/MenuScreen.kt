package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.ui.components.GameConfirmationDialog
import com.scottmangiapane.open2048.ui.components.appFocusBorder

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
    var pendingGameMode by rememberSaveable(stateSaver = GameMode.Saver) { mutableStateOf<GameMode?>(null) }

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
            onDismiss = { pendingGameMode = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isLandscape) Modifier else Modifier.verticalScroll(rememberScrollState()))
                    .padding(horizontal = 24.dp)
                    .padding(top = if (isLandscape) 8.dp else 48.dp, bottom = 8.dp)
                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 12.dp else 24.dp, Alignment.CenterVertically)
            ) {
                // Settings/Stats Row integrated into the main flow to avoid focus traps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statsInteractionSource = remember { MutableInteractionSource() }
                    val statsFocused by statsInteractionSource.collectIsFocusedAsState()
                    IconButton(
                        onClick = onNavigateToStats,
                        modifier = Modifier.appFocusBorder(isFocused = statsFocused),
                        interactionSource = statsInteractionSource
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val settingsInteractionSource = remember { MutableInteractionSource() }
                    val settingsFocused by settingsInteractionSource.collectIsFocusedAsState()
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.appFocusBorder(isFocused = settingsFocused),
                        interactionSource = settingsInteractionSource
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Logo2048(isLandscape)

                // Wrap the main content in a Box to control D-pad navigation flow
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.Top
                        ) {
                            MenuColumn(modifier = Modifier.weight(1f)) {
                                if (canResume) {
                                    MenuSection("CONTINUE") {
                                        MenuButton(
                                            text = "Resume",
                                            icon = Icons.Rounded.PlayArrow,
                                            onClick = onResumeGame,
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.focusRequester(initialFocusRequester)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                MenuSection("CHALLENGE") {
                                    MenuButton(
                                        text = "Daily Challenge",
                                        icon = Icons.Rounded.EmojiEvents,
                                        onClick = { handleStartGame(GameMode.Daily.today()) },
                                        containerColor = amber,
                                        modifier = if (!canResume) Modifier.focusRequester(initialFocusRequester) else Modifier
                                    )
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
                        Column(
                            modifier = Modifier.widthIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (canResume) {
                                MenuSection("CONTINUE") {
                                    MenuButton(
                                        text = "Resume",
                                        icon = Icons.Rounded.PlayArrow,
                                        onClick = onResumeGame,
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.focusRequester(initialFocusRequester)
                                    )
                                }
                            }
                            MenuSection("CHALLENGE") {
                                MenuButton(
                                    text = "Daily Challenge",
                                    icon = Icons.Rounded.EmojiEvents,
                                    onClick = { handleStartGame(GameMode.Daily.today()) },
                                    containerColor = amber,
                                    modifier = if (!canResume) Modifier.focusRequester(initialFocusRequester) else Modifier
                                )
                            }
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

@Composable
fun Logo2048(isLandscape: Boolean) {
    Text(
        text = "2048",
        fontSize = if (isLandscape) 48.sp else 72.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground,
        letterSpacing = if (isLandscape) (-2).sp else (-4).sp
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
        MenuButton("Classic 4x4", Icons.Rounded.Grid4x4, { onStartGame(GameMode.Classic(4)) })
        MenuButton("Small 3x3", Icons.Rounded.Grid3x3, { onStartGame(GameMode.Classic(3)) })
        MenuButton("Large 5x5", Icons.Rounded.GridView, { onStartGame(GameMode.Classic(5)) })
    }
}

@Composable
private fun BlitzModes(onStartGame: (GameMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MenuButton("2 Minute Blitz", Icons.Rounded.HourglassBottom, { onStartGame(GameMode.Blitz(2)) })
        MenuButton("5 Minute Blitz", Icons.Rounded.HourglassTop, { onStartGame(GameMode.Blitz(5)) })
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
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .appFocusBorder(
                isFocused = isFocused,
                color = if (containerColor == MaterialTheme.colorScheme.primary) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // Spacer to keep text centered relative to the whole button width
            Spacer(modifier = Modifier.size(18.dp))
        }
    }
}
