package com.scottmangiapane.open2048.logic

import com.scottmangiapane.open2048.model.Tile
import org.junit.Assert.*
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
    fun testMoveRight() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.RIGHT, 0f, 0f, 10)
        assertEquals(2, result.board[0][3]?.value)
        assertTrue(result.hasChanged)
    }

    @Test
    fun testMoveUp() {
        val board = listOf(
            listOf(null, null, null, null),
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.UP, 0f, 0f, 10)
        assertEquals(2, result.board[0][0]?.value)
        assertTrue(result.hasChanged)
    }

    @Test
    fun testMoveDown() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.DOWN, 0f, 0f, 10)
        assertEquals(2, result.board[3][0]?.value)
        assertTrue(result.hasChanged)
    }

    @Test
    fun testComplexMerge() {
        // [2, 2, 2, 2] -> [4, 4, 0, 0]
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 2), Tile(3, 2), Tile(4, 2)),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0f, 0f, 10)
        assertEquals(4, result.board[0][0]?.value)
        assertEquals(4, result.board[0][1]?.value)
        assertEquals(8, result.scoreGained)
    }

    @Test
    fun testMergeDoesNotChain() {
        // [2, 2, 4, 0] -> [4, 4, 0, 0] NOT [8, 0, 0, 0]
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 2), Tile(3, 4), null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0f, 0f, 10)
        assertEquals(4, result.board[0][0]?.value)
        assertEquals(4, result.board[0][1]?.value)
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
        
        // Edge cases from AdvancedTest
        val boardWithMergeVertical = listOf(
            listOf(Tile(1, 2), Tile(2, 4), Tile(3, 2), Tile(4, 4)),
            listOf(Tile(5, 2), Tile(6, 2), Tile(7, 4), Tile(8, 2)),
            listOf(Tile(9, 2), Tile(10, 4), Tile(11, 2), Tile(12, 4)),
            listOf(Tile(13, 4), Tile(14, 2), Tile(15, 4), Tile(16, 2))
        )
        assertFalse(GameEngine.isGameOver(boardWithMergeVertical))
        
        val boardWithMergeHorizontal = listOf(
            listOf(Tile(1, 2), Tile(2, 2)),
            listOf(Tile(3, 4), Tile(4, 8))
        )
        assertFalse(GameEngine.isGameOver(boardWithMergeHorizontal))

        assertFalse(GameEngine.isGameOver(emptyList()))
    }

    @Test
    fun testCreateInitialBoard() {
        val (board, nextId) = GameEngine.createInitialBoard(4, 0.5f, 0.1f, 0.5f, 0.9f, 0)
        var tileCount = 0
        board.forEach { row ->
            row.forEach { tile ->
                if (tile != null) tileCount++
            }
        }
        assertEquals(2, tileCount)
        assertEquals(2, nextId)
    }

    @Test
    fun testMoveValueSeed() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        
        // Seed < 0.9 should result in a 2
        val result2 = GameEngine.move(board, Direction.RIGHT, 0.89f, 0f, 10)
        val newTiles2 = result2.board.flatten().filterNotNull().filter { it.id == 10 }
        assertEquals(1, newTiles2.size)
        assertEquals(2, newTiles2[0].value)

        // Seed >= 0.9 should result in a 4
        val result4 = GameEngine.move(board, Direction.RIGHT, 0.9f, 0f, 20)
        val newTiles4 = result4.board.flatten().filterNotNull().filter { it.id == 20 }
        assertEquals(1, newTiles4.size)
        assertEquals(4, newTiles4[0].value)
    }

    @Test
    fun testMoveNoChange() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4), Tile(3, 8), Tile(4, 16)),
            listOf(Tile(5, 32), Tile(6, 64), Tile(7, 128), Tile(8, 256)),
            listOf(Tile(9, 512), Tile(10, 1024), Tile(11, 2048), Tile(12, 4096)),
            listOf(Tile(13, 8192), Tile(14, 16384), Tile(15, 32768), Tile(16, 65536))
        )
        
        val result = GameEngine.move(board, Direction.UP, 0.5f, 0.5f, 100)
        assertFalse(result.hasChanged)
        assertEquals(board, result.board)
    }

    @Test
    fun testCreateDailyBoardBranches() {
        // Just verify it runs and respects seed for some distribution
        val (board1, _) = GameEngine.createDailyBoard(4, 12345L)
        val (board2, _) = GameEngine.createDailyBoard(4, 12345L)
        assertEquals(board1.flatten().map { it?.value }, board2.flatten().map { it?.value })
        
        // Coverage for all value thresholds
        val boardLarge = GameEngine.createDailyBoard(10, 999L).first.flatten().filterNotNull().map { it.value }
        val thresholds = listOf(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
        for (t in thresholds) {
            assertTrue("Should contain $t", boardLarge.contains(t))
        }
    }

    @Test
    fun testMoveWhenFullNoMerge() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4)),
            listOf(Tile(3, 8), Tile(4, 16))
        )
        val result = GameEngine.move(board, Direction.LEFT, 0f, 0f, 0)
        assertFalse(result.hasChanged)
    }

    @Test
    fun testMoveWithNoTiles() {
        val board = listOf(listOf(null, null), listOf(null, null))
        val result = GameEngine.move(board, Direction.UP, 0f, 0f, 0)
        assertFalse(result.hasChanged)
    }

    @Test
    fun testMergeAtEnd() {
        // [null, 2, 2] -> [4, null, null] in LEFT
        val board = listOf(listOf(null, Tile(1, 2), Tile(2, 2)))
        val result = GameEngine.move(board, Direction.LEFT, 0f, 0f, 0)
        assertEquals(4, result.board[0][0]?.value)
    }

    @Test
    fun testMoveWhenGameOver() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4)),
            listOf(Tile(3, 4), Tile(4, 2))
        )
        val result = GameEngine.move(board, Direction.UP, 0f, 0f, 0)
        assertFalse(result.hasChanged)
    }

    @Test
    fun testCreateInitialBoardEdgeSeeds() {
        // valueSeed >= 0.9 -> 4
        val (board, _) = GameEngine.createInitialBoard(2, 0.95f, 0f, 0.99f, 0.5f, 0)
        val values = board.flatten().filterNotNull().map { it.value }
        assertEquals(listOf(4, 4), values)
    }

    @Test
    fun testCreateInitialBoardWhenFull() {
        val (b, nextId) = GameEngine.createInitialBoard(1, 0.5f, 0.5f, 0.5f, 0.5f, 0)
        assertEquals(1, nextId) // Only one tile could be added
        assertNotNull(b[0][0])
    }
}
