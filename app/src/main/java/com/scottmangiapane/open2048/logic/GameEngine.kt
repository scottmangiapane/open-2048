package com.scottmangiapane.open2048.logic

import com.scottmangiapane.open2048.model.Tile
import kotlin.random.Random

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

class GameEngine(private val size: Int = 4) {
    private var nextId = 0

    fun createInitialBoard(): List<List<Tile?>> {
        val board = MutableList(size) { MutableList<Tile?>(size) { null } }
        addRandomTile(board)
        addRandomTile(board)
        return board
    }

    private fun addRandomTile(board: MutableList<MutableList<Tile?>>) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == null) emptyCells.add(r to c)
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells[Random.nextInt(emptyCells.size)]
            board[r][c] = Tile(id = nextId++, value = if (Random.nextFloat() < 0.9) 2 else 4)
        }
    }

    fun move(board: List<List<Tile?>>, direction: Direction): Pair<List<List<Tile?>>, Int> {
        var scoreGained = 0
        val tempBoard = MutableList(size) { MutableList<Tile?>(size) { null } }

        // Transform board so we can always treat it as "moving left"
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
                    newRow.add(Tile(id = nextId++, value = mergedValue))
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

        // Rotate back
        val finalBoard = when (direction) {
            Direction.LEFT -> tempBoard
            Direction.RIGHT -> tempBoard.map { it.reversed() }
            Direction.UP -> (0 until size).map { r -> (0 until size).map { c -> tempBoard[c][r] } }
            Direction.DOWN -> (0 until size).map { r -> (0 until size).map { c -> tempBoard[c][size - 1 - r] } }
        }

        return if (board != finalBoard) {
            val mutableFinalBoard = finalBoard.map { it.toMutableList() }.toMutableList()
            addRandomTile(mutableFinalBoard)
            mutableFinalBoard to scoreGained
        } else {
            board to 0
        }
    }

    fun isGameOver(board: List<List<Tile?>>): Boolean {
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == null) return false
                val value = board[r][c]?.value
                if (r + 1 < size && board[r + 1][c]?.value == value) return false
                if (c + 1 < size && board[r][c + 1]?.value == value) return false
            }
        }
        return true
    }
}
