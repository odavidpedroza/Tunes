package com.davidpedroza.tunes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.davidpedroza.tunes.core.database.dao.AlbumDao
import com.davidpedroza.tunes.core.database.dao.RecentlyPlayedDao
import com.davidpedroza.tunes.core.database.dao.SearchDao
import com.davidpedroza.tunes.core.database.dao.SongDao
import com.davidpedroza.tunes.core.database.entity.AlbumEntity
import com.davidpedroza.tunes.core.database.entity.AlbumTrackEntity
import com.davidpedroza.tunes.core.database.entity.RecentlyPlayedEntity
import com.davidpedroza.tunes.core.database.entity.SearchRemoteKeyEntity
import com.davidpedroza.tunes.core.database.entity.SearchResultEntity
import com.davidpedroza.tunes.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        SearchResultEntity::class,
        SearchRemoteKeyEntity::class,
        RecentlyPlayedEntity::class,
        AlbumEntity::class,
        AlbumTrackEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class TunesDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun searchDao(): SearchDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun albumDao(): AlbumDao

    companion object {
        const val NAME = "tunes.db"
    }
}
