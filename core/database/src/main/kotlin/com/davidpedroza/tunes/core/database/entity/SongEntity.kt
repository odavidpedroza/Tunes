package com.davidpedroza.tunes.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local copy of a song. Shared by search results, recently played and album tracks. */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val albumId: Long?,
    val albumTitle: String?,
    val artworkUrl: String?,
    val previewUrl: String?,
    val durationMillis: Long?,
    val trackNumber: Int?,
    val genre: String?,
)
