package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.ui.theme.Open2048Theme

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SupportSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSupportSectionLight() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.LIGHT) {
                SupportSection()
            }
        }
        composeTestRule.onNode(hasClickAction()).assertExists()
    }

    @Test
    fun testSupportSectionOLED() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.OLED) {
                SupportSection()
            }
        }
        composeTestRule.onNode(hasClickAction()).assertExists()
    }

    @Test
    fun testSupportSectionClick() {
        composeTestRule.setContent {
            SupportSection()
        }
        composeTestRule.onNode(hasClickAction()).performClick()
    }
}
