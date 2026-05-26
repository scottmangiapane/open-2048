package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.PreferenceRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.model.GameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val gameEngine = GameEngine()
    private val prefs = PreferenceRepository(application)
    
    private var lastState: GameState? = null
    private var bestScoreJob: Job? = null
    
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
                        isGameOver = gameEngine.isGameOver(saved.board),
                    )
                }
                observeBestScore(saved.board.size)
            } else {
                restartGame(4)
            }
        }
    }

    private fun observeBestScore(size: Int) {
        bestScoreJob?.cancel()
        bestScoreJob = viewModelScope.launch {
            prefs.getBestScore(size).collectLatest { best ->
                _state.update { it.copy(bestScore = best) }
            }
        }
    }

    fun restartGame(size: Int? = null) {
        val currentSize = size ?: _state.value.board.size.takeIf { it > 0 } ?: 4
        lastState = null
        
        if (size != null) {
            observeBestScore(size)
        }

        val (initialBoard, nextId) = gameEngine.createInitialBoard(
            size = currentSize,
            seedValue1 = Random.nextFloat(), seedPos1 = Random.nextFloat(),
            seedValue2 = Random.nextFloat(), seedPos2 = Random.nextFloat(),
            startId = 0
        )
        val newState = GameState(
            board = initialBoard,
            score = 0,
            isGameOver = false,
            canUndo = false,
            nextId = nextId,
            nextValueSeed = Random.nextFloat(),
            nextPosSeed = Random.nextFloat()
        )
        _state.update { newState.copy(bestScore = it.bestScore, isDarkMode = it.isDarkMode) }
        saveGame(newState)
    }

    fun undo() {
        val previous = lastState ?: return
        lastState = null
        val newState = previous.copy(bestScore = _state.value.bestScore, canUndo = false)
        _state.update { newState }
        saveGame(newState)
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = _state.value.isDarkMode ?: false
            prefs.setDarkMode(!current)
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
                val size = currentState.board.size
                
                if (newScore > currentState.bestScore) {
                    viewModelScope.launch { prefs.updateBestScore(size, newScore) }
                }
                
                val newState = currentState.copy(
                    board = result.board,
                    score = newScore,
                    isGameOver = gameEngine.isGameOver(result.board),
                    canUndo = true,
                    nextId = result.nextId,
                    nextValueSeed = Random.nextFloat(),
                    nextPosSeed = Random.nextFloat(),
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
