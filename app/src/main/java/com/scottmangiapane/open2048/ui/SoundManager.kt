package com.scottmangiapane.open2048.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private var moveSoundId: Int = 0
    private var mergeSoundId: Int = 0
    private var gameOverSoundId: Int = 0

    init {
        loadSounds()
    }

    private fun loadSounds() {
        moveSoundId = loadSound("move")
        mergeSoundId = loadSound("merge")
        gameOverSoundId = loadSound("game_over")
    }

    private fun loadSound(name: String): Int {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (resId != 0) {
            soundPool.load(context, resId, 1)
        } else {
            Log.w("SoundManager", "Sound resource not found: $name. Please add $name.mp3 to res/raw/")
            0
        }
    }

    fun playMove() {
        if (moveSoundId != 0) soundPool.play(moveSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playMerge() {
        if (mergeSoundId != 0) soundPool.play(mergeSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playGameOver() {
        if (gameOverSoundId != 0) soundPool.play(gameOverSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    fun release() {
        soundPool.release()
    }
}
