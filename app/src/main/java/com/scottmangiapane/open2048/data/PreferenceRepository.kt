package com.scottmangiapane.open2048.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.scottmangiapane.open2048.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.sqrt

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
        private val HIGHEST_TILE_KEY = intPreferencesKey("highest_tile")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val CONTROL_MODE_KEY = stringPreferencesKey("control_mode")
        private val SHOW_UNDO_KEY = booleanPreferencesKey("show_undo")
        private val SHOW_STOPWATCH_KEY = booleanPreferencesKey("show_stopwatch")
        private val ANIMATION_SPEED_KEY = stringPreferencesKey("animation_speed")
        
        fun getBestScoreKey(modeId: String) = intPreferencesKey("best_score_$modeId")
        fun getHighestTileKey(modeId: String) = intPreferencesKey("highest_tile_$modeId")
        fun getFewestMovesKey(modeId: String) = intPreferencesKey("fewest_moves_$modeId")
        fun getFastestTimeKey(modeId: String) = longPreferencesKey("fastest_time_$modeId")
        fun getWinCountKey(modeId: String) = intPreferencesKey("win_count_$modeId")
        fun getGamesPlayedKey(modeId: String) = intPreferencesKey("games_played_$modeId")
        fun getTotalTimeKey(modeId: String) = longPreferencesKey("total_time_$modeId")

        private val HAS_WON_KEY = booleanPreferencesKey("has_won")
        private val MOVES_TO_WIN_KEY = intPreferencesKey("moves_to_win")
        private val TIME_TO_WIN_KEY = longPreferencesKey("time_to_win")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            theme = preferences[THEME_KEY]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.LIGHT,
            vibrationEnabled = preferences[VIBRATION_ENABLED_KEY] ?: true,
            controlMode = preferences[CONTROL_MODE_KEY]?.let { runCatching { ControlMode.valueOf(it) }.getOrNull() } ?: ControlMode.GESTURES,
            showUndo = preferences[SHOW_UNDO_KEY] ?: true,
            showStopwatch = preferences[SHOW_STOPWATCH_KEY] ?: true,
            animationSpeed = preferences[ANIMATION_SPEED_KEY]?.let { runCatching { AnimationSpeed.valueOf(it) }.getOrNull() } ?: AnimationSpeed.NORMAL
        )
    }

    val theme: Flow<AppTheme?> = userPreferences.map { it.theme }

    fun getBestScore(modeId: String): Flow<Int> = context.dataStore.data.map { 
        it[getBestScoreKey(modeId)] ?: 0 
    }

    val savedGameState: Flow<GameState?> = context.dataStore.data.map { preferences ->
        val boardString = preferences[BOARD_KEY] ?: return@map null
        val modeString = preferences[GAME_MODE_KEY] ?: return@map null
        
        val board = deserializeBoard(boardString)
        val mode = GameMode.fromId(modeString)
        
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
            elapsedTimeMs = preferences[ELAPSED_TIME_KEY] ?: 0L,
            highestTile = preferences[HIGHEST_TILE_KEY] ?: 0,
            hasWon = preferences[HAS_WON_KEY] ?: preferences[booleanPreferencesKey("has_reached_2048")] ?: false,
            movesToWin = preferences[MOVES_TO_WIN_KEY] ?: preferences[intPreferencesKey("moves_to_2048")],
            timeToWin = preferences[TIME_TO_WIN_KEY] ?: preferences[longPreferencesKey("time_to_2048")]
        )
    }

    fun getIntStat(key: Preferences.Key<Int>): Flow<Int> = context.dataStore.data.map { it[key] ?: 0 }
    fun getLongStat(key: Preferences.Key<Long>): Flow<Long> = context.dataStore.data.map { it[key] ?: 0L }

    suspend fun updateBestScore(modeId: String, score: Int) {
        val key = getBestScoreKey(modeId)
        context.dataStore.edit { preferences ->
            val currentBest = preferences[key] ?: 0
            if (score > currentBest) preferences[key] = score
        }
    }

    suspend fun updateHighestTile(modeId: String, tile: Int) {
        val key = getHighestTileKey(modeId)
        context.dataStore.edit { preferences ->
            val current = preferences[key] ?: 0
            if (tile > current) preferences[key] = tile
        }
    }

    suspend fun updateFewestMoves(modeId: String, moves: Int) {
        val key = getFewestMovesKey(modeId)
        context.dataStore.edit { preferences ->
            val current = preferences[key] ?: Int.MAX_VALUE
            if (moves < current) preferences[key] = moves
        }
    }

    suspend fun updateFastestTime(modeId: String, timeMs: Long) {
        val key = getFastestTimeKey(modeId)
        context.dataStore.edit { preferences ->
            val current = preferences[key] ?: Long.MAX_VALUE
            if (timeMs < current) preferences[key] = timeMs
        }
    }

    suspend fun incrementWinCount(modeId: String) {
        val key = getWinCountKey(modeId)
        context.dataStore.edit { preferences ->
            val current = preferences[key] ?: 0
            preferences[key] = current + 1
        }
    }

    suspend fun incrementGamesPlayed(modeId: String) {
        val key = getGamesPlayedKey(modeId)
        context.dataStore.edit { preferences ->
            val current = preferences[key] ?: 0
            preferences[key] = current + 1
        }
    }

    suspend fun addToTotalTime(modeId: String, timeMs: Long) {
        val key = getTotalTimeKey(modeId)
        context.dataStore.edit { preferences ->
            val current = preferences[key] ?: 0L
            preferences[key] = current + timeMs
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME_KEY] = theme.name }
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

    suspend fun setAnimationSpeed(speed: AnimationSpeed) {
        context.dataStore.edit { it[ANIMATION_SPEED_KEY] = speed.name }
    }

    suspend fun saveGameState(state: GameState) {
        context.dataStore.edit { preferences ->
            if (state.movesCount > 0) {
                preferences[BOARD_KEY] = serializeBoard(state.board)
                preferences[SCORE_KEY] = state.score
                preferences[NEXT_ID_KEY] = state.nextId
                preferences[NEXT_VALUE_SEED_KEY] = state.nextValueSeed
                preferences[NEXT_POS_SEED_KEY] = state.nextPosSeed
                preferences[GAME_MODE_KEY] = state.gameMode.id
                preferences[MOVES_COUNT_KEY] = state.movesCount
                preferences[ELAPSED_TIME_KEY] = state.elapsedTimeMs
                preferences[HIGHEST_TILE_KEY] = state.highestTile
                preferences[HAS_WON_KEY] = state.hasWon
                state.movesToWin?.let { preferences[MOVES_TO_WIN_KEY] = it } ?: preferences.remove(MOVES_TO_WIN_KEY)
                state.timeToWin?.let { preferences[TIME_TO_WIN_KEY] = it } ?: preferences.remove(TIME_TO_WIN_KEY)
                state.timeLeftMs?.let { preferences[TIME_LEFT_KEY] = it } ?: preferences.remove(TIME_LEFT_KEY)
            } else {
                preferences.remove(BOARD_KEY)
                preferences.remove(SCORE_KEY)
                preferences.remove(NEXT_ID_KEY)
                preferences.remove(NEXT_VALUE_SEED_KEY)
                preferences.remove(NEXT_POS_SEED_KEY)
                preferences.remove(GAME_MODE_KEY)
                preferences.remove(TIME_LEFT_KEY)
                preferences.remove(MOVES_COUNT_KEY)
                preferences.remove(ELAPSED_TIME_KEY)
                preferences.remove(HIGHEST_TILE_KEY)
                preferences.remove(HAS_WON_KEY)
                preferences.remove(MOVES_TO_WIN_KEY)
                preferences.remove(TIME_TO_WIN_KEY)
            }
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
        val size = sqrt(flatList.size.toDouble()).toInt()
        return if (size * size == flatList.size) flatList.chunked(size) else emptyList()
    }
}
