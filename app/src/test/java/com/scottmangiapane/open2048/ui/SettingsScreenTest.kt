package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.assertTrue

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
    fun testSettingsScreenAppearance() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        composeTestRule.onNodeWithText("Dark").performScrollTo().performClick()
        verify { viewModel.setTheme(AppTheme.DARK) }
        
        composeTestRule.onNodeWithText("Classic").performScrollTo().performClick()
        verify { viewModel.setTheme(AppTheme.CLASSIC) }
    }

    @Test
    fun testSettingsScreenAnimationSpeed() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        composeTestRule.onNodeWithText("Fast").performScrollTo().performClick()
        verify { viewModel.setAnimationSpeed(AnimationSpeed.FAST) }
        
        composeTestRule.onNodeWithText("Off").performScrollTo().performClick()
        verify { viewModel.setAnimationSpeed(AnimationSpeed.NONE) }
    }

    @Test
    fun testSettingsScreenControls() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        // Change Control Mode
        composeTestRule.onNodeWithText("Arrows").performClick()
        verify { viewModel.setControlMode(ControlMode.ARROWS) }
        
        // Full Screen Gestures toggle should disappear when Arrows is selected
        // (But since we're using a mock, the UI won't recompose automatically unless we update the flow)
    }

    @Test
    fun testSettingsScreenControlsFullScreenToggle() {
        val prefs = MutableStateFlow(UserPreferences(controlMode = ControlMode.GESTURES))
        val viewModel = mockk<GameViewModel>(relaxed = true)
        every { viewModel.userPreferences } returns prefs
        every { viewModel.hasTouch } returns true
        
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        composeTestRule.onNodeWithText("Full Screen Gestures").assertExists()
        composeTestRule.onNodeWithText("Full Screen Gestures").performClick()
        verify { viewModel.setFullScreenGestures(any()) }
        
        // Update prefs to ARROWS
        prefs.value = UserPreferences(controlMode = ControlMode.ARROWS)
        composeTestRule.onNodeWithText("Full Screen Gestures").assertDoesNotExist()
    }

    @Test
    fun testSettingsScreenGameplayToggles() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        composeTestRule.onNodeWithText("Show Stopwatch").performScrollTo().performClick()
        verify { viewModel.setShowStopwatch(any()) }
        
        composeTestRule.onNodeWithText("Show Undo Button").performScrollTo().performClick()
        verify { viewModel.setShowUndo(any()) }
        
        composeTestRule.onNodeWithText("Haptic Feedback").performScrollTo().performClick()
        verify { viewModel.setVibrationEnabled(any()) }
    }

    @Test
    fun testSettingsScreenSupportSection() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(viewModel = viewModel, onBack = {})
        }
        
        composeTestRule.onNodeWithText("SUPPORT").assertExists()
        // We don't test the specific SupportSection content here as it's flavor-dependent,
        // but we verify the group is present.
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
