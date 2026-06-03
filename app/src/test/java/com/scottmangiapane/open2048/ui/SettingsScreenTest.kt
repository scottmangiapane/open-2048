package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.*
import com.scottmangiapane.open2048.ui.theme.Open2048Theme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun mockViewModel(preferences: UserPreferences = UserPreferences()): GameViewModel {
        val vm = mockk<GameViewModel>(relaxed = true)
        every { vm.userPreferences } returns MutableStateFlow(preferences)
        every { vm.hasTouch } returns true
        every { vm.hasVibrator } returns true
        return vm
    }

    @Test
    fun testSettingsScreenInteractions() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        // Theme selection
        composeTestRule.onNodeWithText("Dark").performScrollTo().performClick()
        verify { viewModel.setTheme(AppTheme.DARK) }
        
        composeTestRule.onNodeWithText("Classic").performScrollTo().performClick()
        verify { viewModel.setTheme(AppTheme.CLASSIC) }

        // Animation speed
        composeTestRule.onNodeWithText("Fast").performScrollTo().performClick()
        verify { viewModel.setAnimationSpeed(AnimationSpeed.FAST) }
        
        composeTestRule.onNodeWithText("Normal").performScrollTo().performClick()
        verify { viewModel.setAnimationSpeed(AnimationSpeed.NORMAL) }
        
        composeTestRule.onNodeWithText("Slow").performScrollTo().performClick()
        verify { viewModel.setAnimationSpeed(AnimationSpeed.SLOW) }

        // Control mode
        composeTestRule.onNodeWithText("Arrows").performScrollTo().performClick()
        verify { viewModel.setControlMode(ControlMode.ARROWS) }
        
        composeTestRule.onNodeWithText("Gestures").performScrollTo().performClick()
        verify { viewModel.setControlMode(ControlMode.GESTURES) }
        
        composeTestRule.onNodeWithText("Both").performScrollTo().performClick()
        verify { viewModel.setControlMode(ControlMode.BOTH) }
    }

    @Test
    fun testSettingsScreenAllToggles() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        // Show Stopwatch
        composeTestRule.onNodeWithText("Show Stopwatch").performScrollTo().performClick()
        verify { viewModel.setShowStopwatch(any()) }

        // Show Undo
        composeTestRule.onNodeWithText("Show Undo Button").performScrollTo().performClick()
        verify { viewModel.setShowUndo(any()) }

        // Haptic
        composeTestRule.onNodeWithText("Haptic Feedback").performScrollTo().performClick()
        verify { viewModel.setVibrationEnabled(any()) }
        
        // Full Screen Gestures (only if not ARROWS)
        composeTestRule.onNodeWithText("Full Screen Gestures").performScrollTo().performClick()
        verify { viewModel.setFullScreenGestures(any()) }
    }

    @Test
    fun testSettingsScreenTogglesAdvanced() {
        val prefs = MutableStateFlow(UserPreferences(
            showStopwatch = true,
            showUndo = true,
            vibrationEnabled = true
        ))
        val viewModel = mockk<GameViewModel>(relaxed = true)
        every { viewModel.userPreferences } returns prefs
        every { viewModel.hasTouch } returns true
        every { viewModel.hasVibrator } returns true

        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }

        // Toggle them all off
        composeTestRule.onNodeWithText("Show Stopwatch").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Show Undo Button").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Haptic Feedback").performScrollTo().performClick()
    }

    @Test
    fun testSettingsScreenOLED() {
        val viewModel = mockViewModel(UserPreferences())
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.OLED) {
                SettingsScreen(viewModel = viewModel, onBack = {})
            }
        }
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun testSettingsScreenNavigation() {
        var backCalled = false
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { backCalled = true }
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backCalled)
    }
}
