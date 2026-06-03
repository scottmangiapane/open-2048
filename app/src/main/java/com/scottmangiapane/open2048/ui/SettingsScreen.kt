package com.scottmangiapane.open2048.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scottmangiapane.open2048.model.AnimationSpeed
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.ControlMode
import com.scottmangiapane.open2048.ui.components.SelectableButton
import com.scottmangiapane.open2048.ui.components.appFocusBorder

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
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal),
                ),
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    IconButton(
                        onClick = onBack,
                        interactionSource = interactionSource,
                        modifier = Modifier.appFocusBorder(isFocused = isFocused),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            SettingsGroup(title = "APPEARANCE") {
                ThemeSelector(
                    currentTheme = preferences.theme,
                ) { viewModel.setTheme(it) }
            }

            // Animation Section
            SettingsGroup(title = "ANIMATION SPEED") {
                AnimationSpeedSelector(
                    currentSpeed = preferences.animationSpeed,
                ) { viewModel.setAnimationSpeed(it) }
            }

            // Controls Section
            if (viewModel.hasTouch) {
                SettingsGroup(title = "CONTROLS") {
                    ControlModeSelector(
                        currentMode = preferences.controlMode,
                    ) { viewModel.setControlMode(it) }

                    if (preferences.controlMode != ControlMode.ARROWS) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            ToggleRow(
                                label = "Full Screen Gestures",
                                icon = Icons.Rounded.Fullscreen,
                                checked = preferences.fullScreenGestures,
                            ) { viewModel.setFullScreenGestures(it) }
                        }
                    }
                }
            }

            // Gameplay Section
            SettingsGroup(title = "GAMEPLAY") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    ToggleRow(
                        label = "Show Stopwatch",
                        icon = Icons.Rounded.Timer,
                        checked = preferences.showStopwatch,
                    ) { viewModel.setShowStopwatch(it) }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    ToggleRow(
                        label = "Show Undo Button",
                        icon = Icons.Rounded.History,
                        checked = preferences.showUndo,
                    ) { viewModel.setShowUndo(it) }
                    if (viewModel.hasVibrator) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        ToggleRow(
                            label = "Haptic Feedback",
                            icon = Icons.Rounded.Vibration,
                            checked = preferences.vibrationEnabled,
                        ) { viewModel.setVibrationEnabled(it) }
                    }
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
            color = MaterialTheme.colorScheme.primary,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppTheme.entries.forEach { theme ->
            SelectableButton(
                text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                selected = currentTheme == theme,
                onClick = { onThemeChange(theme) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnimationSpeedSelector(
    currentSpeed: AnimationSpeed,
    onSpeedChange: (AnimationSpeed) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimationSpeed.entries.forEach { speed ->
                SelectableButton(
                    text = speed.label,
                    selected = currentSpeed == speed,
                    onClick = { onSpeedChange(speed) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

@Composable
private fun ControlModeSelector(
    currentMode: ControlMode,
    onModeChange: (ControlMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ControlMode.entries.forEach { mode ->
            SelectableButton(
                text = when (mode) {
                    ControlMode.GESTURES -> "Gestures"
                    ControlMode.ARROWS -> "Arrows"
                    ControlMode.BOTH -> "Both"
                },
                selected = currentMode == mode,
                onClick = { onModeChange(mode) },
                modifier = Modifier.weight(1f)
            )
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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appFocusBorder(isFocused = isFocused)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
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
