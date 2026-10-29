package com.davidpedroza.tunes.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.davidpedroza.tunes.core.database.entity.SearchRemoteKeyEntity
import com.davidpedroza.tunes.core.database.entity.SearchResultEntity
import com.davidpedroza.tunes.core.database.entity.SongEntity

@Dao
interface SearchDao {

    /** Songs of a cached search, in API order. Backs the Paging 3 [PagingSource] used by the UI. */
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN search_results ON songs.id = search_results.songId
        WHERE search_results.`query` = :query
        ORDER BY search_results.position ASC
        """,
    )
    fun pagingSource(query: String): PagingSource<Int, SongEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertResults(results: List<SearchResultEntity>)

    @Query("SELECT COUNT(*) FROM search_results WHERE `query` = :query")
    suspend fun countResults(query: String): Int

    @Query("DELETE FROM search_results WHERE `query` = :query")
    suspend fun clearResults(query: String)

    @Query("SELECT * FROM search_remote_keys WHERE `query` = :query")
    suspend fun getRemoteKey(query: String): SearchRemoteKeyEntity?

    @Upsert
    suspend fun upsertRemoteKey(key: SearchRemoteKeyEntity)

    @Query("DELETE FROM search_remote_keys WHERE `query` = :query")
    suspend fun clearRemoteKey(query: String)
}
