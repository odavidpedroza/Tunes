package com.davidpedroza.tunes.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Membership of a song in the cached result list of a search [query].
 * [position] keeps the order the API returned so paging from the database is deterministic.
 */
@Entity(
    tableName = "search_results",
    primaryKeys = ["query", "songId"],
    indices = [Index(value = ["query", "position"])],
)
data class SearchResultEntity(
    val query: String,
    val songId: Long,
    val position: Int,
)

/** Paging bookkeeping for a search [query]: where to resume and whether the source is exhausted. */
@Entity(tableName = "search_remote_keys")
data class SearchRemoteKeyEntity(
    @PrimaryKey val query: String,
    val nextPage: Int,
    val endReached: Boolean,
    val lastUpdatedMillis: Long,
)
