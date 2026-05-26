package com.scottmangiapane.open2048.model

enum class ControlMode {
    GESTURES, ARROWS, BOTH
}

data class UserPreferences(
    val theme: AppTheme = AppTheme.LIGHT,
    val soundsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val controlMode: ControlMode = ControlMode.BOTH,
    val showUndo: Boolean = true,
    val showStopwatch: Boolean = true
)
