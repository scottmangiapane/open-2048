package com.scottmangiapane.open2048.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

object DeviceUtils {
    /**
     * Returns true if the screen height is constrained (< 520 dp).
     */
    @Composable
    fun isSmallHeight(): Boolean {
        return LocalConfiguration.current.screenHeightDp < 520
    }

    /**
     * Returns true if the screen width is constrained (< 360 dp).
     */
    @Composable
    fun isSmallWidth(): Boolean {
        return LocalConfiguration.current.screenWidthDp < 360
    }

    /**
     * Returns true if the screen is considered ultra-small (e.g., 2-inch flip phones, cover displays).
     */
    @Composable
    fun isSmallScreen(): Boolean {
        return isSmallHeight() || isSmallWidth()
    }

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

    /**
     * Returns true if the app was installed from the Google Play Store.
     */
    fun isGooglePlay(context: Context): Boolean {
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        return installer == "com.android.vending"
    }
}
