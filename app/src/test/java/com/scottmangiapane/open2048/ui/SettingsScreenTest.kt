package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.*
import io.mockk.every
import io.mockk.mockk
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

    private fun mockViewModel(): GameViewModel {
        val vm = mockk<GameViewModel>(relaxed = true)
        every { vm.userPreferences } returns MutableStateFlow(UserPreferences())
        every { vm.hasTouch } returns true
        return vm
    }

    @Test
    fun testSettingsScreenBasicUi() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("APPEARANCE").assertExists()
        
        // Test a toggle
        composeTestRule.onNodeWithText("Show Stopwatch").performClick()
        
        // Test radio button
        composeTestRule.onNodeWithText("Swipe Gestures").performClick()
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
