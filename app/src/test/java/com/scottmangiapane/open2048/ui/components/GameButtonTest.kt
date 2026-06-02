package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.assertTrue
import org.robolectric.annotation.Config
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GameButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGameButton() {
        var clicked = false
        composeTestRule.setContent {
            GameButton(text = "Test Button", onClick = { clicked = true })
        }

        composeTestRule.onNodeWithText("Test Button").assertExists()
        composeTestRule.onNodeWithText("Test Button").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testGameButtonBranches() {
        composeTestRule.setContent {
            MaterialTheme {
                // isTextButton = true
                GameButton(text = "Text Button", onClick = {}, isTextButton = true)
                // fullWidth = true, icon != null
                GameButton(text = "Full Width", onClick = {}, fullWidth = true, icon = Icons.Default.Add)
                // disabled
                GameButton(text = "Disabled", onClick = {}, enabled = false)
                
                // containerColor == primary branch in focus border
                GameButton(text = "Primary", onClick = {}, containerColor = MaterialTheme.colorScheme.primary)
                // containerColor != primary
                GameButton(text = "Secondary", onClick = {}, containerColor = Color.Red)
            }
        }
        composeTestRule.onNodeWithText("Text Button").assertExists()
        composeTestRule.onNodeWithText("Full Width").assertExists()
        composeTestRule.onNodeWithText("Disabled").assertExists()
    }

    @Test
    fun testSelectableButton() {
        var clicked = false
        composeTestRule.setContent {
            SelectableButton(text = "Selectable", selected = true, onClick = { clicked = true })
        }

        composeTestRule.onNodeWithText("Selectable").assertExists()
        composeTestRule.onNodeWithText("Selectable").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testSelectableButtonUnselected() {
        composeTestRule.setContent {
            SelectableButton(text = "Unselected", selected = false, onClick = { })
        }
        composeTestRule.onNodeWithText("Unselected").assertExists()
    }

    @Test
    fun testGameButtonIconNoFullWidth() {
        composeTestRule.setContent {
            GameButton(text = "Icon Button", onClick = { }, icon = Icons.Default.Add, fullWidth = false)
        }
        composeTestRule.onNodeWithText("Icon Button").assertExists()
    }

    @Test
    fun testControlButton() {
        var clicked = false
        composeTestRule.setContent {
            ControlButton(icon = Icons.Default.Add, onClick = { clicked = true })
        }
        composeTestRule.onNode(hasClickAction()).performClick()
        assertTrue(clicked)
    }
}
