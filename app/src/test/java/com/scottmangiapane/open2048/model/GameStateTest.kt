package com.scottmangiapane.open2048.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {

    @Test
    fun testCanResume() {
        val board = listOf(listOf(Tile(1, 2)))
        val mode = GameMode.Classic(4)
        
        val stateWithMoves = GameState(board = board, movesCount = 1, gameMode = mode)
        assertTrue(stateWithMoves.canResume)

        val stateNoMoves = GameState(board = board, movesCount = 0, gameMode = mode)
        assertFalse(stateNoMoves.canResume)

        val stateGameOver = GameState(board = board, movesCount = 1, isGameOver = true, gameMode = mode)
        assertFalse(stateGameOver.canResume)

        val stateEmptyBoard = GameState(board = emptyList(), movesCount = 1, gameMode = mode)
        assertFalse(stateEmptyBoard.canResume)
    }
}
