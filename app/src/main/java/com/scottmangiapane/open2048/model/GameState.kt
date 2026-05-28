package com.scottmangiapane.open2048.model

data class GameState(
    val board: List<List<Tile?>> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val canUndo: Boolean = false,
    val theme: AppTheme? = null,
    val nextId: Int = 0,
    val nextValueSeed: Float = 0f,
    val nextPosSeed: Float = 0f,
    val gameMode: GameMode = GameMode.Classic(4),
    val timeLeftMs: Long? = null,
    val movesCount: Int = 0,
    val elapsedTimeMs: Long = 0L,
    val highestTile: Int = 0,
    val hasWon: Boolean = false,
    val movesToWin: Int? = null,
    val timeToWin: Long? = null
)

val GameState.canResume: Boolean get() = board.isNotEmpty() && !isGameOver && movesCount > 0
