package com.scottmangiapane.open2048.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scottmangiapane.open2048.R
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.ui.components.GameButton
import com.scottmangiapane.open2048.ui.components.GameConfirmationDialog
import com.scottmangiapane.open2048.ui.components.MenuIconButton
import com.scottmangiapane.open2048.ui.components.scrollToTopOnFocus
import com.scottmangiapane.open2048.ui.components.scrollToBottomOnFocus
import com.scottmangiapane.open2048.ui.components.oledBorder
import com.scottmangiapane.open2048.ui.components.appContainerColor

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.launch

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
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()
    var pendingGameMode by rememberSaveable(stateSaver = GameMode.Saver) { mutableStateOf(null) }

    val initialFocusRequester = remember { FocusRequester() }
    val statsFocusRequester = remember { FocusRequester() }

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

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val scrollToTopModifier = Modifier.scrollToTopOnFocus(scrollState, coroutineScope)
    val scrollToBottomModifier = Modifier.scrollToBottomOnFocus(scrollState, coroutineScope)

    val contentHPadding = if (isSmall) 12.dp else 24.dp
    val contentVPadding = if (isSmall) 6.dp else 24.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
    ) {
        // 1. Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = contentHPadding, vertical = contentVPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Shared composable for the Resume/Daily buttons
            val mainButtons = @Composable {
                if (canResume) {
                    MenuSection("CONTINUE") {
                        GameButton(
                            text = "Resume",
                            icon = Icons.Rounded.PlayArrow,
                            onClick = onResumeGame,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            fullWidth = true,
                            modifier = Modifier
                                .focusRequester(initialFocusRequester)
                                .then(scrollToTopModifier)
                                .focusProperties { up = statsFocusRequester }
                        )
                    }
                }
                MenuSection("CHALLENGE") {
                    GameButton(
                        text = "Daily Challenge",
                        icon = Icons.Rounded.EmojiEvents,
                        onClick = { handleStartGame(GameMode.Daily.today()) },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        fullWidth = true,
                        modifier = (if (!canResume) Modifier.focusRequester(initialFocusRequester).then(scrollToTopModifier) else Modifier)
                            .then(if (!canResume) Modifier.focusProperties { up = statsFocusRequester } else Modifier)
                    )
                }
            }

            if (isSmall && !isLandscape) {
                // Ultra-small portrait layout: Include logo inside top bar row to prevent eclipsing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Logo2048(isLandscape = false)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MenuIconButton(
                            onClick = onNavigateToStats,
                            modifier = Modifier
                                .focusRequester(statsFocusRequester)
                                .then(scrollToTopModifier)
                                .focusProperties { down = initialFocusRequester }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BarChart,
                                contentDescription = "Statistics",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        MenuIconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .then(scrollToTopModifier)
                                .focusProperties { down = initialFocusRequester }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.widthIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    mainButtons()
                    MenuSection("CLASSIC") {
                        ClassicModes(
                            onStartGame = handleStartGame,
                            bottomButtonModifier = scrollToBottomModifier
                        )
                    }
                    MenuSection("BLITZ") {
                        BlitzModes(
                            onStartGame = handleStartGame,
                            bottomButtonModifier = scrollToBottomModifier
                        )
                    }
                }
            } else if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isSmall) 6.dp else 16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuColumn(modifier = Modifier.weight(1f)) {
                        Logo2048(isLandscape = true)
                        Spacer(modifier = Modifier.height(if (isSmall) 4.dp else 8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(if (isSmall) 6.dp else 12.dp)) {
                            mainButtons()
                        }
                    }

                    MenuColumn(modifier = Modifier.weight(1f)) {
                        MenuSection("CLASSIC") {
                            ClassicModes(
                                onStartGame = handleStartGame,
                                topButtonModifier = Modifier.focusProperties { up = statsFocusRequester }
                            )
                        }
                    }

                    MenuColumn(modifier = Modifier.weight(1f)) {
                        MenuSection("BLITZ") {
                            BlitzModes(
                                onStartGame = handleStartGame,
                                topButtonModifier = Modifier.focusProperties { up = statsFocusRequester },
                                bottomButtonModifier = scrollToBottomModifier
                            )
                        }
                    }
                }
            } else {
                // Exact original portrait layout for normal/large screens
                Logo2048(isLandscape = false)
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.widthIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    mainButtons()
                    MenuSection("CLASSIC") {
                        ClassicModes(
                            onStartGame = handleStartGame,
                            bottomButtonModifier = scrollToBottomModifier
                        )
                    }
                    MenuSection("BLITZ") {
                        BlitzModes(
                            onStartGame = handleStartGame,
                            bottomButtonModifier = scrollToBottomModifier
                        )
                    }
                }
            }
        }

        // 2. Settings/Stats Row (Overlay for normal/large devices or landscape)
        if (!isSmall || isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = contentHPadding, vertical = if (isSmall) 4.dp else 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconSize = if (isSmall) 22.dp else 28.dp

                MenuIconButton(
                    onClick = onNavigateToStats,
                    modifier = Modifier
                        .focusRequester(statsFocusRequester)
                        .then(scrollToTopModifier)
                        .focusProperties { down = initialFocusRequester }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = "Statistics",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(iconSize)
                    )
                }

                MenuIconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .then(scrollToTopModifier)
                        .focusProperties { down = initialFocusRequester }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}

