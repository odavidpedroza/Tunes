package com.davidpedroza.tunes.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.davidpedroza.tunes.core.database.entity.AlbumEntity
import com.davidpedroza.tunes.core.database.entity.AlbumTrackEntity
import com.davidpedroza.tunes.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Upsert
    suspend fun upsertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<AlbumTrackEntity>)

    @Query("DELETE FROM album_tracks WHERE albumId = :albumId")
    suspend fun clearTracks(albumId: Long)

    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun observeAlbum(albumId: Long): Flow<AlbumEntity?>

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN album_tracks ON songs.id = album_tracks.songId
        WHERE album_tracks.albumId = :albumId
        ORDER BY album_tracks.position ASC
        """,
    )
    fun observeTracks(albumId: Long): Flow<List<SongEntity>>
}
