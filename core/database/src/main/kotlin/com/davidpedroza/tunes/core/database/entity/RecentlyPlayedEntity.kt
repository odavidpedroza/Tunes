package com.davidpedroza.tunes.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recently_played",
    indices = [Index(value = ["playedAtMillis"])],
)
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: Long,
    val playedAtMillis: Long,
)
