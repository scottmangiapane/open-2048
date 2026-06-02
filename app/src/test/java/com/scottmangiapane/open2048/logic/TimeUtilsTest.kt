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
}
