package com.davidpedroza.tunes.core.model

/** Helpers for store artwork URLs, which encode the size in the file name (`.../100x100bb.jpg`). */
object Artwork {
    private val sizePattern = Regex("""\d+x\d+(bb)?(?=\.[A-Za-z]+$)""")

    const val THUMBNAIL = 200
    const val LARGE = 600

    /** Rewrites [url] to request a [size]x[size] rendition; returns the input untouched if it has no size token. */
    fun sized(url: String?, size: Int): String? =
        url?.replace(sizePattern) { match -> "${size}x$size${match.groupValues[1]}" }
}
