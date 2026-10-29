package com.davidpedroza.tunes.core.model

/** A playable song. [id] is stable across the catalog (iTunes `trackId`). */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val albumId: Long?,
    val albumTitle: String?,
    /** Template artwork URL (typically 100x100). Use [Artwork.sized] to request another size. */
    val artworkUrl: String?,
    /** 30-second preview stream, when the store provides one. */
    val previewUrl: String?,
    val durationMillis: Long?,
    val trackNumber: Int?,
    val genre: String?,
)
