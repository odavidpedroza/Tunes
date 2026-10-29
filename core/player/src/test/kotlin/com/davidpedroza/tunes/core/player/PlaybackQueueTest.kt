package com.davidpedroza.tunes.core.player

import com.davidpedroza.tunes.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackQueueTest {

    private fun song(id: Long, previewUrl: String? = "https://preview/$id") = Song(
        id = id, title = "Song $id", artist = "Artist", albumId = null, albumTitle = null,
        artworkUrl = null, previewUrl = previewUrl, durationMillis = 1000, trackNumber = null, genre = null,
    )

    @Test
    fun `prepare keeps playable songs and remaps the start index`() {
        val songs = listOf(song(1), song(2, previewUrl = null), song(3))

        val prepared = requireNotNull(PlaybackQueue.prepare(songs, startIndex = 2))

        assertEquals(listOf(1L, 3L), prepared.songs.map { it.id })
        assertEquals(1, prepared.startIndex)
    }

    @Test
    fun `prepare is null when the selected song has no preview`() {
        assertNull(PlaybackQueue.prepare(listOf(song(1), song(2, previewUrl = null)), startIndex = 1))
    }

    @Test
    fun `prepare is null for an out of range index`() {
        assertNull(PlaybackQueue.prepare(listOf(song(1)), startIndex = 5))
    }
}
