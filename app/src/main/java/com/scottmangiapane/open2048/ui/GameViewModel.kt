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

class GameViewModel @JvmOverloads constructor(
    application: Application,
    private val prefs: PreferenceRepository = PreferenceRepository(application),
    private val iconManager: IconManager = IconManager(application),
    private val vibrationManager: VibrationManager = VibrationManager(application),
    providedGameTimer: GameTimer? = null
) : AndroidViewModel(application) {
    internal val gameTimer: GameTimer = providedGameTimer ?: GameTimer(viewModelScope)

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
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

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
            _state.update { current ->
                saved.copy(
                    theme = current.theme ?: saved.theme,
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
            prefs.getBestScore(mode.statsId).collect { best ->
                _state.update { it.copy(bestScore = best) }
            }
        }
    }

    fun startTimer() {
        stopTimer()
        gameTimer.start { elapsed ->
            _state.update { current ->
                val newElapsed = current.elapsedTimeMs + elapsed
                val newTimeLeft = current.timeLeftMs?.let { (it - elapsed).coerceAtLeast(0) }
                val isTimeUp = newTimeLeft == 0L
                
                if (isTimeUp && !current.isGameOver) {
                    stopTimer()
                    current.copy(
                        elapsedTimeMs = newElapsed,
                        timeLeftMs = newTimeLeft,
                        isGameOver = true,
                    )
                } else {
                    current.copy(
                        elapsedTimeMs = newElapsed,
                        timeLeftMs = newTimeLeft,
                    )
                }
            }
        }
    }

    fun stopTimer() {
        gameTimer.stop()
    }

    fun resumeGame() {
        _currentScreen.value = Screen.Game
        startTimer()
    }

    fun navigateToMenu() {
        _currentScreen.value = Screen.Menu
        stopTimer()
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
        val newMode = mode ?: state.value.gameMode
        val newState = createNewGameState(newMode)
        
        viewModelScope.launch {
            prefs.saveGameState(newState)
            prefs.incrementGamesPlayed(newMode.groupStatsId)
        }
        
        _state.value = newState
        observeBestScore(newMode)
        _currentScreen.value = Screen.Game
        startTimer()
    }

    private fun createNewGameState(mode: GameMode): GameState {
        val (board, nextId) = if (mode is GameMode.Daily) {
            GameEngine.createDailyBoard(mode.size, mode.dateSeed)
        } else {
            val random = Random
            GameEngine.createInitialBoard(
                size = mode.size,
                seedValue1 = random.nextFloat(),
                seedPos1 = random.nextFloat(),
                seedValue2 = random.nextFloat(),
                seedPos2 = random.nextFloat(),
                startId = 0
            )
        }
        
        val (nextValueSeed, nextPosSeed) = generateNextSeeds(mode, 2)
        
        return GameState(
            board = board,
            score = 0,
            theme = _state.value.theme,
            nextId = nextId,
            nextValueSeed = nextValueSeed,
            nextPosSeed = nextPosSeed,
            gameMode = mode,
            timeLeftMs = (mode as? GameMode.Blitz)?.let { it.durationMinutes * 60 * 1000L },
            movesCount = 0,
            elapsedTimeMs = 0,
            highestTile = 2,
            hasWon = false,
            movesToWin = null,
            timeToWin = null
        )
    }

    private fun generateNextSeeds(mode: GameMode, count: Int): Pair<Float, Float> {
        val random = if (mode is GameMode.Daily) Random(mode.dateSeed + count) else Random
        return random.nextFloat() to random.nextFloat()
    }

    fun undo() {
        previousStateForUndo?.let { previous ->
            _state.value = previous.copy(canUndo = false)
            previousStateForUndo = null
            viewModelScope.launch {
                prefs.saveGameState(_state.value)
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            prefs.setTheme(theme)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setVibrationEnabled(enabled)
        }
    }

    fun setControlMode(mode: ControlMode) {
        viewModelScope.launch {
            prefs.setControlMode(mode)
        }
    }

    fun setFullScreenGestures(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setFullScreenGestures(enabled)
        }
    }

    fun setShowUndo(show: Boolean) {
        viewModelScope.launch {
            prefs.setShowUndo(show)
        }
    }

    fun setShowStopwatch(show: Boolean) {
        viewModelScope.launch {
            prefs.setShowStopwatch(show)
        }
    }

    fun setAnimationSpeed(speed: AnimationSpeed) {
        viewModelScope.launch {
            prefs.setAnimationSpeed(speed)
        }
    }

    private val intStatsFlows = mutableMapOf<String, StateFlow<Int>>()
    private val longStatsFlows = mutableMapOf<String, StateFlow<Long>>()

    private fun <T> getStatFlow(
        key: String,
        cache: MutableMap<String, StateFlow<T>>,
        initialValue: T,
        flowProducer: () -> Flow<T>
    ): StateFlow<T> {
        return cache.getOrPut(key) {
            flowProducer()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)
        }
    }

    fun getBestScore(mode: GameMode) = getStatFlow("best_${mode.statsId}", intStatsFlows, 0) { prefs.getBestScore(mode.statsId) }
    fun getHighestTile(mode: GameMode) = getStatFlow("highest_${mode.statsId}", intStatsFlows, 0) { prefs.getIntStat(PreferenceRepository.getHighestTileKey(mode.statsId)) }
    fun getFewestMoves(mode: GameMode) = getStatFlow("moves_${mode.statsId}", intStatsFlows, 0) { prefs.getIntStat(PreferenceRepository.getFewestMovesKey(mode.statsId)) }
    fun getFastestTime(mode: GameMode) = getStatFlow("time_${mode.statsId}", longStatsFlows, 0L) { prefs.getLongStat(PreferenceRepository.getFastestTimeKey(mode.statsId)) }
    fun getWinCount(modeId: String) = getStatFlow("wins_$modeId", intStatsFlows, 0) { prefs.getIntStat(PreferenceRepository.getWinCountKey(modeId)) }
    fun getGamesPlayed(modeId: String) = getStatFlow("games_$modeId", intStatsFlows, 0) { prefs.getIntStat(PreferenceRepository.getGamesPlayedKey(modeId)) }
    fun getTotalTime(modeId: String) = getStatFlow("total_$modeId", longStatsFlows, 0L) { prefs.getLongStat(PreferenceRepository.getTotalTimeKey(modeId)) }

    fun move(direction: Direction) {
        if (state.value.isGameOver) return

        val result = GameEngine.move(
            board = state.value.board,
            direction = direction,
            valueSeed = state.value.nextValueSeed,
            posSeed = state.value.nextPosSeed,
            nextId = state.value.nextId
        )

        if (result.hasChanged) {
            previousStateForUndo = state.value
            val newScore = state.value.score + result.scoreGained
            val (nextValueSeed, nextPosSeed) = generateNextSeeds(state.value.gameMode, state.value.movesCount + 1)
            
            val maxTile = result.board.asSequence().flatten().filterNotNull().maxOfOrNull { it.value } ?: 0
            val reachedWinCondition = maxTile >= state.value.gameMode.winCondition
            
            val newlyWon = reachedWinCondition && !state.value.hasWon
            
            val newState = state.value.copy(
                board = result.board,
                score = newScore,
                nextId = result.nextId,
                nextValueSeed = nextValueSeed,
                nextPosSeed = nextPosSeed,
                movesCount = state.value.movesCount + 1,
                highestTile = maxOf(state.value.highestTile, maxTile),
                isGameOver = GameEngine.isGameOver(result.board) || (state.value.timeLeftMs == 0L),
                canUndo = true,
                hasWon = state.value.hasWon || reachedWinCondition,
                movesToWin = if (newlyWon) state.value.movesCount + 1 else state.value.movesToWin,
                timeToWin = if (newlyWon) state.value.elapsedTimeMs else state.value.timeToWin,
            )

            _state.value = newState
            
            if (userPreferences.value.vibrationEnabled) {
                vibrationManager.vibrateForScore(result.scoreGained)
            }

            viewModelScope.launch {
                prefs.saveGameState(newState)
                prefs.updateBestScore(state.value.gameMode.statsId, newScore)
                prefs.updateHighestTile(state.value.gameMode.statsId, newState.highestTile)
                if (newlyWon) {
                    prefs.incrementWinCount(state.value.gameMode.groupStatsId)
                    prefs.updateFewestMoves(state.value.gameMode.statsId, newState.movesToWin!!)
                    prefs.updateFastestTime(state.value.gameMode.statsId, newState.timeToWin!!)
                }
            }
            
            if (newState.isGameOver) {
                stopTimer()
                viewModelScope.launch {
                    prefs.addToTotalTime(state.value.gameMode.groupStatsId, newState.elapsedTimeMs)
                }
            }
        }
    }
}
