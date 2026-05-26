package com.scottmangiapane.open2048.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceRepository(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val BOARD_KEY = stringPreferencesKey("board")
        private val SCORE_KEY = intPreferencesKey("score")
        private val NEXT_ID_KEY = intPreferencesKey("next_id")
        private val NEXT_VALUE_SEED_KEY = floatPreferencesKey("next_value_seed")
        private val NEXT_POS_SEED_KEY = floatPreferencesKey("next_pos_seed")
        private val GAME_MODE_KEY = stringPreferencesKey("game_mode")
        private val TIME_LEFT_KEY = longPreferencesKey("time_left")
        private val MOVES_COUNT_KEY = intPreferencesKey("moves_count")
        private val ELAPSED_TIME_KEY = longPreferencesKey("elapsed_time")
        
        fun getBestScoreKey(modeId: String) = intPreferencesKey("best_score_$modeId")
    }

    val theme: Flow<AppTheme?> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY]?.let { AppTheme.valueOf(it) }
    }

    fun getBestScore(modeId: String): Flow<Int> = context.dataStore.data.map { 
        it[getBestScoreKey(modeId)] ?: 0 
    }

    val savedGameState: Flow<GameState?> = context.dataStore.data.map { preferences ->
        val boardString = preferences[BOARD_KEY] ?: return@map null
        val board = deserializeBoard(boardString)
        val modeString = preferences[GAME_MODE_KEY] ?: "classic:4"
        val themeString = preferences[THEME_KEY] ?: AppTheme.LIGHT.name
        GameState(
            board = board,
            score = preferences[SCORE_KEY] ?: 0,
            theme = AppTheme.valueOf(themeString),
            nextId = preferences[NEXT_ID_KEY] ?: 0,
            nextValueSeed = preferences[NEXT_VALUE_SEED_KEY] ?: 0f,
            nextPosSeed = preferences[NEXT_POS_SEED_KEY] ?: 0f,
            gameMode = deserializeGameMode(modeString),
            timeLeftMs = preferences[TIME_LEFT_KEY],
            movesCount = preferences[MOVES_COUNT_KEY] ?: 0,
            elapsedTimeMs = preferences[ELAPSED_TIME_KEY] ?: 0L
        )
    }

    suspend fun updateBestScore(modeId: String, score: Int) {
        val key = getBestScoreKey(modeId)
        context.dataStore.edit { preferences ->
            val currentBest = preferences[key] ?: 0
            if (score > currentBest) preferences[key] = score
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME_KEY] = theme.name }
    }

    suspend fun saveGameState(state: GameState) {
        context.dataStore.edit { preferences ->
            preferences[BOARD_KEY] = serializeBoard(state.board)
            preferences[SCORE_KEY] = state.score
            preferences[NEXT_ID_KEY] = state.nextId
            preferences[NEXT_VALUE_SEED_KEY] = state.nextValueSeed
            preferences[NEXT_POS_SEED_KEY] = state.nextPosSeed
            preferences[GAME_MODE_KEY] = serializeGameMode(state.gameMode)
            preferences[MOVES_COUNT_KEY] = state.movesCount
            preferences[ELAPSED_TIME_KEY] = state.elapsedTimeMs
            state.timeLeftMs?.let { preferences[TIME_LEFT_KEY] = it } ?: preferences.remove(TIME_LEFT_KEY)
        }
    }

    private fun serializeBoard(board: List<List<Tile?>>): String =
        board.asSequence().flatten().joinToString(",") { it?.let { "${it.id}:${it.value}" } ?: "n" }

    private fun deserializeBoard(data: String): List<List<Tile?>> {
        val flatList = data.split(",").map { s ->
            if (s == "n") null
            else s.split(":").let { Tile(id = it[0].toInt(), value = it[1].toInt()) }
        }
        val size = kotlin.math.sqrt(flatList.size.toDouble()).toInt()
        return flatList.chunked(size)
    }

    private fun serializeGameMode(mode: GameMode): String = when (mode) {
        is GameMode.Classic -> "classic:${mode.size}"
        is GameMode.Blitz -> "blitz:${mode.durationMinutes}"
        is GameMode.Daily -> "daily:${mode.year}:${mode.month}:${mode.day}"
    }

    private fun deserializeGameMode(data: String): GameMode {
        val parts = data.split(":")
        return when (parts[0]) {
            "blitz" -> GameMode.Blitz(parts[1].toInt())
            "daily" -> GameMode.Daily(parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
            else -> GameMode.Classic(parts.getOrNull(1)?.toInt() ?: 4)
        }
    }
}
