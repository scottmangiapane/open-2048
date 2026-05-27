package com.scottmangiapane.open2048.model

enum class ControlMode {
    GESTURES, ARROWS, BOTH
}

data class UserPreferences(
    val theme: AppTheme = AppTheme.LIGHT,
    val vibrationEnabled: Boolean = false,
    val controlMode: ControlMode = ControlMode.GESTURES,
    val showUndo: Boolean = true,
    val showStopwatch: Boolean = true
)
