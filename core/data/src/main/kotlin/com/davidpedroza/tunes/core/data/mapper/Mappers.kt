package com.davidpedroza.tunes.core.data.mapper

import com.davidpedroza.tunes.core.database.entity.AlbumEntity
import com.davidpedroza.tunes.core.database.entity.SongEntity
import com.davidpedroza.tunes.core.model.Album
import com.davidpedroza.tunes.core.model.Song

internal fun SongEntity.toModel(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    previewUrl = previewUrl,
    durationMillis = durationMillis,
    trackNumber = trackNumber,
    genre = genre,
)

internal fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artist = artist,
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = artworkUrl,
    previewUrl = previewUrl,
    durationMillis = durationMillis,
    trackNumber = trackNumber,
    genre = genre,
)

internal fun AlbumEntity.toModel(): Album = Album(
    id = id,
    title = title,
    artist = artist,
    artworkUrl = artworkUrl,
    trackCount = trackCount,
    releaseYear = releaseYear,
    genre = genre,
    copyright = copyright,
)

internal fun Album.toEntity(updatedAtMillis: Long): AlbumEntity = AlbumEntity(
    id = id,
    title = title,
    artist = artist,
    artworkUrl = artworkUrl,
    trackCount = trackCount,
    releaseYear = releaseYear,
    genre = genre,
    copyright = copyright,
    lastUpdatedMillis = updatedAtMillis,
)
