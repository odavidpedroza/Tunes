package com.davidpedroza.tunes.core.data.repository

import com.davidpedroza.tunes.core.model.AlbumDetails
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {

    /** The cached album and its tracks; `null` until [refreshAlbum] has succeeded at least once. */
    fun observeAlbum(albumId: Long): Flow<AlbumDetails?>

    /**
     * Fetches the album from the network and updates the cache.
     * Returns `false` when the catalog does not know [albumId].
     * @throws com.davidpedroza.tunes.core.common.TunesException on network failures.
     */
    suspend fun refreshAlbum(albumId: Long): Boolean
}
