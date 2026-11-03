package com.davidpedroza.tunes.core.player

/**
 * Engine-agnostic view of what the `Player` reported in one event batch. Keeping this a plain data
 * class lets the state mapping ([reduce]) be unit-tested without Media3.
 */
internal data class PlayerSnapshot(
    val mediaItemCount: Int,
    val currentIndex: Int,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    /** True while the engine is buffering or ready, i.e. the current item loaded (or is loading) fine. */
    val isActive: Boolean,
    val positionMillis: Long,
    /** `null` while the engine does not know the duration yet. */
    val durationMillis: Long?,
    val repeatMode: RepeatMode,
    /** The current item changed in this batch (skip, auto-advance, new queue). */
    val isTransition: Boolean,
    /** A player error was raised in this batch; the error itself is stored by the error callback. */
    val hasNewError: Boolean,
)

/** Applies an engine [snapshot] to the current state. */
internal fun PlaybackState.reduce(snapshot: PlayerSnapshot): PlaybackState {
    // The selected song has no stream: the engine has nothing to say about it, keep what play() exposed.
    if (error == PlaybackError.PreviewUnavailable) return copy(isPlaying = false, isBuffering = false)

    return copy(
        currentIndex = if (snapshot.mediaItemCount > 0) snapshot.currentIndex else -1,
        isPlaying = snapshot.isPlaying,
        isBuffering = snapshot.isBuffering,
        positionMillis = snapshot.positionMillis.coerceAtLeast(0L),
        // Never guess from catalogue metadata (the full track length is not the preview length):
        // keep the last engine-reported duration until the new one is known.
        durationMillis = snapshot.durationMillis?.coerceAtLeast(0L) ?: durationMillis,
        repeatMode = snapshot.repeatMode,
        error = when {
            snapshot.hasNewError -> error
            // A stream error is over once a (re)prepare makes progress or another song starts.
            error is PlaybackError.Playback && (snapshot.isTransition || snapshot.isActive) -> null
            else -> error
        },
    )
}
