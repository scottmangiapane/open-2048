package com.scottmangiapane.open2048.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scottmangiapane.open2048.model.GameMode
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                ),
                title = {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                ModeStatCard(label = "2 Minute Blitz", mode = GameMode.Blitz(2), viewModel = viewModel)
                ModeStatCard(label = "5 Minute Blitz", mode = GameMode.Blitz(5), viewModel = viewModel)
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )
        }
        content()
    }
}

@Composable
private fun DailyHeroCard(mode: GameMode.Daily, viewModel: GameViewModel) {
    val bestScore by viewModel.getBestScore(mode).collectAsStateWithLifecycle()
    val highestTile by viewModel.getHighestTile(mode).collectAsStateWithLifecycle()
    val fewestMoves by viewModel.getFewestMoves(mode).collectAsStateWithLifecycle()
    val fastestTime by viewModel.getFastestTime(mode).collectAsStateWithLifecycle()
    
    val totalWins by viewModel.getWinCount("daily").collectAsStateWithLifecycle()
    val totalPlayed by viewModel.getGamesPlayed("daily").collectAsStateWithLifecycle()
    val totalTime by viewModel.getTotalTime("daily").collectAsStateWithLifecycle()

    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Best",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = bestScore.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Text(
                        text = "WIN AT ${mode.winCondition}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("TODAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMicroItem(Modifier.weight(1f), "Max Tile", highestTile.toString())
                StatMicroItem(Modifier.weight(1f), "Fewest Moves", if (fewestMoves == Int.MAX_VALUE || fewestMoves == 0) "-" else fewestMoves.toString())
                StatMicroItem(Modifier.weight(1f), "Fastest Time", if (fastestTime == Long.MAX_VALUE || fastestTime == 0L) "-" else formatTimeShort(fastestTime))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("ALL TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMicroItem(Modifier.weight(1f), "Wins", totalWins.toString())
                StatMicroItem(Modifier.weight(1f), "Played", totalPlayed.toString())
                StatMicroItem(Modifier.weight(1f), "Total Time", formatTimeShort(totalTime))
            }
        }
    }
}

@Composable
private fun ModeStatCard(label: String, mode: GameMode, viewModel: GameViewModel) {
    val bestScore by viewModel.getBestScore(mode).collectAsStateWithLifecycle()
    val highestTile by viewModel.getHighestTile(mode).collectAsStateWithLifecycle()
    val fewestMoves by viewModel.getFewestMoves(mode).collectAsStateWithLifecycle()
    val fastestTime by viewModel.getFastestTime(mode).collectAsStateWithLifecycle()
    val wins by viewModel.getWinCount(mode.id).collectAsStateWithLifecycle()
    val played by viewModel.getGamesPlayed(mode.id).collectAsStateWithLifecycle()
    val totalTime by viewModel.getTotalTime(mode.id).collectAsStateWithLifecycle()

    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "WIN AT ${mode.winCondition}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = bestScore.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMicroItem(Modifier.weight(1f), "Max Tile", highestTile.toString())
                StatMicroItem(Modifier.weight(1f), "Fewest Moves", if (fewestMoves == Int.MAX_VALUE || fewestMoves == 0) "-" else fewestMoves.toString())
                StatMicroItem(Modifier.weight(1f), "Fastest Time", if (fastestTime == Long.MAX_VALUE || fastestTime == 0L) "-" else formatTimeShort(fastestTime))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMicroItem(Modifier.weight(1f), "Wins", wins.toString())
                StatMicroItem(Modifier.weight(1f), "Played", played.toString())
                StatMicroItem(Modifier.weight(1f), "Total Time", formatTimeShort(totalTime))
            }
        }
    }
}

@Composable
private fun StatMicroItem(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTimeShort(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    
    val locale = java.util.Locale.getDefault()
    return when {
        hours > 0 -> String.format(locale, "%dh %dm", hours, minutes)
        minutes > 0 -> String.format(locale, "%dm %ds", minutes, seconds)
        else -> String.format(locale, "%ds", seconds)
    }
}
