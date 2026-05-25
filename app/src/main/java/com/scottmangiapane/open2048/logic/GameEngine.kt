package com.scottmangiapane.open2048.logic

import com.scottmangiapane.open2048.model.Tile

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

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
            board[r][c] = Tile(id = id, value = if (valueSeed < 0.9) 2 else 4)
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
    ): Triple<List<List<Tile?>>, Int, Int> {
        var scoreGained = 0
        val tempBoard = MutableList(size) { MutableList<Tile?>(size) { null } }

        val rotatedBoard = when (direction) {
            Direction.LEFT -> board
            Direction.RIGHT -> board.map { it.reversed() }
            Direction.UP -> (0 until size).map { c -> (0 until size).map { r -> board[r][c] } }
            Direction.DOWN -> (0 until size).map { c -> (0 until size).map { r -> board[r][c] }.reversed() }
        }

        for (r in 0 until size) {
            val row = rotatedBoard[r].filterNotNull()
            val newRow = mutableListOf<Tile?>()
            var i = 0
            while (i < row.size) {
                if (i + 1 < row.size && row[i].value == row[i + 1].value) {
                    val mergedValue = row[i].value * 2
                    newRow.add(Tile(id = row[i + 1].id, value = mergedValue))
                    scoreGained += mergedValue
                    i += 2
                } else {
                    newRow.add(row[i])
                    i++
                }
            }
            while (newRow.size < size) newRow.add(null)
            for (c in 0 until size) {
                tempBoard[r][c] = newRow[c]
            }
        }

        val finalBoard = rotateBack(tempBoard, direction)
        var finalNextId = nextId

        return if (board != finalBoard) {
            val mutableFinalBoard = finalBoard.map { it.toMutableList() }.toMutableList()
            finalNextId = addTile(mutableFinalBoard, valueSeed, posSeed, nextId)
            Triple(mutableFinalBoard, scoreGained, finalNextId)
        } else {
            Triple(board, 0, nextId)
        }
    }

    private fun rotateBack(tempBoard: List<List<Tile?>>, direction: Direction): List<List<Tile?>> {
        val result = MutableList(size) { MutableList<Tile?>(size) { null } }
        for (r in 0 until size) {
            for (c in 0 until size) {
                when (direction) {
                    Direction.LEFT -> result[r][c] = tempBoard[r][c]
                    Direction.RIGHT -> result[r][c] = tempBoard[r][size - 1 - c]
                    Direction.UP -> result[r][c] = tempBoard[c][r]
                    Direction.DOWN -> result[r][c] = tempBoard[c][size - 1 - r]
                }
            }
        }
        return result
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
