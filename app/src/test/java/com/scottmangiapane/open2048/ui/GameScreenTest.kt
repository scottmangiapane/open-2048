package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun mockViewModel(gameState: GameState = GameState()): GameViewModel {
        val vm = mockk<GameViewModel>(relaxed = true)
        every { vm.userPreferences } returns MutableStateFlow(UserPreferences())
        every { vm.state } returns MutableStateFlow(gameState)
        every { vm.currentScreen } returns MutableStateFlow(Screen.Game)
        every { vm.hasTouch } returns true
        every { vm.hasVibrator } returns true
        return vm
    }

    @Test
    fun testGameScreenBasicUi() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                onBackToMenu = {}
            )
        }
        composeTestRule.onNodeWithText("SCORE").assertExists()
        composeTestRule.onNodeWithText("BEST").assertExists()
        
        composeTestRule.onNodeWithText("New Game").performClick()
        verify { viewModel.restartGame() }
    }

    @Test
    fun testGameOverOverlayShows() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4)),
            listOf(Tile(3, 4), Tile(4, 2))
        )
        val gameState = GameState(
            board = board,
            isGameOver = true,
            gameMode = GameMode.Classic(2)
        )
        val viewModel = mockViewModel(gameState)

        composeTestRule.setContent {
            GameScreen(viewModel = viewModel, onBackToMenu = {})
        }

        composeTestRule.onNodeWithText("Game Over!", ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun testGameScreenRestartWithProgress() {
        val gameState = GameState(movesCount = 10, gameMode = GameMode.Classic(4))
        val viewModel = mockViewModel(gameState)
        
        composeTestRule.setContent {
            GameScreen(viewModel = viewModel, onBackToMenu = {})
        }
        
        composeTestRule.onNodeWithText("New Game").performClick()
        composeTestRule.onNodeWithText("Restart Game?").assertExists()
        composeTestRule.onNodeWithText("Restart").performClick()
        
        verify { viewModel.restartGame() }
    }

    @Test
    fun testGameScreenRestartWithoutProgress() {
        val gameState = GameState(movesCount = 0, gameMode = GameMode.Classic(4))
        val viewModel = mockViewModel(gameState)
        
        composeTestRule.setContent {
            GameScreen(viewModel = viewModel, onBackToMenu = {})
        }
        
        composeTestRule.onNodeWithText("New Game").performClick()
        composeTestRule.onNodeWithText("Restart Game?").assertDoesNotExist()
        verify { viewModel.restartGame() }
    }

    @Test
    fun testGameScreenDailyMode() {
        val state = GameState(gameMode = GameMode.Daily.today())
        val viewModel = mockViewModel(state)
        composeTestRule.setContent {
            GameScreen(viewModel = viewModel, onBackToMenu = {})
        }
        composeTestRule.onNodeWithText("DAILY CHALLENGE").assertExists()
    }

    @Test
    fun testGameScreenBlitzMode() {
        val state = GameState(gameMode = GameMode.Blitz(2), timeLeftMs = 120000L)
        val viewModel = mockViewModel(state)
        composeTestRule.setContent {
            GameScreen(viewModel = viewModel, onBackToMenu = {})
        }
        composeTestRule.onNodeWithText("2M BLITZ").assertExists()
        composeTestRule.onNodeWithText("02:00").assertExists()
    }

    @Test
    fun testGameScreenUndoAction() {
        val state = GameState(canUndo = true)
        val viewModel = mockViewModel(state)
        composeTestRule.setContent {
            GameScreen(viewModel = viewModel, onBackToMenu = {})
        }
        composeTestRule.onNodeWithText("Undo").performClick()
        verify { viewModel.undo() }
    }

    @Test
    fun testGameScreenConfettiReset() {
        val stateFlow = MutableStateFlow(GameState(highestTile = 2048, gameMode = GameMode.Classic(4)))
        val viewModel = mockViewModel()
        every { viewModel.state } returns stateFlow
        
        composeTestRule.setContent {
            GameScreen(viewModel = viewModel, onBackToMenu = {})
        }
        
        // Advance clock to let LaunchedEffect run
        composeTestRule.mainClock.advanceTimeBy(1000)
        
        // Now lower the highest tile (undo)
        stateFlow.value = stateFlow.value.copy(highestTile = 1024)
        composeTestRule.mainClock.advanceTimeBy(1000)
        
        // Now raise it again
        stateFlow.value = stateFlow.value.copy(highestTile = 2048)
        composeTestRule.mainClock.advanceTimeBy(1000)
    }
}
