package com.scottmangiapane.open2048.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scottmangiapane.open2048.logic.TimeUtils
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.ui.components.appFocusBorder
import com.scottmangiapane.open2048.ui.components.oledBorder
import com.scottmangiapane.open2048.ui.components.appContainerColor

import com.scottmangiapane.open2048.ui.components.scrollToTopOnFocus
import com.scottmangiapane.open2048.ui.components.scrollToBottomOnFocus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
                ),
                title = {
                    Text(
                        text = "Statistics",
                        style = if (isSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    IconButton(
                        onClick = onBack,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .appFocusBorder(isFocused = isFocused)
                            .scrollToTopOnFocus(scrollState, coroutineScope)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(if (isSmall) 20.dp else 24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
    ) { padding ->
        val screenPadding = if (isSmall) 12.dp else 24.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .verticalScroll(scrollState)
                .padding(screenPadding),
            verticalArrangement = Arrangement.spacedBy(screenPadding)
        ) {
            // Daily Challenge Hero Card
            val today = GameMode.Daily.today()
            StatsSection(title = "DAILY CHALLENGE", icon = Icons.Rounded.EmojiEvents) {
                DailyHeroCard(today, viewModel)
            }

            // Classic Modes
            StatsSection(title = "CLASSIC MODES", icon = Icons.Rounded.Grid4x4) {
                ModeStatCard(label = "Classic 4x4", mode = GameMode.Classic(4), viewModel = viewModel)
                ModeStatCard(label = "Small 3x3", mode = GameMode.Classic(3), viewModel = viewModel)
                ModeStatCard(label = "Large 5x5", mode = GameMode.Classic(5), viewModel = viewModel)
            }

            // Blitz Modes
            StatsSection(title = "BLITZ MODES", icon = Icons.Rounded.Timer) {
                Box(modifier = Modifier.scrollToBottomOnFocus(scrollState, coroutineScope)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModeStatCard(label = "2 Minute Blitz", mode = GameMode.Blitz(2), viewModel = viewModel)
                        ModeStatCard(label = "5 Minute Blitz", mode = GameMode.Blitz(5), viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()

    Column(verticalArrangement = Arrangement.spacedBy(if (isSmall) 6.dp else 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (isSmall) 16.dp else 18.dp)
            )
            Text(
                text = title,
                style = if (isSmall) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        content()
    }
}

@Composable
private fun DailyHeroCard(mode: GameMode.Daily, viewModel: GameViewModel) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()
    val bestScore by viewModel.getBestScore(mode).collectAsStateWithLifecycle()
    val highestTile by viewModel.getHighestTile(mode).collectAsStateWithLifecycle()
    val fewestMoves by viewModel.getFewestMoves(mode).collectAsStateWithLifecycle()
    val fastestTime by viewModel.getFastestTime(mode).collectAsStateWithLifecycle()
    
    val totalWins by viewModel.getWinCount(mode.groupStatsId).collectAsStateWithLifecycle()
    val totalPlayed by viewModel.getGamesPlayed(mode.groupStatsId).collectAsStateWithLifecycle()
    val totalTime by viewModel.getTotalTime(mode.groupStatsId).collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cardPadding = if (isSmall) 10.dp else 16.dp
    val itemSpacing = if (isSmall) 6.dp else 12.dp

    Surface(
        color = appContainerColor(),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .appFocusBorder(isFocused = isFocused)
            .focusable(interactionSource = interactionSource)
            .oledBorder()
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Best",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = bestScore.toString(),
                        style = if (isSmall) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        Icons.Rounded.EmojiEvents, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(if (isSmall) 28.dp else 40.dp)
                    )
                    Text(
                        text = "WIN AT ${mode.winCondition}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isSmall) 8.dp else 16.dp))

            Text("TODAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(if (isSmall) 4.dp else 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing)) {
                StatMicroItem(Modifier.weight(1f), "Max Tile", highestTile.toString())
                StatMicroItem(Modifier.weight(1f), "Fewest Moves", if ((fewestMoves == Int.MAX_VALUE) || (fewestMoves == 0)) "-" else fewestMoves.toString())
                StatMicroItem(Modifier.weight(1f), "Fastest Time", if ((fastestTime == Long.MAX_VALUE) || (fastestTime == 0L)) "-" else TimeUtils.formatDurationAbbreviated(fastestTime))
            }
            
            Spacer(modifier = Modifier.height(if (isSmall) 8.dp else 16.dp))
            Text("ALL TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(if (isSmall) 4.dp else 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing)) {
                StatMicroItem(Modifier.weight(1f), "Wins", totalWins.toString())
                StatMicroItem(Modifier.weight(1f), "Played", totalPlayed.toString())
                StatMicroItem(Modifier.weight(1f), "Total Time", TimeUtils.formatDurationAbbreviated(totalTime))
            }
        }
    }
}

@Composable
private fun ModeStatCard(label: String, mode: GameMode, viewModel: GameViewModel) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()
    val bestScore by viewModel.getBestScore(mode).collectAsStateWithLifecycle()
    val highestTile by viewModel.getHighestTile(mode).collectAsStateWithLifecycle()
    val fewestMoves by viewModel.getFewestMoves(mode).collectAsStateWithLifecycle()
    val fastestTime by viewModel.getFastestTime(mode).collectAsStateWithLifecycle()
    val wins by viewModel.getWinCount(mode.groupStatsId).collectAsStateWithLifecycle()
    val played by viewModel.getGamesPlayed(mode.groupStatsId).collectAsStateWithLifecycle()
    val totalTime by viewModel.getTotalTime(mode.groupStatsId).collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cardPadding = if (isSmall) 10.dp else 16.dp
    val itemSpacing = if (isSmall) 6.dp else 12.dp

    Surface(
        color = appContainerColor(),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .appFocusBorder(isFocused = isFocused)
            .focusable(interactionSource = interactionSource)
            .oledBorder()
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(label, style = if (isSmall) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "WIN AT ${mode.winCondition}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = bestScore.toString(),
                    style = if (isSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = if (isSmall) 8.dp else 16.dp, vertical = if (isSmall) 6.dp else 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing)) {
                StatMicroItem(Modifier.weight(1f), "Max Tile", highestTile.toString())
                StatMicroItem(Modifier.weight(1f), "Fewest Moves", if ((fewestMoves == Int.MAX_VALUE) || (fewestMoves == 0)) "-" else fewestMoves.toString())
                StatMicroItem(Modifier.weight(1f), "Fastest Time", if ((fastestTime == Long.MAX_VALUE) || (fastestTime == 0L)) "-" else TimeUtils.formatDurationAbbreviated(fastestTime))
            }
            
            Spacer(modifier = Modifier.height(if (isSmall) 8.dp else 16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing)) {
                StatMicroItem(Modifier.weight(1f), "Wins", wins.toString())
                StatMicroItem(Modifier.weight(1f), "Played", played.toString())
                StatMicroItem(Modifier.weight(1f), "Total Time", TimeUtils.formatDurationAbbreviated(totalTime))
            }
        }
    }
}

@Composable
private fun StatMicroItem(modifier: Modifier = Modifier, label: String, value: String) {
    val isSmall = com.scottmangiapane.open2048.ui.components.DeviceUtils.isSmallScreen()

    Column(modifier = modifier) {
        Text(
            text = label,
            style = if (isSmall) MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp) else MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1
        )
        Text(
            text = value,
            style = if (isSmall) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
