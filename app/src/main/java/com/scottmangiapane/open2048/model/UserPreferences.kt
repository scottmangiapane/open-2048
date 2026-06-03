package com.scottmangiapane.open2048.model

enum class ControlMode {
    GESTURES, ARROWS, BOTH
}

enum class AnimationSpeed(val label: String) {
    NONE("Off"),
    SLOW("Slow"),
    NORMAL("Normal"),
    FAST("Fast")
}

data class UserPreferences(
    val theme: AppTheme = AppTheme.LIGHT,
    val vibrationEnabled: Boolean = true,
    val controlMode: ControlMode = ControlMode.GESTURES,
    val fullScreenGestures: Boolean = true,
    val showUndo: Boolean = true,
    val showStopwatch: Boolean = false,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL
)
