package com.scottmangiapane.open2048.ui.components

import android.view.KeyCharacterMap
import android.view.KeyEvent

object DeviceUtils {
    val hasDpad: Boolean by lazy {
        KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_CENTER) ||
                KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_UP) ||
                KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_DOWN) ||
                KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_LEFT) ||
                KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_RIGHT)
    }
}
