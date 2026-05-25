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

data class GameState(
    val board: List<List<Tile?>> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val gameEngine = GameEngine()
    private val repository = ScoreRepository(application)
    
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
        _state.update {
            it.copy(
                board = gameEngine.createInitialBoard(),
                score = 0,
                isGameOver = false
            )
        }
    }

    fun move(direction: Direction) {
        if (_state.value.isGameOver) return

        val (newBoard, scoreGained) = gameEngine.move(_state.value.board, direction)
        
        if (newBoard != _state.value.board) {
            val newScore = _state.value.score + scoreGained
            
            _state.update {
                it.copy(
                    board = newBoard,
                    score = newScore,
                    isGameOver = gameEngine.isGameOver(newBoard)
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
