package com.scottmangiapane.open2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scottmangiapane.open2048.ui.GameScreen
import com.scottmangiapane.open2048.ui.GameViewModel
import com.scottmangiapane.open2048.ui.MenuScreen
import com.scottmangiapane.open2048.ui.theme.Open2048Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val state by viewModel.state.collectAsState()
            val currentTheme = state.theme

            var currentScreen by rememberSaveable { mutableStateOf("menu") }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> viewModel.stopTimer()
                        Lifecycle.Event.ON_RESUME -> if (currentScreen == "game") viewModel.resumeGame()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            Open2048Theme(theme = currentTheme) {
                if (currentScreen == "menu") {
                    MenuScreen(
                        currentTheme = currentTheme,
                        onStartGame = { mode ->
                            viewModel.restartGame(mode)
                            currentScreen = "game"
                        },
                        onResumeGame = {
                            viewModel.resumeGame()
                            currentScreen = "game"
                        },
                        onToggleTheme = { viewModel.cycleTheme() },
                        canResume = state.board.isNotEmpty() && !state.isGameOver
                    )
                } else {
                    GameScreen(
                        viewModel = viewModel,
                        onBackToMenu = { 
                            viewModel.stopTimer()
                            currentScreen = "menu" 
                        }
                    )
                }
            }
        }
    }
}
