package com.scottmangiapane.open2048.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.scottmangiapane.open2048.model.AppTheme

class IconManager(private val context: Context) {
    
    private var pendingTheme: AppTheme? = null

    fun setPendingIconUpdate(theme: AppTheme) {
        pendingTheme = theme
    }

    fun applyPendingIconChange() {
        val theme = pendingTheme ?: return
        pendingTheme = null

        val lightComponent = ComponentName(context, "com.scottmangiapane.open2048.MainActivityLight")
        val darkComponent = ComponentName(context, "com.scottmangiapane.open2048.MainActivityDark")
        val classicComponent = ComponentName(context, "com.scottmangiapane.open2048.MainActivityClassic")

        val targetComponent = when (theme) {
            AppTheme.LIGHT -> lightComponent
            AppTheme.DARK -> darkComponent
            AppTheme.CLASSIC -> classicComponent
        }
        
        val components = listOf(lightComponent, darkComponent, classicComponent)

        components.forEach { component ->
            val isTarget = component == targetComponent
            val newState = if (isTarget) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            
            if (context.packageManager.getComponentEnabledSetting(component) != newState) {
                context.packageManager.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }
}
