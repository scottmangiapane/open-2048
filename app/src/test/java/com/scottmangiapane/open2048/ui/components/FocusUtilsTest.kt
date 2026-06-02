package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import org.junit.Assert.assertTrue

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
