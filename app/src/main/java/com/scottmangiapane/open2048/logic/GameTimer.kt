package com.scottmangiapane.open2048.logic

import kotlinx.coroutines.*

class GameTimer(private val scope: CoroutineScope) {
    private var timerJob: Job? = null
    
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
