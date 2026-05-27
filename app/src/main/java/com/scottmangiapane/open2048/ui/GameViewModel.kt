package com.scottmangiapane.open2048.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scottmangiapane.open2048.data.PreferenceRepository
import com.scottmangiapane.open2048.logic.Direction
import com.scottmangiapane.open2048.logic.GameEngine
import com.scottmangiapane.open2048.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceRepository(application)
    private val iconManager = IconManager(application)
    private val vibrationManager = VibrationManager(application)
    
    private var previousStateForUndo: GameState? = null
    private var bestScoreJob: Job? = null
    private var timerJob: Job? = null
    
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Menu)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = prefs.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val canResume: StateFlow<Boolean> = _state
        .map { state -> state.canResume }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false)

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
            val saved = prefs.savedGameState.firstOrNull()
            if (saved != null) {
                _state.update {
                    saved.copy(
                        theme = it.theme,
                        isGameOver = saved.isGameOver || GameEngine.isGameOver(saved.board) || (saved.timeLeftMs == 0L),
                    )
                }
                observeBestScore(saved.gameMode)
                if (!_state.value.isGameOver && saved.board.isNotEmpty()) {
                    startTimer()
                }
            } else {
                val defaultMode = GameMode.Classic(4)
                _state.update { it.copy(gameMode = defaultMode) }
                observeBestScore(defaultMode)
                _currentScreen.value = Screen.Menu
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
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (isActive) {
                delay(1000)
                val currentTick = System.currentTimeMillis()
                val delta = currentTick - lastTick
                lastTick = currentTick
                
                var shouldStop = false
                _state.update { state ->
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
                    saveGame(_state.value)
                    break
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
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

    fun applyPendingIconChange() {
        iconManager.applyPendingIconChange()
    }

    fun restartGame(mode: GameMode? = null) {
        val currentMode = mode ?: _state.value.gameMode
        previousStateForUndo = null
        timerJob?.cancel()
        
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

        previousStateForUndo = currentState
        val newScore = currentState.score + result.scoreGained
        val bestScore = maxOf(currentState.bestScore, newScore)

        if (newScore > currentState.bestScore) {
            viewModelScope.launch { prefs.updateBestScore(currentState.gameMode.id, newScore) }
        }

        val isGameOver = GameEngine.isGameOver(result.board) || (currentState.timeLeftMs == 0L)
        if (isGameOver) stopTimer()

        val prefs = userPreferences.value
        if (prefs.vibrationEnabled) {
            vibrationManager.vibrateForScore(result.scoreGained)
        }

        val (nextV, nextP) = generateNextSeeds(currentState.gameMode, result.nextId)

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
            )
        }
        saveGame(_state.value)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
