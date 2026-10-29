package com.davidpedroza.tunes.core.data.repository

import com.davidpedroza.tunes.core.model.Song
import kotlinx.coroutines.flow.Flow

interface RecentlyPlayedRepository {

    /** Most recently played songs first. */
    fun observeRecentlyPlayed(limit: Int = DEFAULT_LIMIT): Flow<List<Song>>

    /** Records that [song] started playing now. */
    suspend fun markPlayed(song: Song)

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
