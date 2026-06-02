package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ConfettiOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testConfettiOverlay() {
        var finished = false
        composeTestRule.setContent {
            ConfettiOverlay(
                durationMillis = 100,
                onAnimationFinished = { finished = true }
            )
        }
        
        composeTestRule.waitUntil(500) { finished }
    }
}
