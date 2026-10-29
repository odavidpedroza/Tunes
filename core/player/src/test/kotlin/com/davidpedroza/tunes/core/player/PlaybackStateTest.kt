package com.davidpedroza.tunes.core.player

import com.davidpedroza.tunes.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStateTest {

    private fun song(id: Long) = Song(id, "S$id", "A", null, null, null, "u", 1000, null, null)

    @Test
    fun `currentSong follows the index`() {
        val state = PlaybackState(queue = listOf(song(1), song(2)), currentIndex = 1)
        assertEquals(2L, state.currentSong?.id)
        assertNull(PlaybackState().currentSong)
    }

    @Test
    fun `hasNext depends on position unless repeating`() {
        val queue = listOf(song(1), song(2))
        assertTrue(PlaybackState(queue = queue, currentIndex = 0).hasNext)
        assertFalse(PlaybackState(queue = queue, currentIndex = 1).hasNext)
        assertTrue(PlaybackState(queue = queue, currentIndex = 1, repeatMode = RepeatMode.ALL).hasNext)
    }

    @Test
    fun `progress is a clamped ratio`() {
        assertEquals(0.5f, PlaybackState(positionMillis = 50, durationMillis = 100).progress)
        assertEquals(1f, PlaybackState(positionMillis = 500, durationMillis = 100).progress)
        assertEquals(0f, PlaybackState(positionMillis = 50, durationMillis = 0).progress)
    }
}
