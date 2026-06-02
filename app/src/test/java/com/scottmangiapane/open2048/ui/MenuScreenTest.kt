package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MenuScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMenuScreenBasicUi() {
        var startClassicCalled = false
        var resumeCalled = false
        composeTestRule.setContent {
            MenuScreen(
                onStartGame = { startClassicCalled = true },
                onResumeGame = { resumeCalled = true },
                onNavigateToStats = {},
                onNavigateToSettings = {},
                canResume = true,
                hasProgress = false
            )
        }
        composeTestRule.onNodeWithText("Resume").assertExists()
        composeTestRule.onNodeWithText("Classic 4x4").performClick()
        assertTrue(startClassicCalled)
        
        composeTestRule.onNodeWithText("Resume").performClick()
        assertTrue(resumeCalled)
    }

    @Test
    fun testMenuScreenLandscape() {
        val config = android.content.res.Configuration()
        config.orientation = android.content.res.Configuration.ORIENTATION_LANDSCAPE
        
        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalConfiguration provides config
            ) {
                MenuScreen(
                    onStartGame = {},
                    onResumeGame = {},
                    onNavigateToStats = {},
                    onNavigateToSettings = {},
                    canResume = true,
                    hasProgress = false
                )
            }
        }
        composeTestRule.onNodeWithText("CLASSIC").assertExists()
        composeTestRule.onNodeWithText("BLITZ").assertExists()
    }

    @Test
    fun testMenuScreenStartGameWithProgressConfirmation() {
        var startClassicCalled = false
        composeTestRule.setContent {
            MenuScreen(
                onStartGame = { startClassicCalled = true },
                onResumeGame = {},
                onNavigateToStats = {},
                onNavigateToSettings = {},
                canResume = true,
                hasProgress = true
            )
        }
        
        composeTestRule.onNodeWithText("Classic 4x4").performClick()
        composeTestRule.onNodeWithText("Start New Game?").assertExists()
        
        composeTestRule.onNodeWithText("Start New Game").performClick()
        assertTrue(startClassicCalled)
    }

    @Test
    fun testMenuScreenNavigationIcons() {
        var statsCalled = false
        var settingsCalled = false
        composeTestRule.setContent {
            MenuScreen(
                onStartGame = {},
                onResumeGame = {},
                onNavigateToStats = { statsCalled = true },
                onNavigateToSettings = { settingsCalled = true },
                canResume = false,
                hasProgress = false
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Statistics").performClick()
        assertTrue(statsCalled)
        
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        assertTrue(settingsCalled)
    }

    @Test
    fun testMenuScreenClassicModes() {
        var modeSelectedCount = 0
        composeTestRule.setContent {
            MenuScreen(
                onStartGame = { modeSelectedCount++ },
                onResumeGame = {},
                onNavigateToStats = {},
                onNavigateToSettings = {},
                canResume = false,
                hasProgress = false
            )
        }
        
        composeTestRule.onNodeWithText("Small 3x3").performClick()
        composeTestRule.onNodeWithText("Large 5x5").performClick()
        assertTrue(modeSelectedCount == 2)
    }

    @Test
    fun testMenuScreenBlitzModes() {
        var modeSelectedCount = 0
        composeTestRule.setContent {
            MenuScreen(
                onStartGame = { modeSelectedCount++ },
                onResumeGame = {},
                onNavigateToStats = {},
                onNavigateToSettings = {},
                canResume = false,
                hasProgress = false
            )
        }
        
        composeTestRule.onNodeWithText("2 Minute Blitz").performScrollTo().performClick()
        composeTestRule.onNodeWithText("5 Minute Blitz").performScrollTo().performClick()
        assertTrue(modeSelectedCount == 2)
    }
}
