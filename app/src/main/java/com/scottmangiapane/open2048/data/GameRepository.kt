package com.scottmangiapane.open2048.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.scottmangiapane.open2048.model.Tile
import com.scottmangiapane.open2048.ui.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameDataStore by preferencesDataStore(name = "game_state")

class GameRepository(private val context: Context) {
    private val BOARD_KEY = stringPreferencesKey("board")
    private val SCORE_KEY = intPreferencesKey("score")
    private val NEXT_VALUE_SEED_KEY = floatPreferencesKey("next_value_seed")
    private val NEXT_POS_SEED_KEY = floatPreferencesKey("next_pos_seed")
    private val NEXT_ID_KEY = intPreferencesKey("next_id")

    val savedGameState: Flow<GameState?> = context.gameDataStore.data.map { preferences ->
        val boardString = preferences[BOARD_KEY] ?: return@map null
        val score = preferences[SCORE_KEY] ?: 0
        val nextValueSeed = preferences[NEXT_VALUE_SEED_KEY] ?: 0f
        val nextPosSeed = preferences[NEXT_POS_SEED_KEY] ?: 0f
        val nextId = preferences[NEXT_ID_KEY] ?: 0

        val board = deserializeBoard(boardString)
        GameState(
            board = board,
            score = score,
            nextValueSeed = nextValueSeed,
            nextPosSeed = nextPosSeed,
            nextId = nextId
        )
    }

    suspend fun saveGameState(state: GameState) {
        context.gameDataStore.edit { preferences ->
            preferences[BOARD_KEY] = serializeBoard(state.board)
            preferences[SCORE_KEY] = state.score
            preferences[NEXT_VALUE_SEED_KEY] = state.nextValueSeed
            preferences[NEXT_POS_SEED_KEY] = state.nextPosSeed
            preferences[NEXT_ID_KEY] = state.nextId
        }
    }

    suspend fun clearSavedState() {
        context.gameDataStore.edit { it.clear() }
    }

    private fun serializeBoard(board: List<List<Tile?>>): String {
        return board.flatten().joinToString(",") { tile ->
            if (tile == null) "n" else "${tile.id}:${tile.value}"
        }
    }

    private fun deserializeBoard(data: String): List<List<Tile?>> {
        val flatList = data.split(",").map { s ->
            if (s == "n") null
            else {
                val parts = s.split(":")
                Tile(id = parts[0].toInt(), value = parts[1].toInt())
            }
        }
        return flatList.chunked(4)
    }
}
