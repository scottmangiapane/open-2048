package com.scottmangiapane.open2048.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.scottmangiapane.open2048.model.*
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
        private val SOUNDS_ENABLED_KEY = booleanPreferencesKey("sounds_enabled")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val CONTROL_MODE_KEY = stringPreferencesKey("control_mode")
        private val SHOW_UNDO_KEY = booleanPreferencesKey("show_undo")
        private val SHOW_STOPWATCH_KEY = booleanPreferencesKey("show_stopwatch")
        
        fun getBestScoreKey(modeId: String) = intPreferencesKey("best_score_$modeId")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            theme = preferences[THEME_KEY]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.LIGHT,
            soundsEnabled = preferences[SOUNDS_ENABLED_KEY] ?: true,
            vibrationEnabled = preferences[VIBRATION_ENABLED_KEY] ?: true,
            controlMode = preferences[CONTROL_MODE_KEY]?.let { runCatching { ControlMode.valueOf(it) }.getOrNull() } ?: ControlMode.BOTH,
            showUndo = preferences[SHOW_UNDO_KEY] ?: true,
            showStopwatch = preferences[SHOW_STOPWATCH_KEY] ?: true
        )
    }

    val theme: Flow<AppTheme?> = userPreferences.map { it.theme }

    val soundsEnabled: Flow<Boolean> = userPreferences.map { it.soundsEnabled }

    fun getBestScore(modeId: String): Flow<Int> = context.dataStore.data.map { 
        it[getBestScoreKey(modeId)] ?: 0 
    }

    val savedGameState: Flow<GameState?> = context.dataStore.data.map { preferences ->
        val boardString = preferences[BOARD_KEY] ?: return@map null
        val modeString = preferences[GAME_MODE_KEY] ?: return@map null
        
        val board = deserializeBoard(boardString)
        val mode = deserializeGameMode(modeString)
        
        if (board.isEmpty() || mode == null) return@map null

        val themeString = preferences[THEME_KEY] ?: AppTheme.LIGHT.name
        val theme = runCatching { AppTheme.valueOf(themeString) }.getOrNull() ?: AppTheme.LIGHT

        GameState(
            board = board,
            score = preferences[SCORE_KEY] ?: 0,
            theme = theme,
            nextId = preferences[NEXT_ID_KEY] ?: 0,
            nextValueSeed = preferences[NEXT_VALUE_SEED_KEY] ?: 0f,
            nextPosSeed = preferences[NEXT_POS_SEED_KEY] ?: 0f,
            gameMode = mode,
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

    suspend fun setSoundsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUNDS_ENABLED_KEY] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VIBRATION_ENABLED_KEY] = enabled }
    }

    suspend fun setControlMode(mode: ControlMode) {
        context.dataStore.edit { it[CONTROL_MODE_KEY] = mode.name }
    }

    suspend fun setShowUndo(show: Boolean) {
        context.dataStore.edit { it[SHOW_UNDO_KEY] = show }
    }

    suspend fun setShowStopwatch(show: Boolean) {
        context.dataStore.edit { it[SHOW_STOPWATCH_KEY] = show }
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
        board.flatten().joinToString(",") { tile ->
            tile?.let { "${it.id}:${it.value}" } ?: "n"
        }

    private fun deserializeBoard(data: String): List<List<Tile?>> {
        if (data.isBlank()) return emptyList()
        val parts = data.split(",")
        val flatList = parts.map { s ->
            if (s == "n") null
            else {
                val tileParts = s.split(":")
                val id = tileParts.getOrNull(0)?.toIntOrNull() ?: return emptyList()
                val value = tileParts.getOrNull(1)?.toIntOrNull() ?: return emptyList()
                Tile(id, value)
            }
        }
        val size = kotlin.math.sqrt(flatList.size.toDouble()).toInt()
        return if (size * size == flatList.size) flatList.chunked(size) else emptyList()
    }

    private fun serializeGameMode(mode: GameMode): String = when (mode) {
        is GameMode.Classic -> "classic:${mode.size}"
        is GameMode.Blitz -> "blitz:${mode.durationMinutes}"
        is GameMode.Daily -> "daily:${mode.year}:${mode.month}:${mode.day}"
    }

    private fun deserializeGameMode(data: String): GameMode? {
        val parts = data.split(":")
        return when (parts.getOrNull(0)) {
            "classic" -> parts.getOrNull(1)?.toIntOrNull()?.let { GameMode.Classic(it) }
            "blitz" -> parts.getOrNull(1)?.toIntOrNull()?.let { GameMode.Blitz(it) }
            "daily" -> {
                val y = parts.getOrNull(1)?.toIntOrNull() ?: return null
                val m = parts.getOrNull(2)?.toIntOrNull() ?: return null
                val d = parts.getOrNull(3)?.toIntOrNull() ?: return null
                GameMode.Daily(y, m, d)
            }
            else -> null
        }
    }
}
