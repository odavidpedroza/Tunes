package com.davidpedroza.tunes.core.data.repository

import com.davidpedroza.tunes.core.common.TimeProvider
import com.davidpedroza.tunes.core.data.local.TransactionRunner
import com.davidpedroza.tunes.core.data.mapper.toEntity
import com.davidpedroza.tunes.core.data.mapper.toModel
import com.davidpedroza.tunes.core.database.dao.RecentlyPlayedDao
import com.davidpedroza.tunes.core.database.dao.SongDao
import com.davidpedroza.tunes.core.database.entity.RecentlyPlayedEntity
import com.davidpedroza.tunes.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OfflineFirstRecentlyPlayedRepository @Inject constructor(
    private val transaction: TransactionRunner,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val songDao: SongDao,
    private val time: TimeProvider,
) : RecentlyPlayedRepository {

    override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> =
        recentlyPlayedDao.observeRecentlyPlayed(limit).map { entities -> entities.map { it.toModel() } }

    override suspend fun markPlayed(song: Song) = transaction {
        // The song may come from a search that is no longer cached: make sure it exists locally.
        songDao.upsertAll(listOf(song.toEntity()))
        recentlyPlayedDao.upsert(RecentlyPlayedEntity(songId = song.id, playedAtMillis = time.nowMillis()))
        recentlyPlayedDao.trim(keep = MAX_HISTORY)
    }

    private companion object {
        /**
         * More than the home screen shows ([RecentlyPlayedRepository.DEFAULT_LIMIT]) so a longer
         * list can be offered later without losing history; small enough to stay a cheap join.
         */
        const val MAX_HISTORY = 50
    }
}
