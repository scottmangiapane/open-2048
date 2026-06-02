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
class ScoreCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testScoreCard() {
        composeTestRule.setContent {
            ScoreCard(label = "TEST LABEL", score = 9999)
        }

        composeTestRule.onNodeWithText("TEST LABEL").assertExists()
        composeTestRule.onNodeWithText("9999").assertExists()
    }
}
