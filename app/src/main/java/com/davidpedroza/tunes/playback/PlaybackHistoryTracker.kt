package com.davidpedroza.tunes.playback

import com.davidpedroza.tunes.core.data.repository.RecentlyPlayedRepository
import com.davidpedroza.tunes.core.player.PlaybackController
import com.davidpedroza.tunes.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records every song that starts playing into the "recently played" history.
 * Lives in the app module because it is the only place that knows both the player and the data layer.
 */
@Singleton
class PlaybackHistoryTracker @Inject constructor(
    private val playbackController: PlaybackController,
    private val recentlyPlayedRepository: RecentlyPlayedRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            playbackController.state
                .map { state -> state.currentSong.takeIf { state.error == null } }
                .filterNotNull()
                .distinctUntilChanged { old, new -> old.id == new.id }
                .onEach { song -> recentlyPlayedRepository.markPlayed(song) }
                .collect()
        }
    }
}
