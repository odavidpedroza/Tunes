package com.davidpedroza.tunes.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidpedroza.tunes.core.player.PlaybackController
import com.davidpedroza.tunes.core.player.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Thin mediator between the player screen and the app-wide [PlaybackController]:
 * the controller owns the queue and position so playback survives navigation.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackController: PlaybackController,
) : ViewModel() {

    val uiState: StateFlow<PlaybackState> = playbackController.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = playbackController.state.value,
    )

    fun onPlayPause() = playbackController.togglePlayPause()

    fun onSeekTo(positionMillis: Long) = playbackController.seekTo(positionMillis)

    fun onSkipNext() = playbackController.skipToNext()

    fun onSkipPrevious() = playbackController.skipToPrevious()

    fun onToggleRepeat() = playbackController.toggleRepeatMode()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
