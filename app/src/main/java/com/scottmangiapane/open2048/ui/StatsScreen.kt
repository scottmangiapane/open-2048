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
import androidx.compose.ui.graphics.Color
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
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color.Transparent // Parent Surface in MainActivity handles this
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
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

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Best", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(bestScore.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                }
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatMicroItem(Modifier.weight(1f), "Highest Tile", highestTile.toString())
                StatMicroItem(Modifier.weight(1f), "Fewest Moves", if (fewestMoves == Int.MAX_VALUE) "-" else fewestMoves.toString())
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatMicroItem(Modifier.weight(1f), "Fastest Time", if (fastestTime == Long.MAX_VALUE) "-" else formatTimeShort(fastestTime))
                StatMicroItem(Modifier.weight(1f), "Total Wins", totalWins.toString())
            }
        }
    }
}

@Composable
private fun ModeStatCard(label: String, mode: GameMode, viewModel: GameViewModel) {
    val bestScore by viewModel.getBestScore(mode).collectAsStateWithLifecycle()
    val highestTile by viewModel.getHighestTile(mode).collectAsStateWithLifecycle()
    val played by viewModel.getGamesPlayed(mode.id).collectAsStateWithLifecycle()
    val totalTime by viewModel.getTotalTime(mode.id).collectAsStateWithLifecycle()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = bestScore.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMicroItem(Modifier.weight(1f), "Max Tile", highestTile.toString())
                StatMicroItem(Modifier.weight(1f), "Played", played.toString())
                StatMicroItem(Modifier.weight(1f), "Total Time", formatTimeShort(totalTime))
            }
        }
    }
}

@Composable
private fun StatMicroItem(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
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
