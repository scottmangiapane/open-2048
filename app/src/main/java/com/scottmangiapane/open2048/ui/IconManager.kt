package com.scottmangiapane.open2048.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.scottmangiapane.open2048.model.AppTheme

class IconManager(
    private val context: Context,
    private val packageManager: PackageManager = context.packageManager
) {
    
    private var pendingTheme: AppTheme? = null

    fun setPendingIconUpdate(theme: AppTheme) {
        pendingTheme = theme
    }

    fun applyPendingIconChange() {
        val theme = pendingTheme ?: return
        pendingTheme = null

        val lightName = "com.scottmangiapane.open2048.MainActivityLight"
        val darkName = "com.scottmangiapane.open2048.MainActivityDark"
        val classicName = "com.scottmangiapane.open2048.MainActivityClassic"
        val retroName = "com.scottmangiapane.open2048.MainActivityRetro"
        val oledName = "com.scottmangiapane.open2048.MainActivityOled"

        val targetName = when (theme) {
            AppTheme.LIGHT -> lightName
            AppTheme.DARK -> darkName
            AppTheme.CLASSIC -> classicName
            AppTheme.RETRO -> retroName
            AppTheme.OLED -> oledName
        }
        
        val componentNames = listOf(lightName, darkName, classicName, retroName, oledName)

        componentNames.forEach { className ->
            val component = ComponentName(context, className)
            val isTarget = className == targetName
            val newState = if (isTarget) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            
            if (packageManager.getComponentEnabledSetting(component) != newState) {
                packageManager.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }
}
