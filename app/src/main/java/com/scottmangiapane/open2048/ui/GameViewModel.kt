package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.PreferenceRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.canResume
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val gameEngine = GameEngine()
    private val prefs = PreferenceRepository(application)
    
    private var previousStateForUndo: GameState? = null
    private var bestScoreJob: Job? = null
    private var timerJob: Job? = null
    
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Menu)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val canResume: StateFlow<Boolean> = _state
        .map { state -> state.canResume }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        observeTheme()
        loadInitialState()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            prefs.theme.collectLatest { theme ->
                _state.update { it.copy(theme = theme ?: AppTheme.LIGHT) }
            }
        }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val saved = prefs.savedGameState.firstOrNull()
            if (saved != null) {
                _state.update {
                    saved.copy(
                        theme = it.theme,
                        isGameOver = saved.isGameOver || gameEngine.isGameOver(saved.board) || (saved.timeLeftMs == 0L),
                    )
                }
                observeBestScore(saved.gameMode)
                if (!_state.value.isGameOver && saved.board.isNotEmpty()) {
                    startTimer()
                }
            } else {
                val defaultMode = GameMode.Classic(4)
                _state.update { createNewGameState(defaultMode).copy(theme = it.theme) }
                observeBestScore(defaultMode)
                _currentScreen.value = Screen.Menu
            }
        }
    }

    private fun observeBestScore(mode: GameMode) {
        bestScoreJob?.cancel()
        bestScoreJob = viewModelScope.launch {
            prefs.getBestScore(mode.id).collectLatest { best ->
                _state.update { it.copy(bestScore = best) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (isActive) {
                delay(1000)
                val currentTick = System.currentTimeMillis()
                val delta = currentTick - lastTick
                lastTick = currentTick
                
                var shouldStop = false
                _state.update { state ->
                    val newElapsed = state.elapsedTimeMs + delta
                    if (state.gameMode is GameMode.Blitz) {
                        val newTime = (state.timeLeftMs ?: 0L) - delta
                        if (newTime <= 0) {
                            shouldStop = true
                            state.copy(timeLeftMs = 0, isGameOver = true, elapsedTimeMs = newElapsed)
                        } else {
                            state.copy(timeLeftMs = newTime, elapsedTimeMs = newElapsed)
                        }
                    } else {
                        state.copy(elapsedTimeMs = newElapsed)
                    }
                }
                if (shouldStop) {
                    saveGame(_state.value)
                    break
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        saveGame(_state.value)
    }

    fun resumeGame() {
        _currentScreen.value = Screen.Game
        if (!_state.value.isGameOver) {
            startTimer()
        }
    }

    fun navigateToMenu() {
        stopTimer()
        _currentScreen.value = Screen.Menu
    }

    fun restartGame(mode: GameMode? = null) {
        val currentMode = mode ?: _state.value.gameMode
        previousStateForUndo = null
        timerJob?.cancel()
        
        observeBestScore(currentMode)

        val newState = createNewGameState(currentMode)
        _state.update { newState.copy(bestScore = it.bestScore, theme = it.theme) }
        saveGame(_state.value)
        _currentScreen.value = Screen.Game
        startTimer()
    }

    private fun createNewGameState(mode: GameMode): GameState {
            val (initialBoard, nextId) = when (mode) {
                is GameMode.Daily -> gameEngine.createDailyBoard(mode.size, mode.dateSeed)
                else -> gameEngine.createInitialBoard(
                    size = mode.size,
                    seedValue1 = Random.nextFloat(),
                    seedPos1 = Random.nextFloat(),
                    seedValue2 = Random.nextFloat(),
                    seedPos2 = Random.nextFloat(),
                    startId = 0,
                )
            }
        
        val (nextV, nextP) = generateNextSeeds(mode, nextId)

        return GameState(
            board = initialBoard,
            score = 0,
            isGameOver = gameEngine.isGameOver(initialBoard),
            canUndo = false,
            nextId = nextId,
            nextValueSeed = nextV,
            nextPosSeed = nextP,
            gameMode = mode,
            timeLeftMs = if (mode is GameMode.Blitz) mode.durationMinutes * 60 * 1000L else null,
            movesCount = 0,
            elapsedTimeMs = 0L,
        )
    }

    private fun generateNextSeeds(mode: GameMode, nextId: Int): Pair<Float, Float> {
        return if (mode is GameMode.Daily) {
            val r = Random(mode.dateSeed + nextId)
            r.nextFloat() to r.nextFloat()
        } else {
            Random.nextFloat() to Random.nextFloat()
        }
    }

    fun undo() {
        val previous = previousStateForUndo ?: return
        previousStateForUndo = null
        val newState = previous.copy(
            bestScore = _state.value.bestScore, 
            canUndo = false,
            timeLeftMs = _state.value.timeLeftMs,
            elapsedTimeMs = _state.value.elapsedTimeMs
        )
        _state.update { newState }
        saveGame(newState)
        if (!newState.isGameOver) startTimer()
    }

    fun cycleTheme() {
        val nextTheme = when (_state.value.theme) {
            AppTheme.LIGHT -> AppTheme.DARK
            AppTheme.DARK -> AppTheme.CLASSIC
            AppTheme.CLASSIC -> AppTheme.LIGHT
        }
        viewModelScope.launch {
            prefs.setTheme(nextTheme)
        }
    }

    private fun saveGame(state: GameState) {
        viewModelScope.launch {
            prefs.saveGameState(state)
        }
    }

    fun move(direction: Direction) {
        val currentState = _state.value
        if (currentState.isGameOver) return

        val result = gameEngine.move(
            board = currentState.board,
            direction = direction,
            valueSeed = currentState.nextValueSeed,
            posSeed = currentState.nextPosSeed,
            nextId = currentState.nextId
        )

        if (result.hasChanged) {
            previousStateForUndo = currentState
            val newScore = currentState.score + result.scoreGained
            val bestScore = maxOf(currentState.bestScore, newScore)

            if (newScore > currentState.bestScore) {
                viewModelScope.launch { prefs.updateBestScore(currentState.gameMode.id, newScore) }
            }

            val isGameOver = gameEngine.isGameOver(result.board) || (currentState.timeLeftMs == 0L)
            if (isGameOver) timerJob?.cancel()

            val (nextV, nextP) = generateNextSeeds(currentState.gameMode, result.nextId)

            _state.update { state ->
                state.copy(
                    board = result.board,
                    score = newScore,
                    isGameOver = isGameOver,
                    canUndo = true,
                    nextId = result.nextId,
                    nextValueSeed = nextV,
                    nextPosSeed = nextP,
                    bestScore = bestScore,
                    movesCount = state.movesCount + 1,
                )
            }
            saveGame(_state.value)
        }
    }
}
