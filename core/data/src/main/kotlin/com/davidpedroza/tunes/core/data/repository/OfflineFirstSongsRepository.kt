package com.davidpedroza.tunes.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.davidpedroza.tunes.core.common.TimeProvider
import com.davidpedroza.tunes.core.data.mapper.toModel
import com.davidpedroza.tunes.core.data.paging.SearchLocalDataSource
import com.davidpedroza.tunes.core.data.paging.SearchPaging
import com.davidpedroza.tunes.core.data.paging.SearchRemoteMediator
import com.davidpedroza.tunes.core.database.dao.SearchDao
import com.davidpedroza.tunes.core.database.dao.SongDao
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.network.SongsRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OfflineFirstSongsRepository @Inject constructor(
    private val remote: SongsRemoteDataSource,
    private val searchLocal: SearchLocalDataSource,
    private val searchDao: SearchDao,
    private val songDao: SongDao,
    private val time: TimeProvider,
) : SongsRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun searchSongs(query: String): Flow<PagingData<Song>> {
        val term = SearchQuery.normalize(query)
        if (term.isEmpty()) return flowOf(PagingData.empty())

        return Pager(
            config = PagingConfig(
                pageSize = SearchPaging.PAGE_SIZE,
                // Must match pageSize: the mediator emulates offsets from a constant page size.
                initialLoadSize = SearchPaging.PAGE_SIZE,
                prefetchDistance = SearchPaging.PAGE_SIZE / 3,
                enablePlaceholders = false,
            ),
            remoteMediator = SearchRemoteMediator(term, remote, searchLocal, time),
            pagingSourceFactory = { searchDao.pagingSource(term) },
        ).flow.map { pagingData -> pagingData.map { it.toModel() } }
    }

    override fun observeSong(id: Long): Flow<Song?> = songDao.observeSong(id).map { it?.toModel() }
}
