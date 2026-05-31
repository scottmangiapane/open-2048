package com.scottmangiapane.open2048.logic

import com.scottmangiapane.open2048.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class GameEngineTest {

    @Test
    fun testMoveLeftNoMerge() {
        val board = listOf(
            listOf(Tile(id = 1, value = 2), null, null, null),
            listOf(null, Tile(id = 2, value = 4), null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0f, 0f, 10)
        
        assertEquals(2, result.board[0][0]?.value)
        assertEquals(4, result.board[1][0]?.value)
        assertTrue(result.hasChanged)
    }

    @Test
    fun testMoveLeftWithMerge() {
        val board = listOf(
            listOf(Tile(id = 1, value = 2), Tile(id = 2, value = 2), null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0f, 0f, 10)
        
        assertEquals(4, result.board[0][0]?.value)
        assertEquals(4, result.scoreGained)
        assertTrue(result.hasChanged)
    }

    @Test
    fun testIsGameOver() {
        val fullBoardNoMoves = listOf(
            listOf(Tile(1, 2), Tile(2, 4), Tile(3, 2), Tile(4, 4)),
            listOf(Tile(5, 4), Tile(6, 2), Tile(7, 4), Tile(8, 2)),
            listOf(Tile(9, 2), Tile(10, 4), Tile(11, 2), Tile(12, 4)),
            listOf(Tile(13, 4), Tile(14, 2), Tile(15, 4), Tile(16, 2))
        )
        assertTrue(GameEngine.isGameOver(fullBoardNoMoves))

        val fullBoardWithMoves = listOf(
            listOf(Tile(1, 2), Tile(2, 2), Tile(3, 2), Tile(4, 4)),
            listOf(Tile(5, 4), Tile(6, 2), Tile(7, 4), Tile(8, 2)),
            listOf(Tile(9, 2), Tile(10, 4), Tile(11, 2), Tile(12, 4)),
            listOf(Tile(13, 4), Tile(14, 2), Tile(15, 4), Tile(16, 2))
        )
        assertFalse(GameEngine.isGameOver(fullBoardWithMoves))
    }
}
