package com.scottmangiapane.open2048.logic

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameTimer(private val scope: CoroutineScope) {
    private var timerJob: Job? = null
    
    private val _tickEvent = MutableStateFlow(0L)
    val tickEvent = _tickEvent.asStateFlow()

    fun start(onTick: (Long) -> Unit) {
        timerJob?.cancel()
        timerJob = scope.launch {
            var lastTick = System.currentTimeMillis()
            while (isActive) {
                delay(1000)
                val currentTick = System.currentTimeMillis()
                val delta = currentTick - lastTick
                lastTick = currentTick
                onTick(delta)
            }
        }
    }

    fun stop() {
        timerJob?.cancel()
    }
}
