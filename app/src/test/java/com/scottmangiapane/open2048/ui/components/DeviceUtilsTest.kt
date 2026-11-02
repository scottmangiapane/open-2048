package com.scottmangiapane.open2048.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class DeviceUtilsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "w240dp-h320dp")
    fun testSmallScreenDetectionTrue() {
        var isSmallScreen = false
        var isSmallHeight = false
        var isSmallWidth = false

        composeTestRule.setContent {
            isSmallScreen = DeviceUtils.isSmallScreen()
            isSmallHeight = DeviceUtils.isSmallHeight()
            isSmallWidth = DeviceUtils.isSmallWidth()
        }

        assertTrue(isSmallScreen)
        assertTrue(isSmallHeight)
        assertTrue(isSmallWidth)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun testSmallScreenDetectionFalse() {
        var isSmallScreen = false
        var isSmallHeight = false
        var isSmallWidth = false

        composeTestRule.setContent {
            isSmallScreen = DeviceUtils.isSmallScreen()
            isSmallHeight = DeviceUtils.isSmallHeight()
            isSmallWidth = DeviceUtils.isSmallWidth()
        }

        assertFalse(isSmallScreen)
        assertFalse(isSmallHeight)
        assertFalse(isSmallWidth)
    }

    @Test
    fun testHasTouchTrue() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val resources = mockk<android.content.res.Resources>()
        val config = Configuration()
        config.touchscreen = Configuration.TOUCHSCREEN_FINGER

        every { context.packageManager } returns packageManager
        every { context.resources } returns resources
        every { resources.configuration } returns config
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns true

        assertTrue(DeviceUtils.hasTouch(context))
    }

    @Test
    fun testHasTouchFalseFeature() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val resources = mockk<android.content.res.Resources>()
        val config = Configuration()
        config.touchscreen = Configuration.TOUCHSCREEN_FINGER

        every { context.packageManager } returns packageManager
        every { context.resources } returns resources
        every { resources.configuration } returns config
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns false

        assertFalse(DeviceUtils.hasTouch(context))
    }

    @Test
    fun testHasTouchFalseConfig() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val resources = mockk<android.content.res.Resources>()
        val config = Configuration()
        config.touchscreen = Configuration.TOUCHSCREEN_NOTOUCH

        every { context.packageManager } returns packageManager
        every { context.resources } returns resources
        every { resources.configuration } returns config
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN) } returns true

        assertFalse(DeviceUtils.hasTouch(context))
    }

    @Test
    fun testIsGooglePlayTrue() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { context.packageName } returns "com.scottmangiapane.open2048"
        every { packageManager.getInstallerPackageName("com.scottmangiapane.open2048") } returns "com.android.vending"

        assertTrue(DeviceUtils.isGooglePlay(context))
    }

    @Test
    fun testIsGooglePlayFalse() {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { context.packageName } returns "com.scottmangiapane.open2048"
        every { packageManager.getInstallerPackageName("com.scottmangiapane.open2048") } returns "org.fdroid.fdroid"

        assertFalse(DeviceUtils.isGooglePlay(context))
    }
}
