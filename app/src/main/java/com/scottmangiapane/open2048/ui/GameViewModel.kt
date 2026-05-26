package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.PreferenceRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val gameEngine = GameEngine()
    private val prefs = PreferenceRepository(application)
    
    private var lastState: GameState? = null
    private var bestScoreJob: Job? = null
    private var timerJob: Job? = null
    
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        // Observe dark mode setting
        viewModelScope.launch {
            prefs.isDarkMode.collectLatest { isDark ->
                _state.update { it.copy(isDarkMode = isDark) }
            }
        }
        
        // Load initial state
        viewModelScope.launch {
            val saved = prefs.savedGameState.firstOrNull()
            if (saved != null) {
                _state.update { 
                    saved.copy(
                        isDarkMode = it.isDarkMode,
                        isGameOver = saved.isGameOver || gameEngine.isGameOver(saved.board) || (saved.timeLeftMs == 0L),
                    )
                }
                observeBestScore(saved.gameMode)
                if (!_state.value.isGameOver) startTimer()
            } else {
                restartGame(GameMode.Classic(4))
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
        if (_state.value.gameMode is GameMode.Blitz) {
            timerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    var shouldStop = false
                    _state.update { state ->
                        val newTime = (state.timeLeftMs ?: 0L) - 1000L
                        if (newTime <= 0) {
                            shouldStop = true
                            state.copy(timeLeftMs = 0, isGameOver = true)
                        } else {
                            state.copy(timeLeftMs = newTime)
                        }
                    }
                    saveGame(_state.value)
                    if (shouldStop) break
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun resumeGame() {
        if (!_state.value.isGameOver) {
            startTimer()
        }
    }

    fun restartGame(mode: GameMode? = null) {
        val currentMode = mode ?: _state.value.gameMode
        lastState = null
        timerJob?.cancel()
        
        observeBestScore(currentMode)

        val (initialBoard, nextId) = if (currentMode is GameMode.Daily) {
            gameEngine.createDailyBoard(currentMode.size, currentMode.dateSeed)
        } else {
            gameEngine.createInitialBoard(
                size = currentMode.size,
                seedValue1 = Random.nextFloat(), seedPos1 = Random.nextFloat(),
                seedValue2 = Random.nextFloat(), seedPos2 = Random.nextFloat(),
                startId = 0
            )
        }
        
        val (nextV, nextP) = generateNextSeeds(currentMode, nextId)

        val newState = GameState(
            board = initialBoard,
            score = 0,
            isGameOver = gameEngine.isGameOver(initialBoard),
            canUndo = false,
            nextId = nextId,
            nextValueSeed = nextV,
            nextPosSeed = nextP,
            gameMode = currentMode,
            timeLeftMs = if (currentMode is GameMode.Blitz) currentMode.durationMinutes * 60 * 1000L else null
        )
        _state.update { newState.copy(bestScore = it.bestScore, isDarkMode = it.isDarkMode) }
        saveGame(newState)
        if (currentMode is GameMode.Blitz) startTimer()
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
        val previous = lastState ?: return
        lastState = null
        val newState = previous.copy(
            bestScore = _state.value.bestScore, 
            canUndo = false,
            timeLeftMs = _state.value.timeLeftMs // Don't undo time
        )
        _state.update { newState }
        saveGame(newState)
        if (!newState.isGameOver) startTimer()
    }

    fun toggleDarkMode(active: Boolean) {
        viewModelScope.launch {
            prefs.setDarkMode(!active)
        }
    }

    private fun saveGame(state: GameState) {
        viewModelScope.launch {
            prefs.saveGameState(state)
        }
    }

    fun move(direction: Direction) {
        _state.update { currentState ->
            if (currentState.isGameOver) return@update currentState

            val result = gameEngine.move(
                board = currentState.board,
                direction = direction,
                valueSeed = currentState.nextValueSeed,
                posSeed = currentState.nextPosSeed,
                nextId = currentState.nextId
            )
            
            if (result.hasChanged) {
                lastState = currentState
                val newScore = currentState.score + result.scoreGained
                
                if (newScore > currentState.bestScore) {
                    viewModelScope.launch { prefs.updateBestScore(currentState.gameMode.id, newScore) }
                }
                
                val isGameOver = gameEngine.isGameOver(result.board) || (currentState.timeLeftMs == 0L)
                if (isGameOver) timerJob?.cancel()

                val (nextV, nextP) = generateNextSeeds(currentState.gameMode, result.nextId)

                val newState = currentState.copy(
                    board = result.board,
                    score = newScore,
                    isGameOver = isGameOver,
                    canUndo = true,
                    nextId = result.nextId,
                    nextValueSeed = nextV,
                    nextPosSeed = nextP,
                    bestScore = maxOf(currentState.bestScore, newScore)
                )
                saveGame(newState)
                newState
            } else {
                currentState
            }
        }
    }
}
