package com.scottmangiapane.open2048.ui.theme

import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TileColorsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testColorsCoverAllBranches() {
        val themes = AppTheme.entries
        val values = listOf(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192)
        
        composeTestRule.setContent {
            for (theme in themes) {
                for (value in values) {
                    TileColors.getBackgroundColor(value, theme)
                    TileColors.getTextColor(value, theme)
                }
            }
        }
    }
}
