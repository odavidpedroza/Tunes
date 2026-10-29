package com.davidpedroza.tunes.core.data.di

import com.davidpedroza.tunes.core.common.DefaultDispatcherProvider
import com.davidpedroza.tunes.core.common.DispatcherProvider
import com.davidpedroza.tunes.core.common.TimeProvider
import com.davidpedroza.tunes.core.data.local.RoomTransactionRunner
import com.davidpedroza.tunes.core.data.local.TransactionRunner
import com.davidpedroza.tunes.core.data.paging.RoomSearchLocalDataSource
import com.davidpedroza.tunes.core.data.paging.SearchLocalDataSource
import com.davidpedroza.tunes.core.data.repository.AlbumRepository
import com.davidpedroza.tunes.core.data.repository.OfflineFirstAlbumRepository
import com.davidpedroza.tunes.core.data.repository.OfflineFirstRecentlyPlayedRepository
import com.davidpedroza.tunes.core.data.repository.OfflineFirstSongsRepository
import com.davidpedroza.tunes.core.data.repository.RecentlyPlayedRepository
import com.davidpedroza.tunes.core.data.repository.SongsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds abstract fun bindSongsRepository(impl: OfflineFirstSongsRepository): SongsRepository
    @Binds abstract fun bindRecentlyPlayedRepository(impl: OfflineFirstRecentlyPlayedRepository): RecentlyPlayedRepository
    @Binds abstract fun bindAlbumRepository(impl: OfflineFirstAlbumRepository): AlbumRepository
    @Binds abstract fun bindSearchLocalDataSource(impl: RoomSearchLocalDataSource): SearchLocalDataSource
    @Binds abstract fun bindTransactionRunner(impl: RoomTransactionRunner): TransactionRunner

    companion object {
        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

        @Provides
        @Singleton
        fun provideTimeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }
    }
}
