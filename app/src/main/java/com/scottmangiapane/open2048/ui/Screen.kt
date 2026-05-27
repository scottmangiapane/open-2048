package com.scottmangiapane.open2048.ui

sealed class Screen {
    data object Menu : Screen()
    data object Game : Screen()
    data object Stats : Screen()
}
