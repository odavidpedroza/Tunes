package com.davidpedroza.tunes.core.network.itunes

import com.davidpedroza.tunes.core.model.Album
import com.davidpedroza.tunes.core.model.Song

private const val WRAPPER_TRACK = "track"
private const val WRAPPER_COLLECTION = "collection"
private const val KIND_SONG = "song"

/** Maps a result to a [Song], or `null` when the result is not a song (music video, artist...). */
internal fun ItunesResultDto.toSongOrNull(): Song? {
    if (wrapperType != WRAPPER_TRACK || kind != KIND_SONG) return null
    val id = trackId ?: return null
    val title = trackName ?: return null
    return Song(
        id = id,
        title = title,
        artist = artistName.orEmpty(),
        albumId = collectionId,
        albumTitle = collectionName,
        artworkUrl = artworkUrl100 ?: artworkUrl60,
        previewUrl = previewUrl,
        durationMillis = trackTimeMillis,
        trackNumber = trackNumber,
        genre = primaryGenreName,
    )
}

/** Maps a result to an [Album], or `null` when the result is not a collection. */
internal fun ItunesResultDto.toAlbumOrNull(): Album? {
    if (wrapperType != WRAPPER_COLLECTION) return null
    val id = collectionId ?: return null
    val title = collectionName ?: return null
    return Album(
        id = id,
        title = title,
        artist = artistName.orEmpty(),
        artworkUrl = artworkUrl100 ?: artworkUrl60,
        trackCount = trackCount,
        releaseYear = releaseDate?.take(4)?.toIntOrNull(),
        genre = primaryGenreName,
        copyright = copyright,
    )
}
