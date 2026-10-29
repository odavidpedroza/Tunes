package com.davidpedroza.tunes.feature.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.data.repository.AlbumRepository
import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val albumRepository: AlbumRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {

    // Read through the handle's map rather than `toRoute()` so the ViewModel is testable without Android's Bundle.
    private val albumId: Long = requireNotNull(savedStateHandle.get<Long>(AlbumRoute::albumId.name)) {
        "AlbumViewModel needs the albumId route argument"
    }

    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Loading)

    val uiState: StateFlow<AlbumUiState> = combine(
        albumRepository.observeAlbum(albumId),
        refreshState,
        playbackController.state.map { it.currentSong?.id }.distinctUntilChanged(),
    ) { details, refresh, nowPlayingId -> reduce(details, refresh, nowPlayingId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AlbumUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshState.value = RefreshState.Loading
            refreshState.value = try {
                if (albumRepository.refreshAlbum(albumId)) RefreshState.Idle else RefreshState.NotFound
            } catch (e: TunesException) {
                RefreshState.Failed(e)
            }
        }
    }

    /** Plays the album from [index]; the caller navigates to the player. */
    fun onTrackSelected(tracks: List<Song>, index: Int) {
        playbackController.play(tracks, index)
    }

    private fun reduce(details: AlbumDetails?, refresh: RefreshState, nowPlayingId: Long?): AlbumUiState = when {
        details != null -> AlbumUiState.Content(
            details = details,
            isRefreshing = refresh is RefreshState.Loading,
            refreshError = (refresh as? RefreshState.Failed)?.throwable,
            nowPlayingId = nowPlayingId,
        )
        refresh is RefreshState.Loading -> AlbumUiState.Loading
        refresh is RefreshState.Failed -> AlbumUiState.Error(refresh.throwable)
        else -> AlbumUiState.NotFound
    }

    private sealed interface RefreshState {
        data object Loading : RefreshState
        data object Idle : RefreshState
        data object NotFound : RefreshState
        data class Failed(val throwable: Throwable) : RefreshState
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
