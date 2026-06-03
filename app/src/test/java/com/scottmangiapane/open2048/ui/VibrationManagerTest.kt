package com.scottmangiapane.open2048.ui

import android.content.Context
import android.os.Build
import android.os.Vibrator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class VibrationManagerTest {

    private lateinit var vibrator: Vibrator
    private lateinit var context: Context
    private lateinit var vibrationManager: VibrationManager

    @Before
    fun setup() {
        vibrator = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { context.getSystemService(Context.VIBRATOR_SERVICE) } returns vibrator
        vibrationManager = VibrationManager(context, vibrator)
    }

    @Test
    fun testHasVibrator() {
        every { vibrator.hasVibrator() } returns true
        val manager1 = VibrationManager(context, vibrator)
        assertEquals(true, manager1.hasVibrator)
        
        every { vibrator.hasVibrator() } returns false
        val manager2 = VibrationManager(context, vibrator)
        assertEquals(false, manager2.hasVibrator)
    }

    @Test
    @Config(sdk = [26]) // O and above
    fun testVibrateOneShot() {
        vibrationManager.vibrate(100, 128)
        verify { vibrator.vibrate(any<android.os.VibrationEffect>()) }
        
        // Test custom amplitude out of range (coerced)
        vibrationManager.vibrate(100, 300)
        verify { vibrator.vibrate(any<android.os.VibrationEffect>()) }
    }

    @Test
    @Config(sdk = [26])
    fun testVibrateDefaultAmplitude() {
        vibrationManager.vibrate(100, -1)
        verify { vibrator.vibrate(any<android.os.VibrationEffect>()) }
    }

    @Test
    @Config(sdk = [25]) // Below O
    fun testVibrateLegacy() {
        vibrationManager.vibrate(100)
        @Suppress("DEPRECATION")
        verify { vibrator.vibrate(100L) }
    }

    @Test
    fun testVibrateForScoreBranches() {
        // 0 -> vibrate(10, 20)
        vibrationManager.vibrateForScore(0)
        
        // <= 32 -> vibrate(20, 45)
        vibrationManager.vibrateForScore(32)
        vibrationManager.vibrateForScore(2)
        
        // <= 128 -> vibrate(40, 90)
        vibrationManager.vibrateForScore(128)
        vibrationManager.vibrateForScore(64)
        
        // <= 512 -> vibrate(60, 160)
        vibrationManager.vibrateForScore(512)
        vibrationManager.vibrateForScore(256)
        
        // else -> vibrate(100, 220)
        vibrationManager.vibrateForScore(1024)
        vibrationManager.vibrateForScore(2048)
        
        verify(atLeast = 9) { vibrator.vibrate(any<android.os.VibrationEffect>()) }
    }
}
