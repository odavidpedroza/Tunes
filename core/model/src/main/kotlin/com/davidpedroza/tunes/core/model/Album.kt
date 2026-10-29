package com.davidpedroza.tunes.core.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val trackCount: Int?,
    val releaseYear: Int?,
    val genre: String?,
    val copyright: String?,
)

data class AlbumDetails(
    val album: Album,
    val tracks: List<Song>,
)
