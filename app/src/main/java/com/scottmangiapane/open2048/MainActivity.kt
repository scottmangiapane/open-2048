package com.scottmangiapane.open2048

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.Color as AndroidColor
import com.scottmangiapane.open2048.ui.GameScreen
import com.scottmangiapane.open2048.ui.GameViewModel
import com.scottmangiapane.open2048.ui.theme.Open2048Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT
            )
        )
        setContent {
            val viewModel: GameViewModel = viewModel()
            val state by viewModel.state.collectAsState()
            val darkTheme = state.isDarkMode ?: isSystemInDarkTheme()

            Open2048Theme(darkTheme = darkTheme) {
                GameScreen(viewModel = viewModel)
            }
        }
    }
}
