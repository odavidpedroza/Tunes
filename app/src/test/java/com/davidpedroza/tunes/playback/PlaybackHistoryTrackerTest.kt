package com.davidpedroza.tunes.playback

import com.davidpedroza.tunes.core.player.PlaybackError
import com.davidpedroza.tunes.core.player.PlaybackState
import com.davidpedroza.tunes.core.testing.FakePlaybackController
import com.davidpedroza.tunes.core.testing.FakeRecentlyPlayedRepository
import com.davidpedroza.tunes.core.testing.TestData.song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackHistoryTrackerTest {

    private val playback = FakePlaybackController()
    private val history = FakeRecentlyPlayedRepository()

    // The tracker collects in backgroundScope, whose tasks advanceUntilIdle() skips: runCurrent() runs them.
    @Test
    fun everySongThatStartsPlayingIsRecordedOnce() = runTest {
        PlaybackHistoryTracker(playback, history, backgroundScope).start()
        val songs = listOf(song(1), song(2))

        playback.play(songs, 0)
        runCurrent()
        playback.togglePlayPause() // same song, different state: not a new play
        runCurrent()
        playback.skipToNext()
        runCurrent()

        assertEquals(listOf(1L, 2L), history.played.map { it.id })
    }

    @Test
    fun songsThatCouldNotPlayAreNotRecorded() = runTest {
        PlaybackHistoryTracker(playback, history, backgroundScope).start()

        playback.setState(PlaybackState(queue = listOf(song(1, previewUrl = null)), currentIndex = 0, error = PlaybackError.PreviewUnavailable))
        runCurrent()
        playback.setState(PlaybackState(queue = listOf(song(2)), currentIndex = 0, error = PlaybackError.Playback("stream failed")))
        runCurrent()

        assertEquals(emptyList<Long>(), history.played.map { it.id })
    }

    @Test
    fun replayingASongAfterAnotherOneIsRecordedAgain() = runTest {
        PlaybackHistoryTracker(playback, history, backgroundScope).start()

        playback.play(listOf(song(1), song(2)), 0)
        runCurrent()
        playback.skipToNext()
        runCurrent()
        playback.skipToPrevious()
        runCurrent()

        assertEquals(listOf(1L, 2L, 1L), history.played.map { it.id })
        assertEquals(listOf(1L, 2L), history.recentlyPlayed.value.map { it.id })
    }

    @Test
    fun startIsIdempotent() = runTest {
        val tracker = PlaybackHistoryTracker(playback, history, backgroundScope)
        tracker.start()
        tracker.start()

        playback.play(listOf(song(1)), 0)
        runCurrent()

        assertEquals(listOf(1L), history.played.map { it.id })
    }
}
