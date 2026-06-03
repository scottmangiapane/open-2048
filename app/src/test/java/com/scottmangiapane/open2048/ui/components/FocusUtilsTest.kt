package com.scottmangiapane.open2048.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.ui.theme.Open2048Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FocusUtilsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppFocusBorder() {
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .appFocusBorder(isFocused = true)
            )
        }
    }

    @Test
    fun testAppContainerColor() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.OLED) {
                val color = appContainerColor()
                assertEquals(Color.Transparent, color)
            }
            Open2048Theme(theme = AppTheme.LIGHT) {
                val color = appContainerColor()
                assertTrue(color != Color.Transparent)
            }
        }
    }

    @Test
    fun testOledBorder() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.OLED) {
                Box(Modifier.oledBorder())
            }
            Open2048Theme(theme = AppTheme.LIGHT) {
                Box(Modifier.oledBorder())
            }
        }
    }

    @Test
    fun testMenuIconButton() {
        var clicked = false
        composeTestRule.setContent {
            MenuIconButton(onClick = { clicked = true }) {
                Box(Modifier.size(20.dp))
            }
        }
        composeTestRule.onNode(hasClickAction()).performClick()
        assertTrue(clicked)
    }
}
