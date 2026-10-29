package com.davidpedroza.tunes.feature.player

import com.davidpedroza.tunes.core.player.RepeatMode
import com.davidpedroza.tunes.core.testing.FakePlaybackController
import com.davidpedroza.tunes.core.testing.MainDispatcherRule
import com.davidpedroza.tunes.core.testing.TestData.song
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val playbackController = FakePlaybackController()
    private val viewModel by lazy { PlayerViewModel(playbackController) }

    @Test
    fun `ui state mirrors the playback controller`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect() }
        playbackController.play(listOf(song(1), song(2)), 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1L, state.currentSong?.id)
        assertTrue(state.isPlaying)
        assertTrue(state.hasNext)
    }

    @Test
    fun `actions are forwarded to the controller`() = runTest {
        playbackController.play(listOf(song(1), song(2)), 0)

        viewModel.onPlayPause()
        viewModel.onSeekTo(12_000)
        viewModel.onSkipNext()
        viewModel.onSkipPrevious()
        viewModel.onToggleRepeat()

        assertEquals(
            listOf("play(0)", "togglePlayPause", "seekTo(12000)", "skipToNext", "skipToPrevious", "toggleRepeatMode"),
            playbackController.calls,
        )
        assertFalse(playbackController.state.value.isPlaying)
        assertEquals(RepeatMode.ALL, playbackController.state.value.repeatMode)
    }
}
