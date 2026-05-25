package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.ScoreRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.model.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    private val repository = ScoreRepository(application)
    
    private var lastState: GameState? = null
    
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        // Observe best score from local storage
        viewModelScope.launch {
            repository.bestScore.collectLatest { best ->
                _state.update { it.copy(bestScore = best) }
            }
        }
        restartGame()
    }

    fun restartGame() {
        lastState = null
        val (initialBoard, nextId) = gameEngine.createInitialBoard(
            seedValue1 = Random.nextFloat(), seedPos1 = Random.nextFloat(),
            seedValue2 = Random.nextFloat(), seedPos2 = Random.nextFloat(),
            startId = 0
        )
        _state.update {
            it.copy(
                board = initialBoard,
                score = 0,
                isGameOver = false,
                canUndo = false,
                nextValueSeed = Random.nextFloat(),
                nextPosSeed = Random.nextFloat(),
                nextId = nextId
            )
        }
    }

    fun undo() {
        val previous = lastState ?: return
        lastState = null
        _state.update { previous.copy(bestScore = it.bestScore, canUndo = false) }
    }

    fun move(direction: Direction) {
        if (_state.value.isGameOver) return

        val currentState = _state.value
        val (newBoard, scoreGained, newNextId) = gameEngine.move(
            board = currentState.board,
            direction = direction,
            valueSeed = currentState.nextValueSeed,
            posSeed = currentState.nextPosSeed,
            nextId = currentState.nextId
        )
        
        if (newBoard != currentState.board) {
            lastState = currentState
            
            val newScore = currentState.score + scoreGained
            
            _state.update {
                it.copy(
                    board = newBoard,
                    score = newScore,
                    isGameOver = gameEngine.isGameOver(newBoard),
                    canUndo = true,
                    nextValueSeed = Random.nextFloat(),
                    nextPosSeed = Random.nextFloat(),
                    nextId = newNextId
                )
            }
            
            // Persist high score if it's beaten
            if (newScore > _state.value.bestScore) {
                viewModelScope.launch {
                    repository.updateBestScore(newScore)
                }
            }
        }
    }
}
