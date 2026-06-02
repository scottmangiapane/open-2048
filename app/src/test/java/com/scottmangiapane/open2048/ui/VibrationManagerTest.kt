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
    fun testVibrateForScore() {
        vibrationManager.vibrateForScore(0)
        vibrationManager.vibrateForScore(32)
        vibrationManager.vibrateForScore(128)
        vibrationManager.vibrateForScore(512)
        vibrationManager.vibrateForScore(1024)
        
        // This verify might need to handle both overloads or be specific
        // Actually, verify it was called at all
        verify(atLeast = 1) { vibrator.vibrate(any<android.os.VibrationEffect>()) }
    }
}
