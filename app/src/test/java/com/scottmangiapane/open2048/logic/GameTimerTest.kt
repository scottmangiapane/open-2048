package com.scottmangiapane.open2048.logic

import kotlinx.coroutines.*
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameTimerTest {

    @Test
    fun testTimerTicks() = runTest {
        var tickCount = 0
        val timer = GameTimer(this)
        
        timer.start {
            tickCount++
        }
        
        advanceTimeBy(1100)
        assertEquals(1, tickCount)
        
        advanceTimeBy(1000)
        assertEquals(2, tickCount)
        
        timer.stop()
        advanceTimeBy(1000)
        assertEquals(2, tickCount)
    }

    @Test
    fun testTimerRestarts() = runTest {
        var tickCount = 0
        val timer = GameTimer(this)
        
        timer.start { tickCount++ }
        advanceTimeBy(1100)
        assertEquals(1, tickCount)
        
        timer.start { tickCount++ }
        advanceTimeBy(1100)
        assertEquals(2, tickCount)
        
        timer.stop()
    }

    @Test
    fun testTimerStopWithNoJob() {
        val timer = GameTimer(MainScope())
        timer.stop() // Should not crash
    }
}
