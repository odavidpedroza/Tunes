package com.davidpedroza.tunes.core.database.di

import android.content.Context
import androidx.room.Room
import com.davidpedroza.tunes.core.database.TunesDatabase
import com.davidpedroza.tunes.core.database.dao.AlbumDao
import com.davidpedroza.tunes.core.database.dao.RecentlyPlayedDao
import com.davidpedroza.tunes.core.database.dao.SearchDao
import com.davidpedroza.tunes.core.database.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TunesDatabase =
        Room.databaseBuilder(context, TunesDatabase::class.java, TunesDatabase.NAME)
            // The database is a cache of remote data; losing it on a schema bump is acceptable.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideSongDao(db: TunesDatabase): SongDao = db.songDao()
    @Provides fun provideSearchDao(db: TunesDatabase): SearchDao = db.searchDao()
    @Provides fun provideRecentlyPlayedDao(db: TunesDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()
    @Provides fun provideAlbumDao(db: TunesDatabase): AlbumDao = db.albumDao()
}
