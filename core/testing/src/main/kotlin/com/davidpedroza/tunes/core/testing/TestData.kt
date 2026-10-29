package com.davidpedroza.tunes.core.testing

import com.davidpedroza.tunes.core.model.Album
import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.model.Song

object TestData {
    fun song(id: Long, albumId: Long? = 100L, previewUrl: String? = "https://preview/$id.m4a") = Song(
        id = id,
        title = "Song $id",
        artist = "Artist $id",
        albumId = albumId,
        albumTitle = "Album $albumId",
        artworkUrl = "https://art/$id/100x100bb.jpg",
        previewUrl = previewUrl,
        durationMillis = 200_000L,
        trackNumber = id.toInt(),
        genre = "Pop",
    )

    fun album(id: Long = 100L) = Album(
        id = id,
        title = "Album $id",
        artist = "Album Artist",
        artworkUrl = "https://art/album/$id/100x100bb.jpg",
        trackCount = 2,
        releaseYear = 2013,
        genre = "Pop",
        copyright = null,
    )

    fun albumDetails(id: Long = 100L, tracks: List<Song> = listOf(song(1, id), song(2, id))) =
        AlbumDetails(album = album(id), tracks = tracks)
}
