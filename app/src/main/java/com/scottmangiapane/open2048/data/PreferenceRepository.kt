package com.scottmangiapane.open2048.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceRepository(private val context: Context) {
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val BOARD_KEY = stringPreferencesKey("board")
        private val SCORE_KEY = intPreferencesKey("score")
        private val NEXT_ID_KEY = intPreferencesKey("next_id")
        private val NEXT_VALUE_SEED_KEY = floatPreferencesKey("next_value_seed")
        private val NEXT_POS_SEED_KEY = floatPreferencesKey("next_pos_seed")
        
        fun getBestScoreKey(size: Int) = intPreferencesKey("best_score_$size")
    }

    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { it[DARK_MODE_KEY] }

    fun getBestScore(size: Int): Flow<Int> = context.dataStore.data.map { 
        it[getBestScoreKey(size)] ?: 0 
    }

    val savedGameState: Flow<GameState?> = context.dataStore.data.map { preferences ->
        val boardString = preferences[BOARD_KEY] ?: return@map null
        val board = deserializeBoard(boardString)
        GameState(
            board = board,
            score = preferences[SCORE_KEY] ?: 0,
            nextId = preferences[NEXT_ID_KEY] ?: 0,
            nextValueSeed = preferences[NEXT_VALUE_SEED_KEY] ?: 0f,
            nextPosSeed = preferences[NEXT_POS_SEED_KEY] ?: 0f,
        )
    }

    suspend fun updateBestScore(size: Int, score: Int) {
        val key = getBestScoreKey(size)
        context.dataStore.edit { preferences ->
            val currentBest = preferences[key] ?: 0
            if (score > currentBest) preferences[key] = score
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun saveGameState(state: GameState) {
        context.dataStore.edit { preferences ->
            preferences[BOARD_KEY] = serializeBoard(state.board)
            preferences[SCORE_KEY] = state.score
            preferences[NEXT_ID_KEY] = state.nextId
            preferences[NEXT_VALUE_SEED_KEY] = state.nextValueSeed
            preferences[NEXT_POS_SEED_KEY] = state.nextPosSeed
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
}
