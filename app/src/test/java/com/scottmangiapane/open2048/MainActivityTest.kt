package com.scottmangiapane.open2048

import com.scottmangiapane.open2048.model.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivityLight>()

    @Test
    fun testActivityThemes() {
        assertEquals(AppTheme.LIGHT, MainActivityLight().activityTheme)
        assertEquals(AppTheme.DARK, MainActivityDark().activityTheme)
        assertEquals(AppTheme.CLASSIC, MainActivityClassic().activityTheme)
        assertEquals(AppTheme.OLED, MainActivityOLED().activityTheme)
    }

    @Test
    fun testMainActivityNavigationFlow() {
        // Start at Menu
        composeTestRule.onNodeWithText("Classic 4x4").assertExists()
        
        // Go to Stats
        composeTestRule.onNodeWithContentDescription("Statistics").performClick()
        composeTestRule.onNodeWithText("Statistics").assertExists()
        
        // Go back to Menu
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("Classic 4x4").assertExists()
        
        // Go to Settings
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertExists()
        
        // Go back to Menu
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Start a Game
        composeTestRule.onNodeWithText("Classic 4x4").performClick()
        composeTestRule.waitForIdle()
        
        // Header in Game says "2048"
        composeTestRule.onNodeWithText("2048").assertExists()
        
        // Go back to Menu
        composeTestRule.onNodeWithContentDescription("Back to Menu").performClick()
        composeTestRule.waitForIdle()
        
        // Verify we are back on Menu
        composeTestRule.onNodeWithText("Classic 4x4").assertExists()
    }
}
