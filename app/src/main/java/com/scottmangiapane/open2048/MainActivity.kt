package com.scottmangiapane.open2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.ui.GameScreen
import com.scottmangiapane.open2048.ui.GameViewModel
import com.scottmangiapane.open2048.ui.MenuScreen
import com.scottmangiapane.open2048.ui.Screen
import com.scottmangiapane.open2048.ui.theme.Open2048Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val canResume by viewModel.canResume.collectAsStateWithLifecycle()
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()

            LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
                viewModel.stopTimer()
            }

            LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                if (!isChangingConfigurations) {
                    viewModel.applyPendingIconChange()
                }
            }

            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                if (currentScreen == Screen.Game) {
                    viewModel.resumeGame()
                }
            }

            Open2048Theme(theme = state.theme) {
                when (currentScreen) {
                    is Screen.Menu -> {
                        MenuScreen(
                            userPreferences = userPreferences,
                            onStartGame = { mode -> viewModel.restartGame(mode) },
                            onResumeGame = { viewModel.resumeGame() },
                            onSetTheme = { viewModel.setTheme(it) },
                            onSetVibrationEnabled = { viewModel.setVibrationEnabled(it) },
                            onSetControlMode = { viewModel.setControlMode(it) },
                            onSetShowUndo = { viewModel.setShowUndo(it) },
                            onSetShowStopwatch = { viewModel.setShowStopwatch(it) },
                            canResume = canResume
                        )
                    }
                    is Screen.Game -> {
                        BackHandler {
                            viewModel.navigateToMenu()
                        }
                        GameScreen(
                            viewModel = viewModel,
                            onBackToMenu = { viewModel.navigateToMenu() }
                        )
                    }
                }
            }
        }
    }
}
