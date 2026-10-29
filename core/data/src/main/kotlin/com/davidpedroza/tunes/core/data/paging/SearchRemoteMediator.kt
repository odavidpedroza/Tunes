package com.davidpedroza.tunes.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.davidpedroza.tunes.core.common.TimeProvider
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.database.entity.SongEntity
import com.davidpedroza.tunes.core.network.SongsRemoteDataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Keeps the local search cache for [query] in sync with the network as the user scrolls.
 *
 * Room is the single source of truth: the UI always renders the database, and this mediator only
 * writes to it. On failure it reports the error while the cached results stay visible (offline-first).
 */
@OptIn(ExperimentalPagingApi::class)
internal class SearchRemoteMediator(
    private val query: String,
    private val remote: SongsRemoteDataSource,
    private val local: SearchLocalDataSource,
    private val time: TimeProvider,
    private val pageSize: Int = SearchPaging.PAGE_SIZE,
    private val cacheTtl: Duration = SearchPaging.CACHE_TTL,
) : RemoteMediator<Int, SongEntity>() {

    override suspend fun initialize(): InitializeAction {
        val key = local.getRemoteKey(query) ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val isFresh = time.nowMillis() - key.lastUpdatedMillis < cacheTtl.inWholeMilliseconds
        return if (isFresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, SongEntity>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 0
            // Results are only ever appended; there is nothing "before" the first page.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val key = local.getRemoteKey(query) ?: return MediatorResult.Success(endOfPaginationReached = false)
                if (key.endReached) return MediatorResult.Success(endOfPaginationReached = true)
                key.nextPage
            }
        }

        return try {
            val result = remote.searchSongs(term = query, page = page, pageSize = pageSize)
            local.storePage(
                query = query,
                songs = result.songs,
                nextPage = page + 1,
                endReached = result.endReached,
                clearExisting = loadType == LoadType.REFRESH,
            )
            MediatorResult.Success(endOfPaginationReached = result.endReached)
        } catch (e: TunesException) {
            MediatorResult.Error(e)
        }
    }
}

object SearchPaging {
    /** Page size requested from the network and from the local paging source. */
    const val PAGE_SIZE = 25

    /** How long cached results are considered fresh enough to skip the automatic refresh. */
    val CACHE_TTL: Duration = 15.minutes
}
