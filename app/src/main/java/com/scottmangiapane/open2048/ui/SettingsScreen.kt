package com.scottmangiapane.open2048.ui

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
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.ControlMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                ),
                title = {
                    Text(
                        text = "Settings",
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
        containerColor = Color.Transparent
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
            // Appearance Section
            SettingsGroup(title = "APPEARANCE") {
                ThemeSelector(
                    currentTheme = preferences.theme,
                    onThemeChange = { viewModel.setTheme(it) }
                )
            }

            // Controls Section
            SettingsGroup(title = "CONTROLS") {
                ControlModeSelector(
                    currentMode = preferences.controlMode,
                    onModeChange = { viewModel.setControlMode(it) }
                )
            }

            // Gameplay Section
            SettingsGroup(title = "GAMEPLAY") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    ToggleRow(
                        label = "Show Stopwatch",
                        icon = Icons.Rounded.Timer,
                        checked = preferences.showStopwatch,
                        onCheckedChange = { viewModel.setShowStopwatch(it) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    ToggleRow(
                        label = "Show Undo Button",
                        icon = Icons.Rounded.History,
                        checked = preferences.showUndo,
                        onCheckedChange = { viewModel.setShowUndo(it) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    ToggleRow(
                        label = "Confetti Effects",
                        icon = Icons.Rounded.Celebration,
                        checked = preferences.confettiEnabled,
                        onCheckedChange = { viewModel.setConfettiEnabled(it) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    ToggleRow(
                        label = "Haptic Feedback",
                        icon = Icons.Rounded.Vibration,
                        checked = preferences.vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppTheme.entries.forEach { theme ->
            val selected = currentTheme == theme
            Surface(
                onClick = { onThemeChange(theme) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlModeSelector(
    currentMode: ControlMode,
    onModeChange: (ControlMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        ControlMode.entries.forEachIndexed { index, mode ->
            val selected = currentMode == mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModeChange(mode) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = { onModeChange(mode) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when (mode) {
                        ControlMode.GESTURES -> "Swipe Gestures"
                        ControlMode.ARROWS -> "On-Screen Arrows"
                        ControlMode.BOTH -> "Gestures & Arrows"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
            if (index < ControlMode.entries.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
