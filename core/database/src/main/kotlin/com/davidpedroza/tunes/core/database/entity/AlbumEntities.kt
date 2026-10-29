package com.davidpedroza.tunes.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val trackCount: Int?,
    val releaseYear: Int?,
    val genre: String?,
    val copyright: String?,
    val lastUpdatedMillis: Long,
)

/** Ordered membership of a song in an album's track list. */
@Entity(
    tableName = "album_tracks",
    primaryKeys = ["albumId", "songId"],
)
data class AlbumTrackEntity(
    val albumId: Long,
    val songId: Long,
    val position: Int,
)
