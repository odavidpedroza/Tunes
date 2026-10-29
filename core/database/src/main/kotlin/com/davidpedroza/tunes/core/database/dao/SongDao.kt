package com.davidpedroza.tunes.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.davidpedroza.tunes.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE id = :id")
    fun observeSong(id: Long): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongs(ids: List<Long>): List<SongEntity>
}
