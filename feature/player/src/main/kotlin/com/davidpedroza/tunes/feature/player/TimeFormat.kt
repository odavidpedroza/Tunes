package com.davidpedroza.tunes.feature.player

import java.util.Locale

/** Formats a duration as `m:ss` (or `h:mm:ss` past an hour). */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0L) + 500) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}
