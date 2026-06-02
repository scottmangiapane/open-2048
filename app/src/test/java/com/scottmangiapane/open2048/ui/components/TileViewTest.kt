package com.scottmangiapane.open2048.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.scottmangiapane.open2048.model.AnimationSpeed
import com.scottmangiapane.open2048.model.AppTheme
import com.scottmangiapane.open2048.model.Tile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TileViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testTileViewDisplaysValue() {
        val tile = Tile(id = 1, value = 2048, isNew = false)
        composeTestRule.setContent {
            TileView(
                tile = tile,
                tileSize = 100.dp,
                currentTheme = AppTheme.LIGHT,
                animationSpeed = AnimationSpeed.NONE
            )
        }

        composeTestRule.onNodeWithText("2048").assertExists()
    }

    @Test
    fun testTileViewNewTile() {
        val tile = Tile(id = 1, value = 2, isNew = true)
        composeTestRule.setContent {
            TileView(
                tile = tile,
                tileSize = 100.dp,
                currentTheme = AppTheme.DARK,
                animationSpeed = AnimationSpeed.FAST
            )
        }
        
        // Wait for animation delay if needed
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.onNodeWithText("2").assertExists()
    }

    @Test
    fun testTileViewValueFactors() {
        // Test different value thresholds for font sizing factor
        composeTestRule.setContent {
            TileView(Tile(1, 2), 100.dp, AppTheme.CLASSIC, AnimationSpeed.NORMAL)
            TileView(Tile(2, 128), 100.dp, AppTheme.CLASSIC, AnimationSpeed.NORMAL)
            TileView(Tile(3, 2048), 100.dp, AppTheme.CLASSIC, AnimationSpeed.NORMAL)
        }
        composeTestRule.onNodeWithText("2").assertExists()
        composeTestRule.onNodeWithText("128").assertExists()
        composeTestRule.onNodeWithText("2048").assertExists()
    }

    @Test
    fun testTileViewAnimationSpeeds() {
        composeTestRule.setContent {
            TileView(Tile(1, 2), 100.dp, AppTheme.LIGHT, AnimationSpeed.SLOW)
            TileView(Tile(2, 4), 100.dp, AppTheme.LIGHT, AnimationSpeed.NONE)
        }
        composeTestRule.onNodeWithText("2").assertExists()
        composeTestRule.onNodeWithText("4").assertExists()
    }

    @Test
    fun testTileViewMerge() {
        var tileState by mutableStateOf(Tile(id = 1, value = 2, isNew = false))
        composeTestRule.setContent {
            TileView(tileState, 100.dp, AppTheme.LIGHT, AnimationSpeed.NONE)
        }
        
        composeTestRule.onNodeWithText("2").assertExists()
        
        // Update tile value (merge)
        tileState = Tile(id = 1, value = 4, isNew = false)
        
        composeTestRule.onNodeWithText("4").assertExists()
    }
}
