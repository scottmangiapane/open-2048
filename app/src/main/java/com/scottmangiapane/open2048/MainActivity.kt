package com.scottmangiapane.open2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            val darkTheme = state.isDarkMode ?: isSystemInDarkTheme()

            var currentScreen by remember { mutableStateOf("menu") }

            Open2048Theme(darkTheme = darkTheme) {
                if (currentScreen == "menu") {
                    MenuScreen(
                        isDarkMode = darkTheme,
                        onStartGame = { size ->
                            viewModel.restartGame(size)
                            currentScreen = "game"
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode() }
                    )
                } else {
                    GameScreen(
                        viewModel = viewModel,
                        onBackToMenu = { currentScreen = "menu" }
                    )
                }
            }
        }
    }
}
