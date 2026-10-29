package com.davidpedroza.tunes.di

import com.davidpedroza.tunes.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** A scope that lives as long as the process, for work that must outlive any screen. */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(dispatchers: DispatcherProvider): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatchers.default)
}
