package com.scottmangiapane.open2048.model

data class Tile(
    val id: Int,
    val value: Int,
    val isNew: Boolean = false
)
