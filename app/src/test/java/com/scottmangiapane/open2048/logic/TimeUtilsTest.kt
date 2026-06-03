package com.scottmangiapane.open2048.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun testFormatDurationSeconds() {
        assertEquals("00:05", TimeUtils.formatDuration(5000, false))
        assertEquals("00:59", TimeUtils.formatDuration(59000, false))
    }

    @Test
    fun testFormatDurationMinutes() {
        assertEquals("01:00", TimeUtils.formatDuration(60000, false))
        assertEquals("10:05", TimeUtils.formatDuration(605000, false))
    }

    @Test
    fun testFormatDurationHours() {
        assertEquals("1:00:00", TimeUtils.formatDuration(3600000, true))
        assertEquals("2:30:45", TimeUtils.formatDuration(9045000, true))
        // showHours = true but hours = 0
        assertEquals("01:00", TimeUtils.formatDuration(60000, true))
    }

    @Test
    fun testFormatDurationAbbreviated() {
        assertEquals("5s", TimeUtils.formatDurationAbbreviated(5000))
        assertEquals("1m 5s", TimeUtils.formatDurationAbbreviated(65000))
        assertEquals("1h 10m", TimeUtils.formatDurationAbbreviated(4200000))
    }

    @Test
    fun testFormatDurationShowZero() {
        assertEquals("00:00", TimeUtils.formatDuration(0, true))
    }

    @Test
    fun testFormatDurationEdgeCases() {
        // Exactly 1 hour
        assertEquals("1:00:00", TimeUtils.formatDuration(3600000, true))
        // 59m 59s
        assertEquals("59:59", TimeUtils.formatDuration(3599000, false))
        // Negative (should ideally not happen, but check behavior)
        // ms / 1000 will be 0 or negative
    }

    @Test
    fun testFormatDurationAbbreviatedEdgeCases() {
        // 0s
        assertEquals("0s", TimeUtils.formatDurationAbbreviated(0))
        // 1h exactly
        assertEquals("1h 0m", TimeUtils.formatDurationAbbreviated(3600000))
        // 1m exactly
        assertEquals("1m 0s", TimeUtils.formatDurationAbbreviated(60000))
    }
}
