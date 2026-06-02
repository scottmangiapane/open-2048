package com.scottmangiapane.open2048

import com.scottmangiapane.open2048.model.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    @Test
    fun testActivityThemes() {
        assertEquals(AppTheme.LIGHT, MainActivityLight().activityTheme)
        assertEquals(AppTheme.DARK, MainActivityDark().activityTheme)
        assertEquals(AppTheme.CLASSIC, MainActivityClassic().activityTheme)
    }

    @Test
    fun testMainActivityLaunchSubclasses() {
        val light = Robolectric.buildActivity(MainActivityLight::class.java).setup().get()
        assertEquals(AppTheme.LIGHT, light.activityTheme)
        
        val dark = Robolectric.buildActivity(MainActivityDark::class.java).setup().get()
        assertEquals(AppTheme.DARK, dark.activityTheme)
        
        val classic = Robolectric.buildActivity(MainActivityClassic::class.java).setup().get()
        assertEquals(AppTheme.CLASSIC, classic.activityTheme)
    }

    @Test
    fun testMainActivityLaunch() {
        // Use a themed subclass to avoid complexity of Preferences mock in MainActivity
        val controller = Robolectric.buildActivity(MainActivityLight::class.java)
        controller.setup()
        val activity = controller.get()
        assertEquals(AppTheme.LIGHT, activity.activityTheme)
    }
}