@Composable
fun Logo2048(isLandscape: Boolean) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()
    val logoStyle = when {
        isSmall -> MaterialTheme.typography.titleMedium
        isLandscape -> MaterialTheme.typography.displayMedium
        else -> MaterialTheme.typography.displayLarge
    }

    Text(
        text = "2048",
        style = logoStyle,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun MenuSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isSmall) 2.dp else 6.dp)
    ) {
        Text(
            text = title,
            style = if (isSmall) MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp) else MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun ClassicModes(
    onStartGame: (GameMode) -> Unit,
    topButtonModifier: Modifier = Modifier,
    bottomButtonModifier: Modifier = Modifier
) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()

    Column(verticalArrangement = Arrangement.spacedBy(if (isSmall) 4.dp else 8.dp)) {
        GameButton(
            text = "Classic 4x4",
            onClick = { onStartGame(GameMode.Classic(4)) },
            fullWidth = true,
            icon = Icons.Rounded.Grid4x4,
            modifier = topButtonModifier
        )
        GameButton(
            text = "Small 3x3",
            onClick = { onStartGame(GameMode.Classic(3)) },
            fullWidth = true,
            icon = Icons.Rounded.Grid3x3
        )
        GameButton(
            text = "Large 5x5",
            onClick = { onStartGame(GameMode.Classic(5)) },
            fullWidth = true,
            icon = Icons.Rounded.GridView,
            modifier = bottomButtonModifier
        )
    }
}

@Composable
private fun BlitzModes(
    onStartGame: (GameMode) -> Unit,
    topButtonModifier: Modifier = Modifier,
    bottomButtonModifier: Modifier = Modifier
) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()

    Column(verticalArrangement = Arrangement.spacedBy(if (isSmall) 4.dp else 8.dp)) {
        GameButton(
            text = "2 Minute Blitz",
            onClick = { onStartGame(GameMode.Blitz(2)) },
            fullWidth = true,
            icon = Icons.Rounded.HourglassBottom,
            modifier = topButtonModifier
        )
        GameButton(
            text = "5 Minute Blitz",
            onClick = { onStartGame(GameMode.Blitz(5)) },
            fullWidth = true,
            icon = Icons.Rounded.HourglassTop,
            modifier = bottomButtonModifier
        )
    }
}

@Composable
private fun MenuColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()

    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .background(
                color = appContainerColor(),
                shape = RoundedCornerShape(8.dp)
            )
            .oledBorder()
            .padding(if (isSmall) 6.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
