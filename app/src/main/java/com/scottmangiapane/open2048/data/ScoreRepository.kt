package com.scottmangiapane.open2048.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scores")

class ScoreRepository(private val context: Context) {
    private val BEST_SCORE_KEY = intPreferencesKey("best_score")

    val bestScore: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[BEST_SCORE_KEY] ?: 0
    }

    suspend fun updateBestScore(score: Int) {
        context.dataStore.edit { preferences ->
            val currentBest = preferences[BEST_SCORE_KEY] ?: 0
            if (score > currentBest) {
                preferences[BEST_SCORE_KEY] = score
            }
        }
    }
}
