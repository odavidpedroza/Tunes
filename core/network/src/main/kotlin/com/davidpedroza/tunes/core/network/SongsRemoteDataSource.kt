package com.davidpedroza.tunes.core.network

import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.model.Song

/**
 * Network boundary of the app. The rest of the code base only knows this contract, so the
 * backing API (iTunes today) can be swapped by providing another implementation in DI.
 *
 * Implementations translate transport errors into [com.davidpedroza.tunes.core.common.TunesException].
 */
interface SongsRemoteDataSource {

    /** Returns the zero-based [page] of songs matching [term]. */
    suspend fun searchSongs(term: String, page: Int, pageSize: Int): SongsPage

    /** Returns the album with [albumId] and its tracks, or `null` when the store does not know it. */
    suspend fun getAlbum(albumId: Long): AlbumDetails?
}

data class SongsPage(
    val songs: List<Song>,
    /** `true` when the source has no more pages after this one. */
    val endReached: Boolean,
)
