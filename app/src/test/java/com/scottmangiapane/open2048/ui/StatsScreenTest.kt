package com.scottmangiapane.open2048.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.scottmangiapane.open2048.model.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StatsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun mockViewModel(): GameViewModel {
        val vm = mockk<GameViewModel>(relaxed = true)
        
        every { vm.userPreferences } returns MutableStateFlow(UserPreferences())
        every { vm.state } returns MutableStateFlow(GameState())
        
        // Stats
        every { vm.getBestScore(any()) } returns MutableStateFlow(0)
        every { vm.getHighestTile(any()) } returns MutableStateFlow(0)
        every { vm.getFewestMoves(any()) } returns MutableStateFlow(0)
        every { vm.getFastestTime(any()) } returns MutableStateFlow(0L)
        every { vm.getWinCount(any()) } returns MutableStateFlow(0)
        every { vm.getGamesPlayed(any()) } returns MutableStateFlow(0)
        every { vm.getTotalTime(any()) } returns MutableStateFlow(0L)
        
        return vm
    }

    @Test
    fun testStatsScreenBasicUi() {
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            StatsScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }
        composeTestRule.onNodeWithText("Statistics").assertExists()
        composeTestRule.onNodeWithText("DAILY CHALLENGE").assertExists()
        composeTestRule.onNodeWithText("CLASSIC MODES").assertExists()
        composeTestRule.onNodeWithText("BLITZ MODES").assertExists()
    }

    @Test
    fun testStatsScreenNavigation() {
        var backCalled = false
        val viewModel = mockViewModel()
        composeTestRule.setContent {
            StatsScreen(
                viewModel = viewModel,
                onBack = { backCalled = true }
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backCalled)
    }

    @Test
    fun testStatsScreenWithData() {
        val viewModel = mockViewModel()
        every { viewModel.getBestScore(any()) } returns MutableStateFlow(2048)
        every { viewModel.getFewestMoves(any()) } returns MutableStateFlow(500)
        every { viewModel.getFastestTime(any()) } returns MutableStateFlow(120000L) // 2 mins

        composeTestRule.setContent {
            StatsScreen(viewModel = viewModel, onBack = {})
        }
        
        composeTestRule.onAllNodesWithText("2048").onFirst().assertExists()
        composeTestRule.onAllNodesWithText("500").onFirst().assertExists()
        composeTestRule.onAllNodesWithText("2m 0s").onFirst().assertExists()
    }

    @Test
    fun testStatsScreenEmptyData() {
        val viewModel = mockViewModel()
        every { viewModel.getFewestMoves(any()) } returns MutableStateFlow(Int.MAX_VALUE)
        every { viewModel.getFastestTime(any()) } returns MutableStateFlow(Long.MAX_VALUE)

        composeTestRule.setContent {
            StatsScreen(viewModel = viewModel, onBack = {})
        }
        
        val nodes = composeTestRule.onAllNodesWithText("-").fetchSemanticsNodes()
        assertTrue(nodes.size >= 2)
    }

    @Test
    fun testStatsScreenZeroData() {
        val viewModel = mockViewModel()
        every { viewModel.getFewestMoves(any()) } returns MutableStateFlow(0)
        every { viewModel.getFastestTime(any()) } returns MutableStateFlow(0L)

        composeTestRule.setContent {
            StatsScreen(viewModel = viewModel, onBack = {})
        }
        
        val nodes = composeTestRule.onAllNodesWithText("-").fetchSemanticsNodes()
        assertTrue(nodes.size >= 2)
    }
}
