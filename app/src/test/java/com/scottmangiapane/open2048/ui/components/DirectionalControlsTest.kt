package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.logic.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.assertEquals
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DirectionalControlsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDirectionalControlsPortrait() {
        var lastDirection: Direction? = null
        composeTestRule.setContent {
            DirectionalControls(
                isLandscape = false,
                onMove = { lastDirection = it }
            )
        }

        // Current implementation uses ControlButton which uses FilledIconButton.
        // We can find them by their order or add tags.
        // Let's assume order for now or just click all of them.
        val buttons = composeTestRule.onAllNodes(hasClickAction())
        buttons.assertCountEquals(4)
        
        // Left, Up, Down, Right (based on row [Left, col[Up, Down], Right])
        buttons[0].performClick()
        assertEquals(Direction.LEFT, lastDirection)
        
        buttons[1].performClick()
        assertEquals(Direction.UP, lastDirection)
        
        buttons[2].performClick()
        assertEquals(Direction.DOWN, lastDirection)
        
        buttons[3].performClick()
        assertEquals(Direction.RIGHT, lastDirection)
    }

    @Test
    fun testDirectionalControlsLandscape() {
        var lastDirection: Direction? = null
        composeTestRule.setContent {
            DirectionalControls(
                isLandscape = true,
                onMove = { lastDirection = it }
            )
        }
        
        val buttons = composeTestRule.onAllNodes(hasClickAction())
        buttons.assertCountEquals(4)
        
        // Up, Row[Left, Right], Down
        buttons[0].performClick()
        assertEquals(Direction.UP, lastDirection)
        
        buttons[1].performClick()
        assertEquals(Direction.LEFT, lastDirection)
        
        buttons[2].performClick()
        assertEquals(Direction.RIGHT, lastDirection)
        
        buttons[3].performClick()
        assertEquals(Direction.DOWN, lastDirection)
    }
}
