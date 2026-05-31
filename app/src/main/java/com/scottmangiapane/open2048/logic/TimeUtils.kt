package com.scottmangiapane.open2048.logic

import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun formatDuration(ms: Long, showHours: Boolean = false): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        val locale = Locale.getDefault()
        return when {
            showHours && hours > 0 -> String.format(locale, "%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format(locale, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDurationAbbreviated(ms: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        
        val locale = Locale.getDefault()
        return when {
            hours > 0 -> String.format(locale, "%dh %dm", hours, minutes)
            minutes > 0 -> String.format(locale, "%dm %ds", minutes, seconds)
            else -> String.format(locale, "%ds", seconds)
        }
    }
}
