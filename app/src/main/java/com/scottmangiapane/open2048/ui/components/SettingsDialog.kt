package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.ControlMode
import com.scottmangiapane.open2048.model.UserPreferences
import java.util.Locale

@Composable
fun SettingsDialog(
    preferences: UserPreferences,
    onDismiss: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onSoundsToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onControlModeChange: (ControlMode) -> Unit,
    onShowUndoToggle: (Boolean) -> Unit,
    onShowStopwatchToggle: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Theme
                SettingSection("Theme")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = preferences.theme == theme
                        FilterChip(
                            selected = isSelected,
                            onClick = { onThemeChange(theme) },
                            label = { 
                                Text(
                                    text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                // Controls
                SettingSection("Control Mode")
                Column {
                    // Custom order: Arrows, Gestures, Both
                    listOf(ControlMode.ARROWS, ControlMode.GESTURES, ControlMode.BOTH).forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onControlModeChange(mode) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = preferences.controlMode == mode,
                                onClick = { onControlModeChange(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = mode.name.lowercase().replaceFirstChar { it.uppercase(Locale.getDefault()) },
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Toggles
                ToggleSetting("Sounds", preferences.soundsEnabled, onSoundsToggle)
                ToggleSetting("Vibration", preferences.vibrationEnabled, onVibrationToggle)
                ToggleSetting("Show Stopwatch", preferences.showStopwatch, onShowStopwatchToggle)
                ToggleSetting("Show Undo Button", preferences.showUndo, onShowUndoToggle)

                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun SettingSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ToggleSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}
