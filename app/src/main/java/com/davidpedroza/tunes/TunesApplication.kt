package com.davidpedroza.tunes

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.davidpedroza.tunes.playback.PlaybackHistoryTracker
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class TunesApplication : Application(), SingletonImageLoader.Factory {

    @Inject lateinit var okHttpClient: OkHttpClient
    @Inject lateinit var playbackHistoryTracker: PlaybackHistoryTracker

    override fun onCreate() {
        super.onCreate()
        playbackHistoryTracker.start()
    }

    /** Coil shares the app's OkHttp client so artwork benefits from the same connection pool. */
    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(context)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient })) }
        .crossfade(true)
        .build()
}
