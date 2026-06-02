package com.scottmangiapane.open2048.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.scottmangiapane.open2048.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PreferenceRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var testContext: Context
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var repository: PreferenceRepository
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Before
    fun setup() {
        testContext = ApplicationProvider.getApplicationContext()
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test.preferences_pb") }
        )
        repository = PreferenceRepository(testContext, testDataStore)
    }

    @Test
    fun testBoardSerializationAndDeserialization() {
        val board = listOf(
            listOf(Tile(1, 2), null),
            listOf(null, Tile(2, 4))
        )
        val serialized = PreferenceRepository.serializeBoard(board)
        assertEquals("1:2,n,n,2:4", serialized)

        val deserialized = PreferenceRepository.deserializeBoard(serialized)
        assertEquals(2, deserialized.size)
        assertEquals(2, deserialized[0][0]?.value)
        assertNull(deserialized[0][1])
        assertEquals(4, deserialized[1][1]?.value)
    }

    @Test
    fun testBoardDeserializationFailures() {
        assertTrue(PreferenceRepository.deserializeBoard("").isEmpty())
        assertTrue(PreferenceRepository.deserializeBoard("1:2,n,n").isEmpty()) // Not a square
        assertTrue(PreferenceRepository.deserializeBoard("1:a,n,n,n").isEmpty()) // Invalid value
        assertTrue(PreferenceRepository.deserializeBoard("a:2,n,n,n").isEmpty()) // Invalid ID
        assertTrue(PreferenceRepository.deserializeBoard("1,n,n,n").isEmpty()) // Incomplete
        assertTrue(PreferenceRepository.deserializeBoard(" ").isEmpty())
    }

    @Test
    fun testUpdateAndReadStats() = runTest(testScope.testScheduler) {
        repository.updateBestScore("classic_4", 100)
        assertEquals(100, repository.getBestScore("classic_4").first())
        
        repository.updateBestScore("classic_4", 50) // Should not update
        assertEquals(100, repository.getBestScore("classic_4").first())
        
        repository.updateHighestTile("classic_4", 1024)
        assertEquals(1024, repository.getIntStat(PreferenceRepository.getHighestTileKey("classic_4")).first())
        
        repository.updateFewestMoves("classic_4", 1000)
        repository.updateFewestMoves("classic_4", 800)
        assertEquals(800, repository.getIntStat(PreferenceRepository.getFewestMovesKey("classic_4")).first())
        
        repository.updateFastestTime("classic_4", 60000L)
        repository.updateFastestTime("classic_4", 50000L)
        assertEquals(50000L, repository.getLongStat(PreferenceRepository.getFastestTimeKey("classic_4")).first())

        repository.incrementWinCount("classic_4")
        assertEquals(1, repository.getIntStat(PreferenceRepository.getWinCountKey("classic_4")).first())

        repository.incrementGamesPlayed("classic_4")
        assertEquals(1, repository.getIntStat(PreferenceRepository.getGamesPlayedKey("classic_4")).first())

        repository.addToTotalTime("classic_4", 5000L)
        assertEquals(5000L, repository.getLongStat(PreferenceRepository.getTotalTimeKey("classic_4")).first())
    }

    @Test
    fun testUpdateStatsEdgeCases() = runTest(testScope.testScheduler) {
        // current value is larger
        testDataStore.edit { it[PreferenceRepository.getBestScoreKey("classic_4")] = 200 }
        repository.updateBestScore("classic_4", 100)
        assertEquals(200, repository.getBestScore("classic_4").first())

        // current highest tile is larger
        testDataStore.edit { it[PreferenceRepository.getHighestTileKey("classic_4")] = 4096 }
        repository.updateHighestTile("classic_4", 2048)
        assertEquals(4096, repository.getIntStat(PreferenceRepository.getHighestTileKey("classic_4")).first())

        // current fewest moves is smaller
        testDataStore.edit { it[PreferenceRepository.getFewestMovesKey("classic_4")] = 500 }
        repository.updateFewestMoves("classic_4", 600)
        assertEquals(500, repository.getIntStat(PreferenceRepository.getFewestMovesKey("classic_4")).first())

        // current fastest time is smaller
        testDataStore.edit { it[PreferenceRepository.getFastestTimeKey("classic_4")] = 30000L }
        repository.updateFastestTime("classic_4", 40000L)
        assertEquals(30000L, repository.getLongStat(PreferenceRepository.getFastestTimeKey("classic_4")).first())
    }

    @Test
    fun testSettings() = runTest(testScope.testScheduler) {
        repository.setTheme(AppTheme.DARK)
        assertEquals(AppTheme.DARK, repository.userPreferences.first().theme)
        assertEquals(AppTheme.DARK, repository.theme.first())
        
        repository.setVibrationEnabled(false)
        assertFalse(repository.userPreferences.first().vibrationEnabled)
        
        repository.setControlMode(ControlMode.ARROWS)
        assertEquals(ControlMode.ARROWS, repository.userPreferences.first().controlMode)
        
        repository.setShowUndo(false)
        assertFalse(repository.userPreferences.first().showUndo)
        
        repository.setShowStopwatch(false)
        assertFalse(repository.userPreferences.first().showStopwatch)
        
        repository.setAnimationSpeed(AnimationSpeed.SLOW)
        assertEquals(AnimationSpeed.SLOW, repository.userPreferences.first().animationSpeed)
    }

    @Test
    fun testSaveAndLoadGameState() = runTest(testScope.testScheduler) {
        val state = GameState(
            board = listOf(listOf(Tile(1, 2))),
            score = 10,
            theme = AppTheme.LIGHT,
            nextId = 2,
            nextValueSeed = 0.5f,
            nextPosSeed = 0.5f,
            gameMode = GameMode.Classic(1),
            movesCount = 1,
            hasWon = true,
            movesToWin = 100,
            timeToWin = 5000L,
            timeLeftMs = 10000L
        )
        
        repository.saveGameState(state)
        val retrieved = repository.savedGameState.first()
        
        assertNotNull(retrieved)
        assertEquals(10, retrieved?.score)
        assertTrue(retrieved!!.hasWon)
        assertEquals(100, retrieved.movesToWin)
        assertEquals(5000L, retrieved.timeToWin)
        assertEquals(10000L, retrieved.timeLeftMs)

        // Test with nulls
        repository.saveGameState(state.copy(movesToWin = null, timeToWin = null, timeLeftMs = null))
        val retrievedNulls = repository.savedGameState.first()
        assertNull(retrievedNulls?.movesToWin)
        assertNull(retrievedNulls?.timeToWin)
        assertNull(retrievedNulls?.timeLeftMs)

        // Test clear state (movesCount = 0)
        repository.saveGameState(state.copy(movesCount = 0))
        assertNull(repository.savedGameState.first())
    }

    @Test
    fun testInvalidDataFallbacks() = runTest(testScope.testScheduler) {
        testDataStore.edit { preferences ->
            preferences[stringPreferencesKey("board")] = "invalid"
            preferences[stringPreferencesKey("game_mode")] = "classic_4"
        }
        assertNull(repository.savedGameState.first())

        testDataStore.edit { preferences ->
            preferences[stringPreferencesKey("app_theme")] = "INVALID"
            preferences[stringPreferencesKey("control_mode")] = "INVALID"
        }
        val prefs = repository.userPreferences.first()
        assertEquals(AppTheme.LIGHT, prefs.theme)
        assertEquals(ControlMode.GESTURES, prefs.controlMode)
    }

    @Test
    fun testLegacyKeyMapping() = runTest(testScope.testScheduler) {
        testDataStore.edit { preferences ->
            preferences[stringPreferencesKey("board")] = "1:2,n,n,2:4"
            preferences[stringPreferencesKey("game_mode")] = "classic_4"
            preferences[booleanPreferencesKey("has_reached_2048")] = true
            preferences[intPreferencesKey("moves_to_2048")] = 123
            preferences[longPreferencesKey("time_to_2048")] = 456L
        }
        val state = repository.savedGameState.first()
        assertTrue(state?.hasWon == true)
        assertEquals(123, state?.movesToWin)
        assertEquals(456L, state?.timeToWin)
    }

    @Test
    fun testGameModeMapping() {
        assertNotNull(GameMode.fromId("classic_4"))
        assertNotNull(GameMode.fromId("blitz_2"))
        assertNotNull(GameMode.fromId("daily_2024_05_20"))
        assertNull(GameMode.fromId("invalid"))
        assertNull(GameMode.fromId(""))
        
        // Blitz pattern
        assertNull(GameMode.fromId("blitz_abc"))
        
        // Daily pattern
        assertNull(GameMode.fromId("daily_2024_05"))
        assertNull(GameMode.fromId("daily_2024_05_ab"))
    }
}
