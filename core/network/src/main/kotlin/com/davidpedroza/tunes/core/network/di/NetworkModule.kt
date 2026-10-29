package com.davidpedroza.tunes.core.network.di

import com.davidpedroza.tunes.core.network.BuildConfig
import com.davidpedroza.tunes.core.network.SongsRemoteDataSource
import com.davidpedroza.tunes.core.network.itunes.ItunesConfig
import com.davidpedroza.tunes.core.network.itunes.ItunesService
import com.davidpedroza.tunes.core.network.itunes.ItunesSongsRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkModule {

    /** Swap this binding to point the whole app at another catalog API. */
    @Binds
    abstract fun bindSongsRemoteDataSource(impl: ItunesSongsRemoteDataSource): SongsRemoteDataSource

    companion object {
        private const val ITUNES_BASE_URL = "https://itunes.apple.com/"

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
                },
            )
            .build()

        @Provides
        @Singleton
        fun provideItunesService(client: OkHttpClient, json: Json): ItunesService = Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()

        @Provides
        @Singleton
        fun provideItunesConfig(): ItunesConfig =
            ItunesConfig(country = Locale.getDefault().country.ifBlank { "US" })
    }
}
