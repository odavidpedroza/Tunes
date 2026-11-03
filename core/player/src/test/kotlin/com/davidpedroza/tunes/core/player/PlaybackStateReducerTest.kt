package com.davidpedroza.tunes.core.player

import com.davidpedroza.tunes.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackStateReducerTest {

    private fun song(id: Long) = Song(
        id = id,
        title = "Song $id",
        artist = "Artist",
        albumId = 100L,
        albumTitle = "Album",
        artworkUrl = null,
        previewUrl = "https://p/$id.m4a",
        durationMillis = 200_000L,
        trackNumber = id.toInt(),
        genre = "Pop",
    )

    private fun snapshot(
        mediaItemCount: Int = 2,
        currentIndex: Int = 1,
        isPlaying: Boolean = true,
        isBuffering: Boolean = false,
        isActive: Boolean = true,
        positionMillis: Long = 5_000,
        durationMillis: Long? = 30_000,
        repeatMode: RepeatMode = RepeatMode.OFF,
        isTransition: Boolean = false,
        hasNewError: Boolean = false,
    ) = PlayerSnapshot(
        mediaItemCount, currentIndex, isPlaying, isBuffering, isActive,
        positionMillis, durationMillis, repeatMode, isTransition, hasNewError,
    )

    @Test
    fun mapsEngineFieldsOntoTheState() {
        val state = PlaybackState(queue = listOf(song(1), song(2))).reduce(snapshot(repeatMode = RepeatMode.ALL))

        assertEquals(1, state.currentIndex)
        assertEquals(song(2), state.currentSong)
        assertEquals(5_000L, state.positionMillis)
        assertEquals(30_000L, state.durationMillis)
        assertEquals(RepeatMode.ALL, state.repeatMode)
        assertEquals(true, state.isPlaying)
    }

    @Test
    fun unknownDurationKeepsThePreviousOneInsteadOfGuessing() {
        val playing = PlaybackState(queue = listOf(song(1), song(2)), durationMillis = 30_000)

        val state = playing.reduce(snapshot(durationMillis = null, isTransition = true, positionMillis = 0))

        assertEquals(30_000L, state.durationMillis)
        assertEquals(0L, state.positionMillis)
    }

    @Test
    fun emptyEngineQueueClearsTheIndex() {
        val state = PlaybackState(queue = listOf(song(1)), currentIndex = 0).reduce(snapshot(mediaItemCount = 0, currentIndex = 0))

        assertEquals(-1, state.currentIndex)
        assertNull(state.currentSong)
    }

    @Test
    fun unplayableSelectionIsNotOverriddenByTheEngine() {
        val selected = PlaybackState(queue = listOf(song(1)), currentIndex = 0, error = PlaybackError.PreviewUnavailable)

        // The engine still holds (and reports) the previous queue, e.g. two items with index 1.
        val state = selected.reduce(snapshot(mediaItemCount = 2, currentIndex = 1, isPlaying = false))

        assertEquals(0, state.currentIndex)
        assertEquals(song(1), state.currentSong)
        assertEquals(PlaybackError.PreviewUnavailable, state.error)
        assertFalse(state.isPlaying)
        assertFalse(state.isBuffering)
    }

    @Test
    fun streamErrorIsKeptWhileRaisedAndClearedOnceTheEngineRecovers() {
        val failed = PlaybackState(queue = listOf(song(1)), error = PlaybackError.Playback("boom"))

        val stillFailed = failed.reduce(snapshot(isPlaying = false, isActive = false, hasNewError = true))
        assertEquals(PlaybackError.Playback("boom"), stillFailed.error)

        val idle = failed.reduce(snapshot(isPlaying = false, isActive = false))
        assertEquals(PlaybackError.Playback("boom"), idle.error)

        val recovered = failed.reduce(snapshot(isBuffering = true, isActive = true, isPlaying = false))
        assertNull(recovered.error)

        val nextSong = failed.reduce(snapshot(isActive = false, isPlaying = false, isTransition = true))
        assertNull(nextSong.error)
    }
}
