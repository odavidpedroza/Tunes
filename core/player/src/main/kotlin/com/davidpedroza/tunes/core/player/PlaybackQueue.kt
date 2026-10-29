package com.davidpedroza.tunes.core.player

import com.davidpedroza.tunes.core.model.Song

/**
 * Pure helper that prepares a queue for the engine: songs without a preview stream are dropped
 * (the store does not always provide one) and the start index is re-mapped accordingly.
 */
internal object PlaybackQueue {

    data class Prepared(val songs: List<Song>, val startIndex: Int)

    /** Returns `null` when the song at [startIndex] cannot be played. */
    fun prepare(songs: List<Song>, startIndex: Int): Prepared? {
        val selected = songs.getOrNull(startIndex) ?: return null
        if (selected.previewUrl.isNullOrBlank()) return null
        val playable = songs.filter { !it.previewUrl.isNullOrBlank() }
        val newIndex = playable.indexOfFirst { it.id == selected.id }
        return Prepared(playable, newIndex)
    }
}
