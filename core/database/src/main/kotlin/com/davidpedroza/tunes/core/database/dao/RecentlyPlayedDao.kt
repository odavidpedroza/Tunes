package com.davidpedroza.tunes.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.davidpedroza.tunes.core.database.entity.RecentlyPlayedEntity
import com.davidpedroza.tunes.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

    @Upsert
    suspend fun upsert(entry: RecentlyPlayedEntity)

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN recently_played ON songs.id = recently_played.songId
        ORDER BY recently_played.playedAtMillis DESC
        LIMIT :limit
        """,
    )
    fun observeRecentlyPlayed(limit: Int): Flow<List<SongEntity>>

    /** Keeps only the [keep] most recent entries. */
    @Query(
        """
        DELETE FROM recently_played WHERE songId NOT IN (
            SELECT songId FROM recently_played ORDER BY playedAtMillis DESC LIMIT :keep
        )
        """,
    )
    suspend fun trim(keep: Int)
}
