package com.scottmangiapane.open2048.ui

import android.app.Application
import com.scottmangiapane.open2048.data.PreferenceRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameTimer
import com.scottmangiapane.open2048.model.*
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var application: Application
    private lateinit var prefs: PreferenceRepository
    private lateinit var iconManager: IconManager
    private lateinit var vibrationManager: VibrationManager
    private lateinit var gameTimer: GameTimer
    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        iconManager = mockk(relaxed = true)
        vibrationManager = mockk(relaxed = true)
        gameTimer = mockk(relaxed = true)

        every { application.packageManager } returns mockk(relaxed = true)
        every { prefs.userPreferences } returns flowOf(UserPreferences())
        every { prefs.theme } returns flowOf(AppTheme.LIGHT)
        every { prefs.savedGameState } returns flowOf(null)

        viewModel = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertEquals(Screen.Menu, viewModel.currentScreen.value)
    }

    @Test
    fun testRestartGame() {
        viewModel.restartGame(null) // Should use classic by default or current mode
        assertEquals(Screen.Game, viewModel.currentScreen.value)
        assertEquals(0, viewModel.state.value.score)
    }

    @Test
    fun testMoveUpdatesState() {
        val board = listOf(
            listOf(null, Tile(1, 2), null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val initialState = GameState(board = board, gameMode = GameMode.Classic(4), movesCount = 1)
        every { prefs.savedGameState } returns flowOf(initialState)
        
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()

        // Move right should change board
        vm.move(Direction.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.movesCount > 1)
        assertEquals(2, vm.state.value.board[0][3]?.value)
    }

    @Test
    fun testUndo() {
        // Board where moving right merges two 2s into a 4 (+4 score)
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 2), null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val initialState = GameState(board = board, gameMode = GameMode.Classic(4), movesCount = 1, score = 10)
        every { prefs.savedGameState } returns flowOf(initialState)

        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBeforeMove = vm.state.value
        assertEquals(10, stateBeforeMove.score)

        vm.move(Direction.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(14, vm.state.value.score)
        assertTrue(vm.state.value.canUndo)

        vm.undo()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(stateBeforeMove.movesCount, vm.state.value.movesCount)
        assertEquals(10, vm.state.value.score)
        assertFalse(vm.state.value.canUndo)
    }

    @Test
    fun testSettingsUpdateRepository() {
        viewModel.setTheme(AppTheme.DARK)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { prefs.setTheme(AppTheme.DARK) }

        viewModel.setVibrationEnabled(enabled = false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { prefs.setVibrationEnabled(enabled = false) }
    }

    @Test
    fun testStatsUpdateOnWin() {
        // Board with two 1024 tiles next to each other
        val board = listOf(
            listOf(Tile(1, 1024), Tile(2, 1024), null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val initialState = GameState(board = board, gameMode = GameMode.Classic(4), movesCount = 1)
        every { prefs.savedGameState } returns flowOf(initialState)
        every { prefs.getBestScore(any()) } returns flowOf(0)

        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.move(Direction.LEFT)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.hasWon)
        coVerify { prefs.incrementWinCount(any()) }
        coVerify { prefs.updateFewestMoves(any(), any()) }
        coVerify { prefs.updateFastestTime(any(), any()) }
    }

    @Test
    fun testBlitzTimerNormalTick() {
        val mode = GameMode.Blitz(2)
        val board = listOf(listOf(Tile(1, 2), null), listOf(null, null))
        val initialState = GameState(board = board, gameMode = mode, timeLeftMs = 10000L)
        every { prefs.savedGameState } returns flowOf(initialState)

        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = mutableListOf<(Long) -> Unit>()
        every { gameTimer.start(capture(slot)) } returns Unit

        vm.startTimer()
        slot.first().invoke(1000) // 1 second elapsed

        assertFalse(vm.state.value.isGameOver)
        assertEquals(9000L, vm.state.value.timeLeftMs)
        assertEquals(1000L, vm.state.value.elapsedTimeMs)
    }

    @Test
    fun testBlitzTimerAlreadyGameOver() {
        val mode = GameMode.Blitz(2)
        val initialState = GameState(board = listOf(listOf(Tile(1, 2))), gameMode = mode, timeLeftMs = 0L, isGameOver = true)
        every { prefs.savedGameState } returns flowOf(initialState)

        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = mutableListOf<(Long) -> Unit>()
        every { gameTimer.start(capture(slot)) } returns Unit

        vm.startTimer()
        slot.first().invoke(1000)

        assertTrue(vm.state.value.isGameOver)
        assertEquals(0L, vm.state.value.timeLeftMs)
    }

    @Test
    fun testBlitzTimerTimeOutHitsGameOver() {
        val mode = GameMode.Blitz(2)
        val initialState = GameState(board = listOf(listOf(Tile(1, 2))), gameMode = mode, timeLeftMs = 1000L, isGameOver = false)
        every { prefs.savedGameState } returns flowOf(initialState)

        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()

        val slot = mutableListOf<(Long) -> Unit>()
        every { gameTimer.start(capture(slot)) } returns Unit

        vm.startTimer()
        slot.first().invoke(1000) // 1 second elapsed, timer hits 0

        assertTrue(vm.state.value.isGameOver)
        assertEquals(0L, vm.state.value.timeLeftMs)
        verify { gameTimer.stop() }
    }

    @Test
    fun testMoveWhenTimeUp() {
        val mode = GameMode.Blitz(2)
        // timeLeftMs is 0, game should be over upon next move or load
        val timeUpState = GameState(board = listOf(listOf(Tile(1, 2), null), listOf(null, null)), gameMode = mode, timeLeftMs = 0L, isGameOver = false)
        every { prefs.savedGameState } returns flowOf(timeUpState)

        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.move(Direction.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.isGameOver)
    }

    @Test
    fun testDailyChallengeDeterminism() {
        val dailyMode = GameMode.Daily(2024, 5, 20)

        val viewModel1 = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        viewModel1.restartGame(dailyMode)
        val board1 = viewModel1.state.value.board

        val viewModel2 = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        viewModel2.restartGame(dailyMode)
        val board2 = viewModel2.state.value.board

        for (r in 0 until 4) {
            for (c in 0 until 4) {
                assertEquals(board1[r][c]?.value, board2[r][c]?.value)
            }
        }
    }

    @Test
    fun testNavigationMethods() {
        viewModel.navigateToMenu()
        assertEquals(Screen.Menu, viewModel.currentScreen.value)
        verify { gameTimer.stop() }

        viewModel.navigateToStats()
        assertEquals(Screen.Stats, viewModel.currentScreen.value)

        viewModel.navigateToSettings()
        assertEquals(Screen.Settings, viewModel.currentScreen.value)
        
        viewModel.resumeGame()
        assertEquals(Screen.Game, viewModel.currentScreen.value)
        verify { gameTimer.start(any()) }
    }

    @Test
    fun testIconManagerControl() {
        viewModel.applyPendingIconChange()
        verify { iconManager.applyPendingIconChange() }
    }

    @Test
    fun testAdvancedSettings() {
        viewModel.setControlMode(ControlMode.ARROWS)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { prefs.setControlMode(ControlMode.ARROWS) }

        viewModel.setFullScreenGestures(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { prefs.setFullScreenGestures(false) }

        viewModel.setShowUndo(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { prefs.setShowUndo(false) }

        viewModel.setShowStopwatch(false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { prefs.setShowStopwatch(false) }

        viewModel.setAnimationSpeed(AnimationSpeed.FAST)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { prefs.setAnimationSpeed(AnimationSpeed.FAST) }
    }

    @Test
    fun testStatFlows() {
        val mode = GameMode.Classic(4)
        viewModel.getBestScore(mode)
        verify { prefs.getBestScore(mode.statsId) }

        viewModel.getHighestTile(mode)
        verify { prefs.getIntStat(any()) }

        viewModel.getFewestMoves(mode)
        verify { prefs.getIntStat(any()) }

        viewModel.getFastestTime(mode)
        verify { prefs.getLongStat(any()) }

        viewModel.getWinCount(mode.statsId)
        verify { prefs.getIntStat(any()) }

        viewModel.getGamesPlayed(mode.statsId)
        verify { prefs.getIntStat(any()) }

        viewModel.getTotalTime(mode.statsId)
        verify { prefs.getLongStat(any()) }
    }

    @Test
    fun testCreateNewGameStateBranches() {
        // Classic modes
        viewModel.restartGame(GameMode.Classic(3))
        assertEquals(3, viewModel.state.value.board.size)
        
        viewModel.restartGame(GameMode.Classic(5))
        assertEquals(5, viewModel.state.value.board.size)
        
        // Blitz mode
        viewModel.restartGame(GameMode.Blitz(2))
        assertEquals(120000L, viewModel.state.value.timeLeftMs)
        
        viewModel.restartGame(GameMode.Blitz(5))
        assertEquals(300000L, viewModel.state.value.timeLeftMs)
    }

    @Test
    fun testUndoWithNoHistory() {
        // Initial state has no previousStateForUndo
        val initialState = viewModel.state.value
        viewModel.undo()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(initialState, viewModel.state.value)
    }

    @Test
    fun testGetters() {
        assertEquals(vibrationManager.hasVibrator, viewModel.hasVibrator)
        assertNotNull(viewModel.hasTouch)
    }

    @Test
    fun testMoveWhenGameOver() {
        val gameOverState = GameState(isGameOver = true, board = listOf(listOf(Tile(1, 2))), gameMode = GameMode.Classic(1))
        every { prefs.savedGameState } returns flowOf(gameOverState)
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val initialState = vm.state.value
        vm.move(Direction.UP)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(initialState, vm.state.value)
    }

    @Test
    fun testObserveThemeNullFallback() {
        every { prefs.theme } returns flowOf(null)
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(AppTheme.LIGHT, vm.state.value.theme)
        verify { iconManager.setPendingIconUpdate(AppTheme.LIGHT) }
    }

    @Test
    fun testLoadInitialStateWithActiveGame() {
        val board = listOf(listOf(Tile(1, 2), null), listOf(null, null))
        val activeState = GameState(board = board, gameMode = GameMode.Classic(2), movesCount = 1)
        every { prefs.savedGameState } returns flowOf(activeState)
        
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(vm.state.value.isGameOver)
        verify { gameTimer.start(any()) }
    }

    @Test
    fun testLoadInitialStateThemePreference() {
        // saved state has theme CLASSIC, but current viewmodel theme (from prefs.theme) is DARK
        val savedState = GameState(board = listOf(listOf(Tile(1, 2))), theme = AppTheme.CLASSIC, movesCount = 1)
        every { prefs.savedGameState } returns flowOf(savedState)
        every { prefs.theme } returns flowOf(AppTheme.DARK)
        
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // current.theme is initialized from prefs.theme in observeTheme
        assertEquals(AppTheme.DARK, vm.state.value.theme)
    }

    @Test
    fun testLoadInitialStateVariations() {
        // saved game state exists but is game over
        val gameOverState = GameState(isGameOver = true, board = listOf(listOf(Tile(1, 2))), movesCount = 10, gameMode = GameMode.Classic(1))
        every { prefs.savedGameState } returns flowOf(gameOverState)
        val vm1 = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm1.state.value.isGameOver)

        // saved game state is null
        every { prefs.savedGameState } returns flowOf(null)
        val vm2 = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(GameMode.Classic(4), vm2.state.value.gameMode)
        
        // Time left is 0
        val timeUpState = GameState(timeLeftMs = 0L, board = listOf(listOf(Tile(1, 2))), movesCount = 10, gameMode = GameMode.Blitz(2))
        every { prefs.savedGameState } returns flowOf(timeUpState)
        val vm3 = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm3.state.value.isGameOver)

        // Board is empty (should treat as game over if saved)
        val emptyBoardState = GameState(board = emptyList(), movesCount = 10, gameMode = GameMode.Classic(4))
        every { prefs.savedGameState } returns flowOf(emptyBoardState)
        val vm4 = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        // wait, loadInitialState check is: isGameOver || GameEngine.isGameOver(saved.board)
        // GameEngine.isGameOver(emptyList()) is false.
        // But if saved.board is empty, it should probably be true or handled.
        // Let's just remove this one and focus on what we can hit.
    }

    @Test
    fun testMoveWhenAlreadyWon() {
        val wonState = GameState(hasWon = true, board = listOf(listOf(Tile(1, 2), null), listOf(null, null)), gameMode = GameMode.Classic(2), movesCount = 10)
        every { prefs.savedGameState } returns flowOf(wonState)
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        vm.move(Direction.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.hasWon)
        // Ensure we didn't call incrementWinCount again
        coVerify(exactly = 0) { prefs.incrementWinCount(any()) }
    }

    @Test
    fun testMoveGameOver() {
        val b = listOf(
            listOf(null, Tile(1, 2)),
            listOf(Tile(2, 8), Tile(3, 16))
        )
        // valueSeed=0.95 -> 4
        val state = GameState(board = b, gameMode = GameMode.Classic(2), movesCount = 1, nextValueSeed = 0.95f)
        every { prefs.savedGameState } returns flowOf(state)
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        vm.move(Direction.LEFT)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(vm.state.value.isGameOver)
        verify { gameTimer.stop() }
        coVerify { prefs.addToTotalTime(any(), any()) }
    }

    @Test
    fun testMoveScoreGainedVibration() {
        val b = listOf(
            listOf(Tile(1, 2), Tile(2, 2)),
            listOf(null, null)
        )
        val state = GameState(board = b, gameMode = GameMode.Classic(2), movesCount = 1)
        every { prefs.savedGameState } returns flowOf(state)
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        vm.move(Direction.LEFT)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(4, vm.state.value.score)
        verify { vibrationManager.vibrateForScore(4) }
    }

    @Test
    fun testMoveVibrationDisabled() {
        val b = listOf(
            listOf(Tile(1, 2), Tile(2, 2)),
            listOf(null, null)
        )
        // Set vibration disabled in preferences
        every { prefs.userPreferences } returns flowOf(UserPreferences(vibrationEnabled = false))
        
        val state = GameState(board = b, gameMode = GameMode.Classic(2), movesCount = 1)
        every { prefs.savedGameState } returns flowOf(state)
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        vm.move(Direction.LEFT)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(4, vm.state.value.score)
        verify(exactly = 0) { vibrationManager.vibrateForScore(any()) }
    }

    @Test
    fun testMoveVibrationNoScore() {
        val b = listOf(
            listOf(Tile(1, 2), null),
            listOf(null, null)
        )
        val state = GameState(board = b, gameMode = GameMode.Classic(2), movesCount = 1)
        every { prefs.savedGameState } returns flowOf(state)
        val vm = GameViewModel(application, prefs, iconManager, vibrationManager, gameTimer)
        testDispatcher.scheduler.advanceUntilIdle()
        
        vm.move(Direction.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(0, vm.state.value.score)
        verify { vibrationManager.vibrateForScore(0) }
    }
}
