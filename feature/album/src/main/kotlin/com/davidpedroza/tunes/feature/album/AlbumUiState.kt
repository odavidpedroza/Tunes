package com.davidpedroza.tunes.feature.album

import com.davidpedroza.tunes.core.model.AlbumDetails

sealed interface AlbumUiState {
    data object Loading : AlbumUiState

    data object NotFound : AlbumUiState

    data class Error(val throwable: Throwable) : AlbumUiState

    data class Content(
        val details: AlbumDetails,
        val isRefreshing: Boolean,
        /** Set when the latest refresh failed but cached data is available. */
        val refreshError: Throwable?,
        val nowPlayingId: Long?,
    ) : AlbumUiState
}
