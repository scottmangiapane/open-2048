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
        
        // Test New Game button interaction
        composeTestRule.onNodeWithText("New Game").performClick()
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
        // Should NOT show confirmation dialog
        composeTestRule.onNodeWithText("Restart Game?").assertDoesNotExist()
        verify { viewModel.restartGame() }
    }

    @Test
    fun testGameScreenLargeScreen() {
        val viewModel = mockViewModel()
        
        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalWindowInfo provides object : androidx.compose.ui.platform.WindowInfo {
                    override val isWindowFocused: Boolean = true
                    override val containerSize: androidx.compose.ui.unit.IntSize = androidx.compose.ui.unit.IntSize(3000, 3000)
                }
            ) {
                GameScreen(viewModel = viewModel, onBackToMenu = {})
            }
        }
        composeTestRule.onNodeWithText("SCORE").assertExists()
    }
    
    @Test
    fun testGameScreenMediumScreen() {
        val viewModel = mockViewModel()
        
        composeTestRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalWindowInfo provides object : androidx.compose.ui.platform.WindowInfo {
                    override val isWindowFocused: Boolean = true
                    override val containerSize: androidx.compose.ui.unit.IntSize = androidx.compose.ui.unit.IntSize(1800, 1800)
                }
            ) {
                GameScreen(viewModel = viewModel, onBackToMenu = {})
            }
        }
        composeTestRule.onNodeWithText("SCORE").assertExists()
    }
}
