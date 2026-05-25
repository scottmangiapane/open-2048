package com.scottmangiapane.open2048.ui

import androidx.lifecycle.ViewModel
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.model.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameState(
    val board: List<List<Tile?>> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false
)

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine()
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
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
            val newBestScore = if (newScore > _state.value.bestScore) newScore else _state.value.bestScore
            
            _state.update {
                it.copy(
                    board = newBoard,
                    score = newScore,
                    bestScore = newBestScore,
                    isGameOver = gameEngine.isGameOver(newBoard)
                )
            }
        }
    }
}
