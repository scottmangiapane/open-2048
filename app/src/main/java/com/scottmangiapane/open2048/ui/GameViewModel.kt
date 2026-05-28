package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.PreferenceRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.logic.GameTimer
import com.scottmangiapane.open2048.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceRepository(application)
    private val iconManager = IconManager(application)
    private val vibrationManager = VibrationManager(application)
    private val gameTimer = GameTimer(viewModelScope)
    
    private var previousStateForUndo: GameState? = null
    private var bestScoreJob: Job? = null
    
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Menu)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = prefs.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    init {
        observeTheme()
        loadInitialState()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            prefs.theme.collectLatest { theme ->
                val newTheme = theme ?: AppTheme.LIGHT
                _state.update { it.copy(theme = newTheme) }
                iconManager.setPendingIconUpdate(newTheme)
            }
        }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val saved = prefs.savedGameState.firstOrNull() ?: return@launch run {
                val defaultMode = GameMode.Classic(4)
                _state.update { it.copy(gameMode = defaultMode) }
                observeBestScore(defaultMode)
                _currentScreen.value = Screen.Menu
            }

            val isGameOver = saved.isGameOver || GameEngine.isGameOver(saved.board) || (saved.timeLeftMs == 0L)
            _state.update {
                saved.copy(
                    theme = it.theme,
                    isGameOver = isGameOver,
                )
            }
            observeBestScore(saved.gameMode)
            if (!isGameOver && saved.board.isNotEmpty()) {
                startTimer()
            }
        }
    }

    private fun observeBestScore(mode: GameMode) {
        bestScoreJob?.cancel()
        bestScoreJob = viewModelScope.launch {
            prefs.getBestScore(mode.id).collectLatest { best ->
                _state.update { it.copy(bestScore = best) }
            }
        }
    }

    private fun startTimer() {
        gameTimer.start { delta ->
            var shouldStop = false

            // Track total time spent only if the game has actually started (at least one move)
            val currentState = _state.value
            if (currentState.movesCount > 0) {
                viewModelScope.launch {
                    val aggregateId = if (currentState.gameMode is GameMode.Daily) "daily" else currentState.gameMode.id
                    prefs.addToTotalTime(aggregateId, delta)
                }
            }

            _state.update { state ->
                if (state.movesCount == 0) return@update state

                val newElapsed = state.elapsedTimeMs + delta
                if (state.gameMode is GameMode.Blitz) {
                    val newTime = (state.timeLeftMs ?: 0L) - delta
                    if (newTime <= 0) {
                        shouldStop = true
                        state.copy(timeLeftMs = 0, isGameOver = true, elapsedTimeMs = newElapsed)
                    } else {
                        state.copy(timeLeftMs = newTime, elapsedTimeMs = newElapsed)
                    }
                } else {
                    state.copy(elapsedTimeMs = newElapsed)
                }
            }
            if (shouldStop) {
                gameTimer.stop()
                saveGame(_state.value)
            }
        }
    }

    fun stopTimer() {
        gameTimer.stop()
        saveGame(_state.value)
    }

    fun resumeGame() {
        _currentScreen.value = Screen.Game
        if (!_state.value.isGameOver) {
            startTimer()
        }
    }

    fun navigateToMenu() {
        stopTimer()
        _currentScreen.value = Screen.Menu
    }

    fun navigateToStats() {
        _currentScreen.value = Screen.Stats
    }

    fun navigateToSettings() {
        _currentScreen.value = Screen.Settings
    }

    fun applyPendingIconChange() {
        iconManager.applyPendingIconChange()
    }

    fun restartGame(mode: GameMode? = null) {
        val currentMode = mode ?: _state.value.gameMode
        previousStateForUndo = null
        gameTimer.stop()
        
        observeBestScore(currentMode)

        val newState = createNewGameState(currentMode)
        _state.update { newState.copy(bestScore = it.bestScore, theme = it.theme) }
        saveGame(_state.value)

        _currentScreen.value = Screen.Game
        startTimer()
    }

    private fun createNewGameState(mode: GameMode): GameState {
            val (initialBoard, nextId) = when (mode) {
                is GameMode.Daily -> GameEngine.createDailyBoard(mode.size, mode.dateSeed)
                else -> GameEngine.createInitialBoard(
                    size = mode.size,
                    seedValue1 = Random.nextFloat(),
                    seedPos1 = Random.nextFloat(),
                    seedValue2 = Random.nextFloat(),
                    seedPos2 = Random.nextFloat(),
                    startId = 0,
                )
            }
        
        val (nextV, nextP) = generateNextSeeds(mode, nextId)

        return GameState(
            board = initialBoard,
            score = 0,
            isGameOver = GameEngine.isGameOver(initialBoard),
            canUndo = false,
            nextId = nextId,
            nextValueSeed = nextV,
            nextPosSeed = nextP,
            gameMode = mode,
            timeLeftMs = (mode as? GameMode.Blitz)?.let { it.durationMinutes * 60 * 1000L },
            movesCount = 0,
            elapsedTimeMs = 0L,
            highestTile = initialBoard.flatten().filterNotNull().maxOfOrNull { it.value } ?: 0
        )
    }

    private fun generateNextSeeds(mode: GameMode, nextId: Int): Pair<Float, Float> {
        return if (mode is GameMode.Daily) {
            val r = Random(mode.dateSeed + nextId)
            r.nextFloat() to r.nextFloat()
        } else {
            Random.nextFloat() to Random.nextFloat()
        }
    }

    fun undo() {
        val previous = previousStateForUndo ?: return
        previousStateForUndo = null
        val newState = previous.copy(
            bestScore = _state.value.bestScore, 
            canUndo = false,
            timeLeftMs = _state.value.timeLeftMs,
            elapsedTimeMs = _state.value.elapsedTimeMs,
        )
        _state.update { newState }
        saveGame(newState)
        if (!newState.isGameOver) startTimer()
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { prefs.setTheme(theme) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setVibrationEnabled(enabled) }
    }

    fun setControlMode(mode: ControlMode) {
        viewModelScope.launch { prefs.setControlMode(mode) }
    }

    fun setShowUndo(show: Boolean) {
        viewModelScope.launch { prefs.setShowUndo(show) }
    }

    fun setShowStopwatch(show: Boolean) {
        viewModelScope.launch { prefs.setShowStopwatch(show) }
    }

    fun setConfettiEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setConfettiEnabled(enabled) }
    }

    private val statsFlows = mutableMapOf<String, StateFlow<*>>()

    fun getBestScore(mode: GameMode): StateFlow<Int> {
        return statsFlows.getOrPut("best_${mode.id}") {
            prefs.getBestScore(mode.id)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        } as StateFlow<Int>
    }

    fun getHighestTile(mode: GameMode): StateFlow<Int> {
        return statsFlows.getOrPut("highest_${mode.id}") {
            prefs.getIntStat(PreferenceRepository.getHighestTileKey(mode.id))
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        } as StateFlow<Int>
    }

    fun getFewestMoves(mode: GameMode): StateFlow<Int> {
        return statsFlows.getOrPut("moves_${mode.id}") {
            prefs.getIntStat(PreferenceRepository.getFewestMovesKey(mode.id))
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        } as StateFlow<Int>
    }

    fun getFastestTime(mode: GameMode): StateFlow<Long> {
        return statsFlows.getOrPut("time_${mode.id}") {
            prefs.getLongStat(PreferenceRepository.getFastestTimeKey(mode.id))
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
        } as StateFlow<Long>
    }

    fun getWinCount(modeId: String): StateFlow<Int> {
        return statsFlows.getOrPut("wins_$modeId") {
            prefs.getIntStat(PreferenceRepository.getWinCountKey(modeId))
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        } as StateFlow<Int>
    }

    fun getGamesPlayed(modeId: String): StateFlow<Int> {
        return statsFlows.getOrPut("played_$modeId") {
            prefs.getIntStat(PreferenceRepository.getGamesPlayedKey(modeId))
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        } as StateFlow<Int>
    }

    fun getTotalTime(modeId: String): StateFlow<Long> {
        return statsFlows.getOrPut("total_time_$modeId") {
            prefs.getLongStat(PreferenceRepository.getTotalTimeKey(modeId))
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
        } as StateFlow<Long>
    }

    private fun saveGame(state: GameState) {
        viewModelScope.launch {
            prefs.saveGameState(state)
        }
    }

    fun move(direction: Direction) {
        val currentState = _state.value
        if (currentState.isGameOver) return

        val result = GameEngine.move(
            board = currentState.board,
            direction = direction,
            valueSeed = currentState.nextValueSeed,
            posSeed = currentState.nextPosSeed,
            nextId = currentState.nextId
        )

        if (!result.hasChanged) return

        // Increment games played and set initial highest tile on the first valid move
        if (currentState.movesCount == 0) {
            viewModelScope.launch {
                val aggregateId = if (currentState.gameMode is GameMode.Daily) "daily" else currentState.gameMode.id
                prefs.incrementGamesPlayed(aggregateId)
                prefs.updateHighestTile(currentState.gameMode.id, currentState.highestTile)
            }
        }

        val newScore = currentState.score + result.scoreGained
        val bestScore = maxOf(currentState.bestScore, newScore)
        if (newScore > currentState.bestScore) {
            viewModelScope.launch { prefs.updateBestScore(currentState.gameMode.id, newScore) }
        }

        val isGameOver = GameEngine.isGameOver(result.board) || (currentState.timeLeftMs == 0L)
        if (isGameOver) stopTimer()

        if (userPreferences.value.vibrationEnabled) {
            vibrationManager.vibrateForScore(result.scoreGained)
        }

        val (nextV, nextP) = generateNextSeeds(currentState.gameMode, result.nextId)
        val maxTile = result.board.flatten().filterNotNull().maxOfOrNull { it.value } ?: 0
        val reached2048ThisMove = maxTile >= 2048 && !currentState.hasReached2048
        
        val movesTo2048 = if (reached2048ThisMove) currentState.movesCount + 1 else currentState.movesTo2048
        val timeTo2048 = if (reached2048ThisMove) currentState.elapsedTimeMs else currentState.timeTo2048

        if (reached2048ThisMove) {
            viewModelScope.launch {
                prefs.incrementWinCount(if (currentState.gameMode is GameMode.Daily) "daily" else currentState.gameMode.id)
                prefs.updateFewestMoves(currentState.gameMode.id, movesTo2048!!)
                prefs.updateFastestTime(currentState.gameMode.id, timeTo2048!!)
            }
        }

        if (maxTile > currentState.highestTile) {
            viewModelScope.launch {
                prefs.updateHighestTile(currentState.gameMode.id, maxTile)
            }
        }

        previousStateForUndo = currentState
        _state.update { state ->
            state.copy(
                board = result.board,
                score = newScore,
                isGameOver = isGameOver,
                canUndo = true,
                nextId = result.nextId,
                nextValueSeed = nextV,
                nextPosSeed = nextP,
                bestScore = bestScore,
                movesCount = state.movesCount + 1,
                highestTile = maxOf(state.highestTile, maxTile),
                hasReached2048 = state.hasReached2048 || reached2048ThisMove,
                movesTo2048 = movesTo2048,
                timeTo2048 = timeTo2048
            )
        }
        saveGame(_state.value)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
