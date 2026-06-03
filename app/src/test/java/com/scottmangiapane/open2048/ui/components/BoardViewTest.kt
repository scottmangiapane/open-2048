package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.AnimationSpeed
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.GameMode
import com.scottmangiapane.open2048.model.GameState
import com.scottmangiapane.open2048.model.Tile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import com.scottmangiapane.open2048.logic.Direction

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BoardViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testBoardContainerDisplaysTiles() {
        val board = listOf(
            listOf(Tile(1, 2), null),
            listOf(null, Tile(2, 4))
        )
        val state = GameState(board = board, gameMode = GameMode.Classic(2))

        composeTestRule.setContent {
            BoardContainer(
                state = state,
                currentTheme = AppTheme.LIGHT,
                animationSpeed = AnimationSpeed.NONE
            )
        }

        composeTestRule.onNodeWithText("2").assertExists()
        composeTestRule.onNodeWithText("4").assertExists()
    }

    @Test
    fun testGameOverOverlay() {
        val board = listOf(
            listOf(Tile(1, 2), Tile(2, 4)),
            listOf(Tile(3, 4), Tile(4, 2))
        )
        val gameOverState = GameState(board = board, gameMode = GameMode.Classic(2), isGameOver = true)
        
        composeTestRule.setContent {
            BoardContainer(
                state = gameOverState,
                currentTheme = AppTheme.LIGHT,
                animationSpeed = AnimationSpeed.NONE
            )
        }

        composeTestRule.onNodeWithText("Game Over!").assertExists()
    }

    @Test
    fun testTimeUpOverlay() {
        val board = listOf(listOf(Tile(1, 2)))
        val state = GameState(board = board, gameMode = GameMode.Blitz(2), timeLeftMs = 0L, isGameOver = true)

        composeTestRule.setContent {
            BoardContainer(
                state = state,
                currentTheme = AppTheme.LIGHT,
                animationSpeed = AnimationSpeed.NONE
            )
        }

        composeTestRule.onNodeWithText("Time's Up!").assertExists()
    }

    @Test
    fun testBoardInteractionSwipeAllDirections() {
        var capturedDirection: Direction? = null
        val board = listOf(listOf(Tile(1, 2), null), listOf(null, null))
        val state = GameState(board = board, gameMode = GameMode.Classic(2))
        
        composeTestRule.setContent {
            BoardContainer(
                state = state,
                currentTheme = AppTheme.LIGHT,
                animationSpeed = AnimationSpeed.NONE,
                onMove = { capturedDirection = it },
                autoPlay = true,
                fullScreenGestures = false
            )
        }

        composeTestRule.onRoot().performTouchInput { swipeRight() }
        assertEquals(Direction.RIGHT, capturedDirection)

        capturedDirection = null
        composeTestRule.onRoot().performTouchInput { swipeLeft() }
        assertEquals(Direction.LEFT, capturedDirection)

        capturedDirection = null
        composeTestRule.onRoot().performTouchInput { swipeUp() }
        assertEquals(Direction.UP, capturedDirection)

        capturedDirection = null
        composeTestRule.onRoot().performTouchInput { swipeDown() }
        assertEquals(Direction.DOWN, capturedDirection)
        
        // Small swipe
        capturedDirection = null
        composeTestRule.onRoot().performTouchInput {
            swipe(start = center, end = center + Offset(10f, 0f), durationMillis = 100)
        }
        assertNull(capturedDirection)
    }

    @Test
    fun testBoardKeyEvents() {
        var capturedDirection: Direction? = null
        val board = listOf(listOf(Tile(1, 2), null), listOf(null, null))
        val state = GameState(board = board, gameMode = GameMode.Classic(2))
        val focusRequester = FocusRequester()
        
        composeTestRule.setContent {
            BoardContainer(
                state = state,
                currentTheme = AppTheme.LIGHT,
                animationSpeed = AnimationSpeed.NONE,
                onMove = { capturedDirection = it },
                focusRequester = focusRequester,
                autoPlay = true
            )
        }

        composeTestRule.onRoot().performClick()
        composeTestRule.runOnIdle {
            focusRequester.requestFocus()
        }

        // Test Arrow Keys (Right, Left, Up, Down)
        val keys = listOf(
            android.view.KeyEvent.KEYCODE_DPAD_RIGHT to Direction.RIGHT,
            android.view.KeyEvent.KEYCODE_DPAD_LEFT to Direction.LEFT,
            android.view.KeyEvent.KEYCODE_DPAD_UP to Direction.UP,
            android.view.KeyEvent.KEYCODE_DPAD_DOWN to Direction.DOWN
        )

        keys.forEach { (keyCode, expected) ->
            capturedDirection = null
            composeTestRule.onRoot().performKeyPress(androidx.compose.ui.input.key.KeyEvent(
                android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode)
            ))
            assertEquals(expected, capturedDirection)
        }
    }

    @Test
    fun testBoardInteractionSwipeDiagonal() {
        var capturedDirection: Direction? = null
        composeTestRule.setContent {
            BoardContainer(
                state = GameState(board = listOf(listOf(Tile(1, 2), null), listOf(null, null))),
                currentTheme = AppTheme.LIGHT,
                animationSpeed = AnimationSpeed.NONE,
                onMove = { capturedDirection = it },
                autoPlay = true,
                fullScreenGestures = false
            )
        }

        // Horizontal dominant, but below threshold
        capturedDirection = null
        composeTestRule.onRoot().performTouchInput {
            swipe(start = center, end = center + Offset(10f, 5f), durationMillis = 100)
        }
        assertNull(capturedDirection)

        // Vertical dominant, above threshold
        capturedDirection = null
        composeTestRule.onRoot().performTouchInput {
            swipe(start = center, end = center + Offset(50f, 200f), durationMillis = 100)
        }
        assertEquals(Direction.DOWN, capturedDirection)
    }
}
