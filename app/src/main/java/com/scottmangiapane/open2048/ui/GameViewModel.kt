package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.GameRepository
import com.scottmangiapane.open2048.data.ScoreRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.model.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameState(
    val board: List<List<Tile?>> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val canUndo: Boolean = false,
    val nextValueSeed: Float = 0f,
    val nextPosSeed: Float = 0f,
    val nextId: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val gameEngine = GameEngine()
    private val scoreRepository = ScoreRepository(application)
    private val gameRepository = GameRepository(application)
    
    private var lastState: GameState? = null
    
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        // Observe best score from local storage
        viewModelScope.launch {
            scoreRepository.bestScore.collectLatest { best ->
                _state.update { it.copy(bestScore = best) }
            }
        }
        
        // Load initial state
        viewModelScope.launch {
            val saved = gameRepository.savedGameState.firstOrNull()
            if (saved != null) {
                _state.update { 
                    saved.copy(
                        bestScore = it.bestScore,
                        isGameOver = gameEngine.isGameOver(saved.board)
                    )
                }
            } else {
                restartGame()
            }
        }
    }

    fun restartGame() {
        lastState = null
        val (initialBoard, nextId) = gameEngine.createInitialBoard(
            seedValue1 = Random.nextFloat(), seedPos1 = Random.nextFloat(),
            seedValue2 = Random.nextFloat(), seedPos2 = Random.nextFloat(),
            startId = 0
        )
        val newState = GameState(
            board = initialBoard,
            score = 0,
            isGameOver = false,
            canUndo = false,
            nextValueSeed = Random.nextFloat(),
            nextPosSeed = Random.nextFloat(),
            nextId = nextId
        )
        _state.update { newState.copy(bestScore = it.bestScore) }
        saveGame(newState)
    }

    fun undo() {
        val previous = lastState ?: return
        lastState = null
        val newState = previous.copy(bestScore = _state.value.bestScore, canUndo = false)
        _state.update { newState }
        saveGame(newState)
    }

    private fun saveGame(state: GameState) {
        viewModelScope.launch {
            gameRepository.saveGameState(state)
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
                val isGameOver = gameEngine.isGameOver(result.board)
                
                if (newScore > currentState.bestScore) {
                    viewModelScope.launch {
                        scoreRepository.updateBestScore(newScore)
                    }
                }
                
                val newState = currentState.copy(
                    board = result.board,
                    score = newScore,
                    isGameOver = isGameOver,
                    canUndo = true,
                    nextValueSeed = Random.nextFloat(),
                    nextPosSeed = Random.nextFloat(),
                    nextId = result.nextId,
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
