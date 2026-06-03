package com.scottmangiapane.open2048.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.ui.theme.Open2048Theme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
                GameButton(text = "Text Button", onClick = {}, isTextButton = true)
                GameButton(text = "Full Width", onClick = {}, fullWidth = true, icon = Icons.Default.Add)
                GameButton(text = "Disabled", onClick = {}, enabled = false)
            }
        }
        composeTestRule.onNodeWithText("Text Button").assertExists()
        composeTestRule.onNodeWithText("Full Width").assertExists()
        composeTestRule.onNodeWithText("Disabled").assertExists()
    }

    @Test
    fun testGameButtonOLED() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.OLED) {
                GameButton(text = "OLED Button", onClick = {}, containerColor = Color.Black)
                SelectableButton(text = "OLED Selectable", selected = true, onClick = {})
                SelectableButton(text = "OLED Unselected", selected = false, onClick = {})
                ControlButton(icon = Icons.Default.Add, onClick = {})
            }
        }
        composeTestRule.onNodeWithText("OLED Button").assertExists()
        composeTestRule.onNodeWithText("OLED Selectable").assertExists()
        composeTestRule.onNodeWithText("OLED Unselected").assertExists()
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
    fun testControlButton() {
        var clicked = false
        composeTestRule.setContent {
            ControlButton(icon = Icons.Default.Add, onClick = { clicked = true })
        }
        composeTestRule.onNode(hasClickAction()).performClick()
        assertTrue(clicked)
    }
}
