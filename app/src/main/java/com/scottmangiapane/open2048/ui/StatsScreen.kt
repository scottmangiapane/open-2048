package com.scottmangiapane.open2048.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scottmangiapane.open2048.model.GameMode
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Parent Surface handles background
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                ),
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                )
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StatsCategory("CHALLENGE", Icons.Rounded.EmojiEvents) {
                val today = GameMode.Daily.today()
                DailyModeStats("Daily Challenge", today, viewModel)
            }

            StatsCategory("CLASSIC", Icons.Rounded.Grid4x4) {
                ModeStats("Classic 4x4", GameMode.Classic(4), viewModel)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ModeStats("Small 3x3", GameMode.Classic(3), viewModel)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ModeStats("Large 5x5", GameMode.Classic(5), viewModel)
            }

            StatsCategory("BLITZ", Icons.Rounded.Timer) {
                ModeStats("2 Minute Blitz", GameMode.Blitz(2), viewModel)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ModeStats("5 Minute Blitz", GameMode.Blitz(5), viewModel)
            }
        }
    }
}

@Composable
private fun StatsCategory(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun ModeStats(label: String, mode: GameMode, viewModel: GameViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val bestScore by viewModel.getBestScore(mode).collectAsStateWithLifecycle()
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Best: $bestScore",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val highestTile by viewModel.getHighestTile(mode).collectAsStateWithLifecycle()
                val fewestMoves by viewModel.getFewestMoves(mode).collectAsStateWithLifecycle()
                val fastestTime by viewModel.getFastestTime(mode).collectAsStateWithLifecycle()
                val wins by viewModel.getWinCount(mode.id).collectAsStateWithLifecycle()
                val played by viewModel.getGamesPlayed(mode.id).collectAsStateWithLifecycle()
                val totalTime by viewModel.getTotalTime(mode.id).collectAsStateWithLifecycle()

                StatDetailRow("Highest Tile", highestTile.toString())
                StatDetailRow("Fewest Moves to 2048", if (fewestMoves == Int.MAX_VALUE) "-" else fewestMoves.toString())
                StatDetailRow("Fastest Time to 2048", if (fastestTime == Long.MAX_VALUE) "-" else formatTime(fastestTime))
                StatDetailRow("Wins (2048 reached)", wins.toString())
                StatDetailRow("Games Played", played.toString())
                StatDetailRow("Total Time Spent", formatTime(totalTime))
            }
        }
    }
}

@Composable
private fun DailyModeStats(label: String, mode: GameMode.Daily, viewModel: GameViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val bestScore by viewModel.getBestScore(mode).collectAsStateWithLifecycle()
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Today's Best: $bestScore",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Today's stats
                val highestTile by viewModel.getHighestTile(mode).collectAsStateWithLifecycle()
                val fewestMoves by viewModel.getFewestMoves(mode).collectAsStateWithLifecycle()
                val fastestTime by viewModel.getFastestTime(mode).collectAsStateWithLifecycle()
                
                // All-time daily stats
                val wins by viewModel.getWinCount("daily").collectAsStateWithLifecycle()
                val played by viewModel.getGamesPlayed("daily").collectAsStateWithLifecycle()
                val totalTime by viewModel.getTotalTime("daily").collectAsStateWithLifecycle()

                Text("Today", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                StatDetailRow("Highest Tile Today", highestTile.toString())
                StatDetailRow("Fewest Moves to 2048", if (fewestMoves == Int.MAX_VALUE) "-" else fewestMoves.toString())
                StatDetailRow("Fastest Time to 2048", if (fastestTime == Long.MAX_VALUE) "-" else formatTime(fastestTime))
                
                Spacer(modifier = Modifier.height(4.dp))
                Text("All Time", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                StatDetailRow("Total Wins", wins.toString())
                StatDetailRow("Total Games Played", played.toString())
                StatDetailRow("Total Time Spent", formatTime(totalTime))
            }
        }
    }
}

@Composable
private fun StatDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatTime(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    
    val locale = java.util.Locale.getDefault()
    return when {
        hours > 0 -> String.format(locale, "%dh %dm %ds", hours, minutes, seconds)
        minutes > 0 -> String.format(locale, "%dm %ds", minutes, seconds)
        else -> String.format(locale, "%ds", seconds)
    }
}
