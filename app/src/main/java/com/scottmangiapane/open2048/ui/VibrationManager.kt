package com.scottmangiapane.open2048.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class VibrationManager(context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibrate(durationMs: Long = 50, amplitude: Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val finalAmplitude = if (amplitude == -1) VibrationEffect.DEFAULT_AMPLITUDE else amplitude.coerceIn(1, 255)
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, finalAmplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    /**
     * Vibrate based on a score or tile value.
     */
    fun vibrateForScore(score: Int) {
        when {
            score == 0 -> vibrate(10, 50) // Very light tap for a move with no merge
            score <= 8 -> vibrate(20, 100) // Light merge (2+2 or 4+4)
            score <= 64 -> vibrate(40, 150) // Medium merge
            score <= 512 -> vibrate(60, 200) // Strong merge
            else -> vibrate(100, 255) // Heavy merge (1024+)
        }
    }
}
