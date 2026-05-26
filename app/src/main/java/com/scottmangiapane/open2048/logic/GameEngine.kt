package com.scottmangiapane.open2048.logic

import com.scottmangiapane.open2048.model.Tile

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class MoveResult(
    val board: List<List<Tile?>>,
    val scoreGained: Int,
    val nextId: Int,
    val hasChanged: Boolean
)

class GameEngine(private val size: Int = 4) {

    fun createInitialBoard(
        seedValue1: Float, seedPos1: Float,
        seedValue2: Float, seedPos2: Float,
        startId: Int
    ): Pair<List<List<Tile?>>, Int> {
        val board = MutableList(size) { MutableList<Tile?>(size) { null } }
        var currentId = startId
        currentId = addTile(board, seedValue1, seedPos1, currentId)
        currentId = addTile(board, seedValue2, seedPos2, currentId)
        return board to currentId
    }

    private fun addTile(
        board: MutableList<MutableList<Tile?>>,
        valueSeed: Float,
        posSeed: Float,
        id: Int
    ): Int {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == null) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val index = (posSeed * emptyCells.size).toInt().coerceIn(0, emptyCells.size - 1)
            val (r, c) = emptyCells[index]
            board[r][c] = Tile(id = id, value = if (valueSeed < 0.9f) 2 else 4, isNew = true)
            return id + 1
        }
        return id
    }

    fun move(
        board: List<List<Tile?>>,
        direction: Direction,
        valueSeed: Float,
        posSeed: Float,
        nextId: Int
    ): MoveResult {
        var scoreGained = 0
        
        fun rotate(b: List<List<Tile?>>): List<List<Tile?>> =
            (0 until size).map { c -> (0 until size).map { r -> b[size - 1 - r][c] } }

        val transformed = when (direction) {
            Direction.LEFT -> board
            Direction.UP -> rotate(rotate(rotate(board)))
            Direction.RIGHT -> rotate(rotate(board))
            Direction.DOWN -> rotate(board)
        }

        val shifted = transformed.map { row ->
            val originalRow = row.filterNotNull()
            val newRow = mutableListOf<Tile>()
            var i = 0
            while (i < originalRow.size) {
                if (i + 1 < originalRow.size && originalRow[i].value == originalRow[i + 1].value) {
                    val mergedValue = originalRow[i].value * 2
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
            Direction.UP -> rotate(shifted)
            Direction.RIGHT -> rotate(rotate(shifted))
            Direction.DOWN -> rotate(rotate(rotate(shifted)))
        }

        val hasChanged = board.flatten().map { it?.id to it?.value } != 
                         finalBoard.flatten().map { it?.id to it?.value }

        return if (hasChanged) {
            val mutableFinal = finalBoard.map { it.toMutableList() }.toMutableList()
            val finalNextId = addTile(mutableFinal, valueSeed, posSeed, nextId)
            MoveResult(mutableFinal, scoreGained, finalNextId, true)
        } else {
            MoveResult(board, 0, nextId, false)
        }
    }

    fun isGameOver(board: List<List<Tile?>>): Boolean {
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == null) return false
                val val1 = board[r][c]?.value
                if (r + 1 < size && board[r + 1][c]?.value == val1) return false
                if (c + 1 < size && board[r][c + 1]?.value == val1) return false
            }
        }
        return true
    }
}
