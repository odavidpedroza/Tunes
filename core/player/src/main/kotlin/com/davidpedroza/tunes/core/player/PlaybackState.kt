package com.davidpedroza.tunes.core.player

import com.davidpedroza.tunes.core.model.Song

enum class RepeatMode { OFF, ALL, ONE }

sealed interface PlaybackError {
    /** The selected song has no preview stream in the catalog. */
    data object PreviewUnavailable : PlaybackError

    /** The stream could not be loaded or decoded. */
    data class Playback(val message: String?) : PlaybackError
}

data class PlaybackState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val error: PlaybackError? = null,
) {
    val currentSong: Song? get() = queue.getOrNull(currentIndex)
    val hasNext: Boolean get() = repeatMode != RepeatMode.OFF || currentIndex in 0 until queue.lastIndex
    val hasPrevious: Boolean get() = queue.isNotEmpty()
    val progress: Float get() = if (durationMillis > 0) (positionMillis.toFloat() / durationMillis).coerceIn(0f, 1f) else 0f
}
