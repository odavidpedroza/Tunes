package com.davidpedroza.tunes.feature.songs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.davidpedroza.tunes.core.data.repository.RecentlyPlayedRepository
import com.davidpedroza.tunes.core.data.repository.SearchQuery
import com.davidpedroza.tunes.core.data.repository.SongsRepository
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.withIndex
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SongsViewModel @Inject constructor(
    songsRepository: SongsRepository,
    recentlyPlayedRepository: RecentlyPlayedRepository,
    private val playbackController: PlaybackController,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    /**
     * Normalized term, debounced so that typing does not fire a request per keystroke
     * (the catalog API allows roughly 20 calls per minute). The first value is the restored
     * query (empty on a cold start) and is not debounced, so a restored search shows at once.
     */
    @OptIn(FlowPreview::class)
    private val debouncedSearch: Flow<String> = query
        .map(SearchQuery::normalize)
        .withIndex()
        .debounce { (index, term) -> if (index == 0 || term.isEmpty()) 0.milliseconds else SEARCH_DEBOUNCE }
        .map { it.value }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<Song>> = debouncedSearch
        .flatMapLatest { term -> songsRepository.searchSongs(term) }
        .cachedIn(viewModelScope)

    val uiState: StateFlow<SongsUiState> = combine(
        query,
        debouncedSearch,
        recentlyPlayedRepository.observeRecentlyPlayed(),
        playbackController.state.map { it.currentSong?.id }.distinctUntilChanged(),
    ) { query, activeSearch, recentlyPlayed, nowPlayingId ->
        SongsUiState(
            query = query,
            activeSearch = activeSearch,
            recentlyPlayed = recentlyPlayed,
            nowPlayingId = nowPlayingId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SongsUiState(query = query.value, activeSearch = SearchQuery.normalize(query.value)),
    )

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun onClearQuery() = onQueryChange("")

    /** Starts playing [songs] from [index]; the caller navigates to the player. */
    fun onSongSelected(songs: List<Song>, index: Int) {
        playbackController.play(songs, index)
    }

    companion object {
        private const val KEY_QUERY = "query"
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        val SEARCH_DEBOUNCE = 350.milliseconds
    }
}
