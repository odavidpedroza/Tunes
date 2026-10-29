package com.davidpedroza.tunes.core.data.repository

import androidx.paging.PagingData
import com.davidpedroza.tunes.core.model.Song
import kotlinx.coroutines.flow.Flow

interface SongsRepository {

    /**
     * Paged songs matching [query], offline-first: results are served from the local cache,
     * which is refreshed from the network while the user scrolls. A blank query yields no items.
     */
    fun searchSongs(query: String): Flow<PagingData<Song>>

    /** The cached song with [id], if any. */
    fun observeSong(id: Long): Flow<Song?>
}
