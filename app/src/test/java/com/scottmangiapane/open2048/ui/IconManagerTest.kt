package com.scottmangiapane.open2048.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.scottmangiapane.open2048.model.AppTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IconManagerTest {

    @Test
    fun testApplyPendingIconChangeSpecifics() {
        val context = mockk<Context>(relaxed = true)
        val packageManager = mockk<PackageManager>(relaxed = true)
        
        every { context.packageName } returns "com.scottmangiapane.open2048"
        // Initially everything is default
        every { packageManager.getComponentEnabledSetting(any()) } returns PackageManager.COMPONENT_ENABLED_STATE_DEFAULT

        val iconManager = IconManager(context, packageManager)
        
        // Test switching to CLASSIC
        iconManager.setPendingIconUpdate(AppTheme.CLASSIC)
        iconManager.applyPendingIconChange()

        verify { 
            packageManager.setComponentEnabledSetting(
                match { it.className == "com.scottmangiapane.open2048.MainActivityClassic" },
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        verify { 
            packageManager.setComponentEnabledSetting(
                match { it.className == "com.scottmangiapane.open2048.MainActivityLight" },
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
        verify { 
            packageManager.setComponentEnabledSetting(
                match { it.className == "com.scottmangiapane.open2048.MainActivityDark" },
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    @Test
    fun testApplyPendingIconChangeLight() {
        val context = mockk<Context>(relaxed = true)
        val packageManager = mockk<PackageManager>(relaxed = true)
        every { context.packageName } returns "com.scottmangiapane.open2048"
        every { packageManager.getComponentEnabledSetting(any()) } returns PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        val iconManager = IconManager(context, packageManager)
        
        iconManager.setPendingIconUpdate(AppTheme.LIGHT)
        iconManager.applyPendingIconChange()
        
        verify { 
            packageManager.setComponentEnabledSetting(
                match { it.className == "com.scottmangiapane.open2048.MainActivityLight" },
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                any()
            )
        }
    }

    @Test
    fun testApplyPendingIconChangeDark() {
        val context = mockk<Context>(relaxed = true)
        val packageManager = mockk<PackageManager>(relaxed = true)
        every { context.packageName } returns "com.scottmangiapane.open2048"
        every { packageManager.getComponentEnabledSetting(any()) } returns PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        val iconManager = IconManager(context, packageManager)
        
        iconManager.setPendingIconUpdate(AppTheme.DARK)
        iconManager.applyPendingIconChange()
        
        verify { 
            packageManager.setComponentEnabledSetting(
                match { it.className == "com.scottmangiapane.open2048.MainActivityDark" },
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                any()
            )
        }
    }

    @Test
    fun testApplyPendingIconChangeNoPending() {
        val packageManager = mockk<PackageManager>(relaxed = true)
        val iconManager = IconManager(mockk(relaxed = true), packageManager)
        
        iconManager.applyPendingIconChange()
        verify(exactly = 0) { packageManager.setComponentEnabledSetting(any(), any(), any()) }
    }

    @Test
    fun testApplyPendingIconChangeAlreadyInState() {
        val context = mockk<Context>(relaxed = true)
        val packageManager = mockk<PackageManager>(relaxed = true)
        every { context.packageName } returns "com.scottmangiapane.open2048"
        
        // Classic is already enabled
        every { packageManager.getComponentEnabledSetting(match { it.className.endsWith("Classic") }) } returns PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        // Light and Dark are already disabled
        every { packageManager.getComponentEnabledSetting(match { it.className.endsWith("Light") }) } returns PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        every { packageManager.getComponentEnabledSetting(match { it.className.endsWith("Dark") }) } returns PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        val iconManager = IconManager(context, packageManager)
        
        iconManager.setPendingIconUpdate(AppTheme.CLASSIC)
        iconManager.applyPendingIconChange()

        verify(exactly = 0) { packageManager.setComponentEnabledSetting(any(), any(), any()) }
    }
}
