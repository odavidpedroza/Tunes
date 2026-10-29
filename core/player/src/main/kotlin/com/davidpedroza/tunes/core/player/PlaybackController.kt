package com.davidpedroza.tunes.core.player

import com.davidpedroza.tunes.core.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * Playback boundary of the app. The UI only talks to this contract; the engine behind it
 * (ExoPlayer today) can be replaced or faked in tests.
 *
 * All methods must be called from the main thread.
 */
interface PlaybackController {

    val state: StateFlow<PlaybackState>

    /** Replaces the queue with [songs] and starts playing the one at [startIndex]. */
    fun play(songs: List<Song>, startIndex: Int)

    fun togglePlayPause()

    fun seekTo(positionMillis: Long)

    fun skipToNext()

    fun skipToPrevious()

    fun toggleRepeatMode()

    /** Frees the underlying engine. The controller must not be used afterwards. */
    fun release()
}
