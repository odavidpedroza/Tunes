package com.davidpedroza.tunes.feature.songs

import com.davidpedroza.tunes.core.model.Song

/**
 * Home screen state. Search results are delivered separately as a `Flow<PagingData<Song>>`
 * because Paging owns their lifecycle; this holds everything else.
 */
data class SongsUiState(
    /** Raw text in the search field. */
    val query: String = "",
    /** Normalized query being searched; empty when the home shows the recently played list. */
    val activeSearch: String = "",
    val recentlyPlayed: List<Song> = emptyList(),
    /** Song currently loaded in the player, used to highlight it in lists. */
    val nowPlayingId: Long? = null,
) {
    val isSearching: Boolean get() = activeSearch.isNotEmpty()
}
