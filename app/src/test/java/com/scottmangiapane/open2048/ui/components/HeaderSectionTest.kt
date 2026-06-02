package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.assertTrue
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HeaderSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHeaderSectionDisplaysScoreAndMode() {
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            score = 1234,
            bestScore = 5678,
            gameMode = GameMode.Classic(4),
            movesCount = 42,
            elapsedTimeMs = 60000L
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = {},
                onUndo = {},
                isLandscape = false
            )
        }

        composeTestRule.onNodeWithText("1234").assertExists()
        composeTestRule.onNodeWithText("5678").assertExists()
        composeTestRule.onNodeWithText("42").assertExists()
        composeTestRule.onNodeWithText("CLASSIC").assertExists()
        composeTestRule.onNodeWithText("TIME: 01:00").assertExists()
    }

    @Test
    fun testHeaderSectionDailyMode() {
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            gameMode = GameMode.Daily(2024, 5, 20)
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = {},
                onUndo = {},
                isLandscape = false
            )
        }

        composeTestRule.onNodeWithText("DAILY CHALLENGE").assertExists()
        composeTestRule.onNodeWithText("DAY BEST").assertExists()
    }

    @Test
    fun testHeaderSectionBlitzMode() {
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            gameMode = GameMode.Blitz(2),
            timeLeftMs = 30000L
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = {},
                onUndo = {},
                isLandscape = false
            )
        }

        composeTestRule.onNodeWithText("2M BLITZ").assertExists()
        composeTestRule.onNodeWithText("00:30").assertExists()
    }

    @Test
    fun testHeaderSectionActions() {
        var restartClicked = false
        var undoClicked = false
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            canUndo = true,
            movesCount = 1
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = { restartClicked = true },
                onUndo = { undoClicked = true },
                isLandscape = false,
                showUndo = true
            )
        }

        composeTestRule.onNodeWithText("New Game").performClick()
        assertTrue(restartClicked)

        composeTestRule.onNodeWithText("Undo").performClick()
        assertTrue(undoClicked)
    }

    @Test
    fun testHeaderSectionLandscape() {
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            gameMode = GameMode.Classic(3)
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = {},
                onUndo = {},
                isLandscape = true
            )
        }

        composeTestRule.onNodeWithText("3x3 CLASSIC").assertExists()
    }

    @Test
    fun testHeaderSectionUndoDisabled() {
        val state = GameState(board = listOf(listOf(Tile(1, 2))), canUndo = false)
        composeTestRule.setContent {
            HeaderSection(state = state, onRestart = {}, onUndo = {}, isLandscape = false)
        }
        composeTestRule.onNodeWithText("Undo").assertIsNotEnabled()
    }

    @Test
    fun testHeaderSectionBlitzLowTime() {
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            gameMode = GameMode.Blitz(2),
            timeLeftMs = 5000L // 5 seconds
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = {},
                onUndo = {},
                isLandscape = false
            )
        }

        composeTestRule.onNodeWithText("00:05").assertExists()
    }

    @Test
    fun testHeaderSectionClassicLarge() {
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            gameMode = GameMode.Classic(5)
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = {},
                onUndo = {},
                isLandscape = false
            )
        }

        composeTestRule.onNodeWithText("5x5 CLASSIC").assertExists()
    }
}
