package com.ceylonapz.babytube.presentation.player

import com.ceylonapz.babytube.domain.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val videos = (1..3).map { Video("v$it", "Song $it", "Channel", "") }
    private val history = PlaybackHistory()

    private fun TestScope.viewModel(startIndex: Int = 0): PlayerViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return PlayerViewModel(videos, startIndex, source = "Lullabies", history = history)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts at the clicked video with its neighbours`() = runTest {
        val state = viewModel(startIndex = 1).uiState.value

        assertEquals("v2", state.video.id)
        assertEquals("v1", state.previousVideo?.id)
        assertEquals("v3", state.nextVideo?.id)
        assertEquals(LastPlayed("Lullabies", "v2"), history.lastPlayed.value)
    }

    @Test
    fun `next and previous move through the queue and stop at the ends`() = runTest {
        val viewModel = viewModel()

        viewModel.playPrevious()
        assertEquals("v1", viewModel.uiState.value.video.id)

        viewModel.playNext()
        viewModel.playNext()
        viewModel.playNext()
        assertEquals("v3", viewModel.uiState.value.video.id)
        assertNull(viewModel.uiState.value.nextVideo)
        assertEquals(LastPlayed("Lullabies", "v3"), history.lastPlayed.value)
    }

    @Test
    fun `each new video requests a fresh start`() = runTest {
        val viewModel = viewModel()
        val first = viewModel.uiState.value.playRequest

        viewModel.playNext()
        val second = viewModel.uiState.value.playRequest
        viewModel.replay()

        assertTrue(second > first)
        assertTrue(viewModel.uiState.value.playRequest > second)
        assertEquals("v2", viewModel.uiState.value.video.id)
    }

    @Test
    fun `ended video counts down then plays the next one`() = runTest {
        val viewModel = viewModel()

        viewModel.onVideoEnded()
        assertEquals(PlayerEnd.UpNext(videos[1], 5), viewModel.uiState.value.end)

        advanceTimeBy(2_000); runCurrent()
        assertEquals(PlayerEnd.UpNext(videos[1], 3), viewModel.uiState.value.end)

        advanceTimeBy(3_000); runCurrent()
        assertEquals("v2", viewModel.uiState.value.video.id)
        assertNull(viewModel.uiState.value.end)
    }

    @Test
    fun `play now skips the countdown`() = runTest {
        val viewModel = viewModel()

        viewModel.onVideoEnded()
        viewModel.playNext()
        advanceTimeBy(10_000); runCurrent()

        // Only one step forward: the cancelled countdown must not advance again.
        assertEquals("v2", viewModel.uiState.value.video.id)
    }

    @Test
    fun `watch again replays the same video and cancels autoplay`() = runTest {
        val viewModel = viewModel()

        viewModel.onVideoEnded()
        viewModel.replay()
        advanceTimeBy(10_000); runCurrent()

        assertEquals("v1", viewModel.uiState.value.video.id)
        assertNull(viewModel.uiState.value.end)
    }

    @Test
    fun `last video ends on the finished screen`() = runTest {
        val viewModel = viewModel(startIndex = 2)

        viewModel.onVideoEnded()

        assertEquals(PlayerEnd.Finished, viewModel.uiState.value.end)
    }

    @Test
    fun `controls hide by themselves while playing`() = runTest {
        val viewModel = viewModel()
        viewModel.onPlayingChanged(true)

        viewModel.showControls()
        assertTrue(viewModel.uiState.value.controlsVisible)

        advanceTimeBy(4_000); runCurrent()
        viewModel.onUserInteraction() // a key press keeps them up longer
        advanceTimeBy(4_000); runCurrent()
        assertTrue(viewModel.uiState.value.controlsVisible)

        advanceTimeBy(1_500); runCurrent()
        assertFalse(viewModel.uiState.value.controlsVisible)
    }

    @Test
    fun `controls stay up while paused`() = runTest {
        val viewModel = viewModel()

        viewModel.showControls()
        advanceTimeBy(60_000); runCurrent()

        assertTrue(viewModel.uiState.value.controlsVisible)
    }

    @Test
    fun `controls cannot cover the end screen`() = runTest {
        val viewModel = viewModel(startIndex = 2)

        viewModel.onVideoEnded()
        viewModel.showControls()

        assertFalse(viewModel.uiState.value.controlsVisible)
    }

    @Test
    fun `progress and duration are tracked`() = runTest {
        val viewModel = viewModel()

        viewModel.onDuration(200f)
        viewModel.onProgress(42f)

        assertEquals(42f, viewModel.uiState.value.positionSeconds)
        assertEquals(200f, viewModel.uiState.value.durationSeconds)
    }
}
