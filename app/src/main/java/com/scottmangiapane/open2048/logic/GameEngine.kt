package com.scottmangiapane.open2048.logic

import com.scottmangiapane.open2048.model.Tile
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class MoveResult(
    val board: List<List<Tile?>>,
    val scoreGained: Int,
    val nextId: Int,
    val hasChanged: Boolean,
)

object GameEngine {

    fun createInitialBoard(
        size: Int,
        seedValue1: Float,
        seedPos1: Float,
        seedValue2: Float,
        seedPos2: Float,
        startId: Int,
    ): Pair<List<List<Tile?>>, Int> {
        val board = MutableList(size) { MutableList<Tile?>(size) { null } }
        var currentId = startId
        currentId = addTile(board, seedValue1, seedPos1, currentId)
        currentId = addTile(board, seedValue2, seedPos2, currentId)
        return board to currentId
    }

    fun createDailyBoard(size: Int, seed: Long): Pair<List<List<Tile?>>, Int> {
        val board = MutableList(size) { MutableList<Tile?>(size) { null } }
        val random = Random(seed)
        var currentId = 0
        
        val numTiles = (size * size * 0.85).toInt()
        repeat(numTiles) {
            val valueSeed = random.nextFloat()
            val value = when {
                valueSeed < 0.15f -> 2
                valueSeed < 0.30f -> 4
                valueSeed < 0.45f -> 8
                valueSeed < 0.55f -> 16
                valueSeed < 0.65f -> 32
                valueSeed < 0.75f -> 64
                valueSeed < 0.82f -> 128
                valueSeed < 0.88f -> 256
                valueSeed < 0.93f -> 512
                else -> 1024
            }
            currentId = addTileWithValue(board, value, random.nextFloat(), currentId)
        }
        return board to currentId
    }

    private fun addTileWithValue(
        board: MutableList<MutableList<Tile?>>,
        value: Int,
        posSeed: Float,
        id: Int
    ): Int {
        val size = board.size
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == null) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val index = (posSeed * emptyCells.size).toInt().coerceIn(0, emptyCells.size - 1)
            val (r, c) = emptyCells[index]
            board[r][c] = Tile(id = id, value = value, isNew = true)
            return id + 1
        }
        return id
    }

    private fun addTile(
        board: MutableList<MutableList<Tile?>>,
        valueSeed: Float,
        posSeed: Float,
        id: Int
    ): Int {
        val value = if (valueSeed < 0.9f) 2 else 4
        return addTileWithValue(board, value, posSeed, id)
    }

    fun move(
        board: List<List<Tile?>>,
        direction: Direction,
        valueSeed: Float,
        posSeed: Float,
        nextId: Int
    ): MoveResult {
        val size = board.size
        var scoreGained = 0

        val transformed = when (direction) {
            Direction.LEFT -> board
            Direction.UP -> rotate90CounterClockwise(board)
            Direction.RIGHT -> rotate180(board)
            Direction.DOWN -> rotate90Clockwise(board)
        }

        val shifted = transformed.map { row ->
            val originalRow = row.filterNotNull()
            val newRow = mutableListOf<Tile>()
            var i = 0
            while (i < originalRow.size) {
                if (i + 1 < originalRow.size && originalRow[i].value == originalRow[i + 1].value) {
                    val mergedValue = originalRow[i].value * 2
                    // Use the ID of the tile being merged into
                    newRow.add(Tile(id = originalRow[i + 1].id, value = mergedValue))
                    scoreGained += mergedValue
                    i += 2
                } else {
                    newRow.add(originalRow[i].copy(isNew = false))
                    i++
                }
            }
            newRow + List(size - newRow.size) { null }
        }

        val finalBoard = when (direction) {
            Direction.LEFT -> shifted
            Direction.UP -> rotate90Clockwise(shifted)
            Direction.RIGHT -> rotate180(shifted)
            Direction.DOWN -> rotate90CounterClockwise(shifted)
        }

        val hasChanged = board.map { row -> row.map { it?.value } } != 
                         finalBoard.map { row -> row.map { it?.value } }

        return if (hasChanged) {
            val mutableFinal = finalBoard.map { it.toMutableList() }.toMutableList()
            val finalNextId = addTile(mutableFinal, valueSeed, posSeed, nextId)
            MoveResult(
                board = mutableFinal,
                scoreGained = scoreGained,
                nextId = finalNextId,
                hasChanged = true,
            )
        } else {
            MoveResult(
                board = board,
                scoreGained = 0,
                nextId = nextId,
                hasChanged = false,
            )
        }
    }

    private fun rotate90Clockwise(board: List<List<Tile?>>): List<List<Tile?>> {
        val size = board.size
        return (0 until size).map { c -> (size - 1 downTo 0).map { r -> board[r][c] } }
    }

    private fun rotate90CounterClockwise(board: List<List<Tile?>>): List<List<Tile?>> {
        val size = board.size
        return (size - 1 downTo 0).map { c -> (0 until size).map { r -> board[r][c] } }
    }

    private fun rotate180(board: List<List<Tile?>>): List<List<Tile?>> {
        return board.reversed().map { it.reversed() }
    }

    fun isGameOver(board: List<List<Tile?>>): Boolean {
        if (board.isEmpty()) return false
        val size = board.size
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == null) return false
                val current = board[r][c]?.value ?: continue
                if (r + 1 < size && board[r + 1][c]?.value == current) return false
                if (c + 1 < size && board[r][c + 1]?.value == current) return false
            }
        }
        return true
    }
}
