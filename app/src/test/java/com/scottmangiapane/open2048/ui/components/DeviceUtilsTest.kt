package com.scottmangiapane.open2048.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceUtilsTest {

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
}
