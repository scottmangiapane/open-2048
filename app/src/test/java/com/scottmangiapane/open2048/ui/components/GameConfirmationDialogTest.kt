package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.assertTrue
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GameConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGameConfirmationDialog() {
        var confirmed = false
        var dismissed = false
        
        composeTestRule.setContent {
            GameConfirmationDialog(
                title = "Test Title",
                message = "Test Message",
                confirmText = "OK",
                onConfirm = { confirmed = true },
                onDismiss = { dismissed = true }
            )
        }

        composeTestRule.onNodeWithText("Test Title").assertExists()
        composeTestRule.onNodeWithText("Test Message").assertExists()
        
        composeTestRule.onNodeWithText("OK").performClick()
        assertTrue(confirmed)
        
        composeTestRule.onNodeWithText("Cancel").performClick()
        assertTrue(dismissed)
    }
}
