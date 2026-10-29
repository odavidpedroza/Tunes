package com.davidpedroza.tunes.core.player.di

import com.davidpedroza.tunes.core.player.ExoPlayerPlaybackController
import com.davidpedroza.tunes.core.player.PlaybackController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlayerModule {

    @Binds
    abstract fun bindPlaybackController(impl: ExoPlayerPlaybackController): PlaybackController
}
