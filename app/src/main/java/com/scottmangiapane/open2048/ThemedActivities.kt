package com.scottmangiapane.open2048

import com.scottmangiapane.open2048.model.AppTheme

class MainActivityLight : MainActivity() {
    override val activityTheme: AppTheme = AppTheme.LIGHT
}

class MainActivityDark : MainActivity() {
    override val activityTheme: AppTheme = AppTheme.DARK
}

class MainActivityClassic : MainActivity() {
    override val activityTheme: AppTheme = AppTheme.CLASSIC
}
