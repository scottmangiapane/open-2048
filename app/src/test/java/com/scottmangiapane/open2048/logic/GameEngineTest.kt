package com.scottmangiapane.open2048.logic

import com.scottmangiapane.open2048.model.Tile
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    @Test
    fun testMoveLeftNoMerge() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0.5f, 0.5f, 100)
        assertFalse(result.hasChanged)
        assertEquals(board, result.board)
    }

    @Test
    fun testMoveLeftWithMerge() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 2), null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0.5f, 0.5f, 100)
        assertTrue(result.hasChanged)
        assertEquals(4, result.board[0][0]?.value)
        assertEquals(4, result.scoreGained)
        assertNull(result.board[0][1])
        // One tile should be added (the 100th tile)
        val newTiles = result.board.flatten().filterNotNull().filter { it.id == 100 }
        assertEquals(1, newTiles.size)
    }

    @Test
    fun testMoveRight() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.RIGHT, 0.5f, 0.5f, 100)
        assertTrue(result.hasChanged)
        assertEquals(2, result.board[0][3]?.value)
    }

    @Test
    fun testMoveUp() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.UP, 0.5f, 0.5f, 100)
        assertFalse(result.hasChanged)
    }

    @Test
    fun testMoveDown() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.DOWN, 0.5f, 0.5f, 100)
        assertTrue(result.hasChanged)
        assertEquals(2, result.board[3][0]?.value)
    }

    @Test
    fun testComplexMerge() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 2), Tile(3, 4), Tile(4, 4)),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0.5f, 0.5f, 100)
        assertEquals(4, result.board[0][0]?.value)
        assertEquals(8, result.board[0][1]?.value)
        assertEquals(12, result.scoreGained)
    }

    @Test
    fun testMergeDoesNotChain() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 2), Tile(3, 4), null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.LEFT, 0.5f, 0.5f, 100)
        // 2+2=4, but it shouldn't merge with existing 4 in the same move
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

        val fullBoardWithHorizontalMove = listOf(
            listOf(Tile(1, 2), Tile(2, 2), Tile(3, 2), Tile(4, 4)),
            listOf(Tile(5, 4), Tile(6, 2), Tile(7, 4), Tile(8, 2)),
            listOf(Tile(9, 2), Tile(10, 4), Tile(11, 2), Tile(12, 4)),
            listOf(Tile(13, 4), Tile(14, 2), Tile(15, 4), Tile(16, 2))
        )
        assertFalse(GameEngine.isGameOver(fullBoardWithHorizontalMove))

        val fullBoardWithVerticalMove = listOf(
            listOf(Tile(1, 2), Tile(2, 4), Tile(3, 2), Tile(4, 4)),
            listOf(Tile(5, 2), Tile(6, 2), Tile(7, 4), Tile(8, 2)),
            listOf(Tile(9, 2), Tile(10, 4), Tile(11, 2), Tile(12, 4)),
            listOf(Tile(13, 4), Tile(14, 2), Tile(15, 4), Tile(16, 2))
        )
        assertFalse(GameEngine.isGameOver(fullBoardWithVerticalMove))

        val boardWithEmptySpace = listOf(
            listOf(Tile(1, 2), Tile(2, 4), Tile(3, 2), null),
            listOf(Tile(5, 4), Tile(6, 2), Tile(7, 4), Tile(8, 2)),
            listOf(Tile(9, 2), Tile(10, 4), Tile(11, 2), Tile(12, 4)),
            listOf(Tile(13, 4), Tile(14, 2), Tile(15, 4), Tile(16, 2))
        )
        assertFalse(GameEngine.isGameOver(boardWithEmptySpace))
    }

    @Test
    fun testCreateInitialBoard() {
        val (board, nextId) = GameEngine.createInitialBoard(4, 0.5f, 0.5f, 0.5f, 0.1f, 0)
        assertEquals(2, nextId)
        val tiles = board.flatten().filterNotNull()
        assertEquals(2, tiles.size)
        // seed 0.5 < 0.9 => value 2
        assertEquals(2, tiles[0].value)
        assertEquals(2, tiles[1].value)
    }

    @Test
    fun testMoveValueSeed() {
        val board = listOf(
            listOf(Tile(1, 2), null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        // Move that causes a new tile to appear
        // valueSeed 0.95 >= 0.9 => value 4
        val result = GameEngine.move(board, Direction.RIGHT, 0.95f, 0.5f, 10)
        val newTiles = result.board.flatten().filterNotNull().filter { it.id == 10 }
        assertEquals(1, newTiles.size)
        assertEquals(4, newTiles[0].value)
        
        // valueSeed 0.5 < 0.9 => value 2
        val result2 = GameEngine.move(board, Direction.RIGHT, 0.5f, 0.5f, 10)
        val newTiles2 = result2.board.flatten().filterNotNull().filter { it.id == 10 }
        assertEquals(2, newTiles2[0].value)
    }

    @Test
    fun testMoveNoChange() {
        val board = listOf(
            listOf(null, null, null, Tile(1, 2)),
            listOf(null, null, null, null),
            listOf(null, null, null, null),
            listOf(null, null, null, null)
        )
        val result = GameEngine.move(board, Direction.RIGHT, 0.5f, 0.5f, 10)
        assertFalse(result.hasChanged)
        assertEquals(board, result.board)
    }

    @Test
    fun testCreateDailyBoardBranches() {
        val (board1, _) = GameEngine.createDailyBoard(4, 12345L)
        val (board2, _) = GameEngine.createDailyBoard(4, 12345L)
        assertEquals(board1.flatten().map { it?.value }, board2.flatten().map { it?.value })
        
        val boardLarge = GameEngine.createDailyBoard(10, 999L).first.flatten().filterNotNull().map { it.value }
        val thresholds = listOf(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
        for (t in thresholds) {
            assertTrue("Should contain $t", boardLarge.contains(t))
        }
    }

    @Test
    fun testMoveWhenFullNoMerge() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4), Tile(3, 8), Tile(4, 16)),
            listOf(Tile(5, 32), Tile(6, 64), Tile(7, 128), Tile(8, 256)),
            listOf(Tile(9, 512), Tile(10, 1024), Tile(11, 2048), Tile(12, 4096)),
            listOf(Tile(13, 8192), Tile(14, 16384), Tile(15, 32768), Tile(16, 65536))
        )
        val result = GameEngine.move(board, Direction.UP, 0.5f, 0.5f, 100)
        assertFalse(result.hasChanged)
    }

    @Test
    fun testMoveWithNoTiles() {
        val board = List(4) { List<Tile?>(4) { null } }
        val result = GameEngine.move(board, Direction.LEFT, 0.5f, 0.5f, 10)
        assertFalse(result.hasChanged)
    }

    @Test
    fun testMergeAtEnd() {
        val board = listOf(listOf(null, null, Tile(1, 2), Tile(2, 2)))
        val result = GameEngine.move(board, Direction.LEFT, 0.5f, 0.5f, 10)
        assertEquals(4, result.board[0][0]?.value)
    }

    @Test
    fun testMoveWhenGameOver() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4)),
            listOf(Tile(3, 4), Tile(4, 2))
        )
        val result = GameEngine.move(board, Direction.UP, 0.5f, 0.5f, 10)
        assertFalse(result.hasChanged)
    }

    @Test
    fun testCreateInitialBoardEdgeSeeds() {
        val (b, _) = GameEngine.createInitialBoard(2, 0.0f, 0.0f, 1.0f, 1.0f, 0)
        assertNotNull(b[0][0])
        assertNotNull(b[1][1])
    }

    @Test
    fun testCreateInitialBoardWhenFull() {
        val (b, nextId) = GameEngine.createInitialBoard(1, 0.5f, 0.5f, 0.5f, 0.5f, 0)
        assertEquals(1, nextId)
        assertNotNull(b[0][0])
    }

    @Test
    fun testIsGameOverSmall() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4)),
            listOf(Tile(3, 4), Tile(4, 8))
        )
        assertTrue(GameEngine.isGameOver(board))
        
        val boardWithMove = listOf(
            listOf(Tile(1, 2), Tile(2, 2)),
            listOf(Tile(3, 4), Tile(4, 8))
        )
        assertFalse(GameEngine.isGameOver(boardWithMove))
    }

    @Test
    fun testCreateDailyBoardValueDistribution() {
        // We want to ensure all branches of the 'when' in createDailyBoard are reachable.
        // Instead of relying on a large board, we can check multiple seeds.
        val valuesSeen = mutableSetOf<Int>()
        for (seed in 1..100L) {
            val (board, _) = GameEngine.createDailyBoard(4, seed)
            board.flatten().filterNotNull().forEach { valuesSeen.add(it.value) }
        }
        val expectedValues = listOf(2, 4, 8, 16, 32, 64, 128, 256, 512, 1024)
        for (value in expectedValues) {
            assertTrue("Should have seen value $value", valuesSeen.contains(value))
        }
    }
}
