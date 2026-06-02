package com.scottmangiapane.open2048.model

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class GameModeTest {

    @Test
    fun testClassicMode() {
        val mode = GameMode.Classic(4)
        assertEquals("classic_4", mode.id)
        assertEquals(4, mode.size)
        assertEquals(2048, mode.winCondition)
        assertEquals("classic_4", mode.statsId)
        
        assertEquals(256, GameMode.Classic(3).winCondition)
        assertEquals(16384, GameMode.Classic(5).winCondition)
        assertEquals(2048, GameMode.Classic(6).winCondition)
    }

    @Test
    fun testBlitzMode() {
        val mode = GameMode.Blitz(5)
        assertEquals("blitz_5", mode.id)
        assertEquals(4, mode.size)
        assertEquals(1024, mode.winCondition)
        assertEquals("blitz_5", mode.statsId)
        
        assertEquals(512, GameMode.Blitz(2).winCondition)
        assertEquals(1024, GameMode.Blitz(3).winCondition)
    }

    @Test
    fun testDailyMode() {
        val mode = GameMode.Daily(2024, 5, 20)
        assertEquals("daily_2024_5_20", mode.id)
        assertEquals(4, mode.size)
        assertEquals(2048, mode.winCondition)
        assertEquals(20240520L, mode.dateSeed)
        assertEquals("daily", mode.statsId)
    }

    @Test
    fun testDailyToday() {
        val today = GameMode.Daily.today()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        assertEquals(cal.get(Calendar.YEAR), today.year)
        assertEquals(cal.get(Calendar.MONTH) + 1, today.month)
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), today.day)
    }

    @Test
    fun testFromId() {
        val classic = GameMode.fromId("classic_4")
        assertTrue(classic is GameMode.Classic)
        assertEquals(4, (classic as GameMode.Classic).size)

        val blitz = GameMode.fromId("blitz_5")
        assertTrue(blitz is GameMode.Blitz)
        assertEquals(5, (blitz as GameMode.Blitz).durationMinutes)

        val daily = GameMode.fromId("daily_2024_5_20")
        assertTrue(daily is GameMode.Daily)
        val dailyMode = daily as GameMode.Daily
        assertEquals(2024, dailyMode.year)
        assertEquals(5, dailyMode.month)
        assertEquals(20, dailyMode.day)
        
        assertNull(GameMode.fromId("unknown_1"))
        assertNull(GameMode.fromId("classic"))
        assertNull(GameMode.fromId("blitz_abc"))
        assertNull(GameMode.fromId("daily_2023_10"))
        assertNull(GameMode.fromId("daily_2023_10_invalid"))
        assertNull(GameMode.fromId(""))
    }

    @Test
    fun testSaver() {
        val classic = GameMode.Classic(4)
        val scope = object : SaverScope {
            override fun canBeSaved(value: Any): Boolean = true
        }
        val saved = with(GameMode.Saver) { scope.save(classic) }
        assertEquals("classic_4", saved)
        
        val restored = GameMode.Saver.restore(saved!!)
        assertEquals(classic, restored)
        
        val savedNull = with(GameMode.Saver) { scope.save(null) }
        assertEquals("", savedNull)
        assertNull(GameMode.Saver.restore(""))
    }
}
