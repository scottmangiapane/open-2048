package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import com.scottmangiapane.open2048.ui.components.BoardContainer
import com.scottmangiapane.open2048.ui.components.HeaderSection
import com.scottmangiapane.open2048.ui.components.ScoreCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SmallScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "w240dp-h320dp")
    fun testHeaderSectionOn2InchScreen() {
        val state = GameState(
            board = listOf(
                listOf(Tile(1, 2), Tile(2, 4)),
                listOf(Tile(3, 8), Tile(4, 16))
            ),
            score = 2048,
            bestScore = 4096,
            movesCount = 99,
            gameMode = GameMode.Classic(4)
        )

        composeTestRule.setContent {
            HeaderSection(
                state = state,
                onRestart = {},
                onUndo = {},
                isLandscape = false
            )
        }

        composeTestRule.onAllNodesWithText("2048").assertCountEquals(2)
        composeTestRule.onNodeWithText("CLASSIC").assertExists()
        composeTestRule.onNodeWithText("SCORE").assertExists()
        composeTestRule.onNodeWithText("BEST").assertExists()
        composeTestRule.onNodeWithText("MOVES").assertExists()
        composeTestRule.onNodeWithText("4096").assertExists()
        composeTestRule.onNodeWithText("New Game").assertExists()
    }

    @Test
    @Config(qualifiers = "w240dp-h320dp")
    fun testScoreCardOnSmallWidth() {
        composeTestRule.setContent {
            ScoreCard(label = "SCORE", score = 123456)
        }

        composeTestRule.onNodeWithText("SCORE").assertExists()
        composeTestRule.onNodeWithText("123456").assertExists()
    }

    @Test
    @Config(qualifiers = "w240dp-h320dp")
    fun testBoardContainerOn2InchScreen() {
        val state = GameState(
            board = listOf(
                listOf(Tile(1, 2), Tile(2, 4), Tile(3, 8), Tile(4, 16)),
                listOf(Tile(5, 32), Tile(6, 64), Tile(7, 128), Tile(8, 256)),
                listOf(Tile(9, 512), Tile(10, 1024), Tile(11, 2048), Tile(12, 4096)),
                listOf(Tile(13, 8192), Tile(14, 16384), null, null)
            ),
            gameMode = GameMode.Classic(4)
        )

        composeTestRule.setContent {
            BoardContainer(
                state = state,
                currentTheme = com.scottmangiapane.open2048.model.AppTheme.CLASSIC,
                animationSpeed = com.scottmangiapane.open2048.model.AnimationSpeed.FAST
            )
        }

        composeTestRule.onNodeWithText("1024").assertExists()
        composeTestRule.onNodeWithText("2048").assertExists()
        composeTestRule.onNodeWithText("16384").assertExists()
    }

    @Test
    @Config(qualifiers = "w240dp-h320dp")
    fun testMenuScreenOn2InchScreen() {
        composeTestRule.setContent {
            MenuScreen(
                onStartGame = {},
                onResumeGame = {},
                onNavigateToStats = {},
                onNavigateToSettings = {},
                canResume = true,
                hasProgress = true
            )
        }

        composeTestRule.onNodeWithText("Resume").assertExists()
        composeTestRule.onNodeWithText("Daily Challenge").assertExists()
        composeTestRule.onNodeWithText("Classic 4x4").assertExists()
    }
}
