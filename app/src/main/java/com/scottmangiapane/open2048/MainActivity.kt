package com.scottmangiapane.open2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.canResume
import com.scottmangiapane.open2048.ui.GameScreen
import com.scottmangiapane.open2048.ui.GameViewModel
import com.scottmangiapane.open2048.ui.MenuScreen
import com.scottmangiapane.open2048.ui.StatsScreen
import com.scottmangiapane.open2048.ui.SettingsScreen
import com.scottmangiapane.open2048.ui.Screen
import com.scottmangiapane.open2048.ui.theme.Open2048Theme

open class MainActivity : ComponentActivity() {
    open val activityTheme: AppTheme = AppTheme.LIGHT
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            
            val canResume by remember { derivedStateOf { state.canResume } }
            val hasProgress by remember { derivedStateOf { state.movesCount > 0 && !state.isGameOver } }

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

            Open2048Theme(theme = state.theme ?: activityTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        is Screen.Menu -> {
                            MenuScreen(
                                onStartGame = { mode -> viewModel.restartGame(mode) },
                                onResumeGame = { viewModel.resumeGame() },
                                onNavigateToStats = { viewModel.navigateToStats() },
                                onNavigateToSettings = { viewModel.navigateToSettings() },
                                canResume = canResume,
                                hasProgress = hasProgress
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
                        is Screen.Stats -> {
                            BackHandler {
                                viewModel.navigateToMenu()
                            }
                            StatsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateToMenu() }
                            )
                        }
                        is Screen.Settings -> {
                            BackHandler {
                                viewModel.navigateToMenu()
                            }
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateToMenu() }
                            )
                        }
                    }
                }
            }
        }
    }
}
