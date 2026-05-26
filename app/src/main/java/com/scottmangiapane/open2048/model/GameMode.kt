package com.scottmangiapane.open2048.model

sealed class GameMode {
    abstract val size: Int
    abstract val id: String

    data class Classic(override val size: Int) : GameMode() {
        override val id: String = "classic_$size"
    }

    data class Blitz(val durationMinutes: Int) : GameMode() {
        override val size: Int = 4
        override val id: String = "blitz_$durationMinutes"
    }
}
