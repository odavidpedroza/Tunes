package com.davidpedroza.tunes.core.data.paging

import com.davidpedroza.tunes.core.common.TimeProvider
import com.davidpedroza.tunes.core.data.local.TransactionRunner
import com.davidpedroza.tunes.core.data.mapper.toEntity
import com.davidpedroza.tunes.core.database.dao.SearchDao
import com.davidpedroza.tunes.core.database.dao.SongDao
import com.davidpedroza.tunes.core.database.entity.SearchRemoteKeyEntity
import com.davidpedroza.tunes.core.database.entity.SearchResultEntity
import com.davidpedroza.tunes.core.model.Song
import javax.inject.Inject

/** Persistence operations the search [SearchRemoteMediator] needs, isolated so it can be tested with a fake. */
internal interface SearchLocalDataSource {

    suspend fun getRemoteKey(query: String): SearchRemoteKeyEntity?

    /**
     * Atomically stores one page of results for [query]: when [clearExisting] is set the previous
     * results are dropped first; [songs] are appended after the ones already cached and the paging
     * key is updated with [nextPage]/[endReached].
     */
    suspend fun storePage(
        query: String,
        songs: List<Song>,
        nextPage: Int,
        endReached: Boolean,
        clearExisting: Boolean,
    )
}

internal class RoomSearchLocalDataSource @Inject constructor(
    private val transaction: TransactionRunner,
    private val searchDao: SearchDao,
    private val songDao: SongDao,
    private val time: TimeProvider,
) : SearchLocalDataSource {

    override suspend fun getRemoteKey(query: String): SearchRemoteKeyEntity? = searchDao.getRemoteKey(query)

    override suspend fun storePage(
        query: String,
        songs: List<Song>,
        nextPage: Int,
        endReached: Boolean,
        clearExisting: Boolean,
    ) = transaction {
        // Freshness is measured from the last refresh, not from the last appended page.
        val lastUpdatedMillis = if (clearExisting) time.nowMillis() else searchDao.getRemoteKey(query)?.lastUpdatedMillis ?: time.nowMillis()
        if (clearExisting) {
            searchDao.clearResults(query)
            searchDao.clearRemoteKey(query)
        }
        val offset = searchDao.countResults(query)
        songDao.upsertAll(songs.map { it.toEntity() })
        // Duplicates (the API's order is not perfectly stable between calls) are ignored by the DAO.
        searchDao.insertResults(
            songs.mapIndexed { index, song -> SearchResultEntity(query = query, songId = song.id, position = offset + index) },
        )
        searchDao.upsertRemoteKey(
            SearchRemoteKeyEntity(
                query = query,
                nextPage = nextPage,
                endReached = endReached,
                lastUpdatedMillis = lastUpdatedMillis,
            ),
        )
    }
}
