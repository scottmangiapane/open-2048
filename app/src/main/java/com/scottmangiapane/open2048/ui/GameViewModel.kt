package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.PreferenceRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.logic.GameTimer
import com.scottmangiapane.open2048.model.*
import com.scottmangiapane.open2048.ui.components.DeviceUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceRepository(application)
    private val iconManager = IconManager(application)
    private val vibrationManager = VibrationManager(application)
    private val gameTimer = GameTimer(viewModelScope)

    val hasVibrator: Boolean = vibrationManager.hasVibrator
    val hasTouch: Boolean = DeviceUtils.hasTouch(application)

    init {
        observeTheme()
        loadInitialState()
    }
    
    private var previousStateForUndo: GameState? = null
    private var bestScoreJob: Job? = null
    
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Menu)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = prefs.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

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
                    prefs.addToTotalTime(currentState.gameMode.statsId, delta)
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

    fun setAnimationSpeed(speed: AnimationSpeed) {
        viewModelScope.launch { prefs.setAnimationSpeed(speed) }
    }

    private val intStatsFlows = mutableMapOf<String, StateFlow<Int>>()
    private val longStatsFlows = mutableMapOf<String, StateFlow<Long>>()

    private fun <T> getStatFlow(
        key: String,
        cache: MutableMap<String, StateFlow<T>>,
        defaultValue: T,
        flowProvider: () -> Flow<T>
    ): StateFlow<T> {
        return cache.getOrPut(key) {
            flowProvider()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultValue)
        }
    }

    fun getBestScore(mode: GameMode): StateFlow<Int> = getStatFlow("best_${mode.id}", intStatsFlows, 0) {
        prefs.getBestScore(mode.id)
    }

    fun getHighestTile(mode: GameMode): StateFlow<Int> = getStatFlow("highest_${mode.id}", intStatsFlows, 0) {
        prefs.getIntStat(PreferenceRepository.getHighestTileKey(mode.id))
    }

    fun getFewestMoves(mode: GameMode): StateFlow<Int> = getStatFlow("moves_${mode.id}", intStatsFlows, 0) {
        prefs.getIntStat(PreferenceRepository.getFewestMovesKey(mode.id))
    }

    fun getFastestTime(mode: GameMode): StateFlow<Long> = getStatFlow("time_${mode.id}", longStatsFlows, 0L) {
        prefs.getLongStat(PreferenceRepository.getFastestTimeKey(mode.id))
    }

    fun getWinCount(modeId: String): StateFlow<Int> = getStatFlow("wins_$modeId", intStatsFlows, 0) {
        prefs.getIntStat(PreferenceRepository.getWinCountKey(modeId))
    }

    fun getGamesPlayed(modeId: String): StateFlow<Int> = getStatFlow("played_$modeId", intStatsFlows, 0) {
        prefs.getIntStat(PreferenceRepository.getGamesPlayedKey(modeId))
    }

    fun getTotalTime(modeId: String): StateFlow<Long> = getStatFlow("total_time_$modeId", longStatsFlows, 0L) {
        prefs.getLongStat(PreferenceRepository.getTotalTimeKey(modeId))
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
                prefs.incrementGamesPlayed(currentState.gameMode.statsId)
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
        val targetTile = currentState.gameMode.winCondition
        val reachedTargetThisMove = maxTile >= targetTile && !currentState.hasWon
        
        val movesToWin = if (reachedTargetThisMove) currentState.movesCount + 1 else currentState.movesToWin
        val timeToWin = if (reachedTargetThisMove) currentState.elapsedTimeMs else currentState.timeToWin

        if (reachedTargetThisMove) {
            viewModelScope.launch {
                prefs.incrementWinCount(currentState.gameMode.statsId)
                prefs.updateFewestMoves(currentState.gameMode.id, movesToWin!!)
                prefs.updateFastestTime(currentState.gameMode.id, timeToWin!!)
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
                hasWon = state.hasWon || reachedTargetThisMove,
                movesToWin = movesToWin,
                timeToWin = timeToWin
            )
        }
        saveGame(_state.value)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
