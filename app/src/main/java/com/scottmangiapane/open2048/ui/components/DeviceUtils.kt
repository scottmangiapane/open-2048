package com.scottmangiapane.open2048.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

object DeviceUtils {
    /**
     * Returns true if the device has a touchscreen.
     * We check both the system feature and the actual hardware configuration
     * to avoid "fake touch" reporting on some emulators and TVs.
     */
    fun hasTouch(context: Context): Boolean {
        val hasFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        val hasConfig = context.resources.configuration.touchscreen != Configuration.TOUCHSCREEN_NOTOUCH
        return hasFeature && hasConfig
    }
}
