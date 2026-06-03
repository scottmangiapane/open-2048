package com.scottmangiapane.open2048.ui.theme

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.scottmangiapane.open2048.model.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOpen2048ThemeLight() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.LIGHT) {
                Text("Hello Light")
            }
        }
        composeTestRule.onNodeWithText("Hello Light").assertExists()
    }

    @Test
    fun testOpen2048ThemeDark() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.DARK) {
                Text("Hello Dark")
            }
        }
        composeTestRule.onNodeWithText("Hello Dark").assertExists()
    }

    @Test
    fun testOpen2048ThemeClassic() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.CLASSIC) {
                Text("Hello Classic")
            }
        }
        composeTestRule.onNodeWithText("Hello Classic").assertExists()
    }

    @Test
    fun testOpen2048ThemeOLED() {
        composeTestRule.setContent {
            Open2048Theme(theme = AppTheme.OLED) {
                Text("Hello OLED")
            }
        }
        composeTestRule.onNodeWithText("Hello OLED").assertExists()
    }
}
