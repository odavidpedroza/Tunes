package com.davidpedroza.tunes.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.data.testing.FakeSearchLocalDataSource
import com.davidpedroza.tunes.core.data.testing.FakeSongsRemoteDataSource
import com.davidpedroza.tunes.core.data.testing.FakeTimeProvider
import com.davidpedroza.tunes.core.data.testing.song
import com.davidpedroza.tunes.core.database.entity.SearchRemoteKeyEntity
import com.davidpedroza.tunes.core.database.entity.SongEntity
import com.davidpedroza.tunes.core.network.SongsPage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalPagingApi::class)
class SearchRemoteMediatorTest {

    private val remote = FakeSongsRemoteDataSource()
    private val local = FakeSearchLocalDataSource()
    private val time = FakeTimeProvider(now = 10_000_000L)
    private val mediator = SearchRemoteMediator(
        query = "daft punk",
        remote = remote,
        local = local,
        time = time,
        pageSize = 2,
        cacheTtl = 15.minutes,
    )
    private val state = PagingState<Int, SongEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 2),
        leadingPlaceholderCount = 0,
    )

    @Test
    fun `initialize refreshes when nothing is cached`() = runTest {
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `initialize skips the refresh while the cache is fresh`() = runTest {
        local.keys["daft punk"] = SearchRemoteKeyEntity("daft punk", nextPage = 1, endReached = false, lastUpdatedMillis = time.now - 1_000)

        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `initialize refreshes once the cache is stale`() = runTest {
        local.keys["daft punk"] = SearchRemoteKeyEntity("daft punk", 1, false, lastUpdatedMillis = time.now - 16.minutes.inWholeMilliseconds)

        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `refresh replaces cached results with the first page`() = runTest {
        local.results["daft punk"] = mutableListOf(song(99))
        remote.pages["daft punk" to 0] = SongsPage(listOf(song(1), song(2)), endReached = false)

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(1L, 2L), local.results["daft punk"]?.map { it.id })
        assertEquals(1, local.keys["daft punk"]?.nextPage)
        assertEquals(Triple("daft punk", 0, 2), remote.searchCalls.single())
    }

    @Test
    fun `append loads the page stored in the remote key`() = runTest {
        local.results["daft punk"] = mutableListOf(song(1), song(2))
        local.keys["daft punk"] = SearchRemoteKeyEntity("daft punk", nextPage = 1, endReached = false, lastUpdatedMillis = 0)
        remote.pages["daft punk" to 1] = SongsPage(listOf(song(3)), endReached = true)

        val result = mediator.load(LoadType.APPEND, state)

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(1L, 2L, 3L), local.results["daft punk"]?.map { it.id })
        assertEquals(2, local.keys["daft punk"]?.nextPage)
        assertTrue(local.keys["daft punk"]!!.endReached)
    }

    @Test
    fun `append stops without a network call once the end was reached`() = runTest {
        local.keys["daft punk"] = SearchRemoteKeyEntity("daft punk", nextPage = 3, endReached = true, lastUpdatedMillis = 0)

        val result = mediator.load(LoadType.APPEND, state)

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue(remote.searchCalls.isEmpty())
    }

    @Test
    fun `prepend is always the end of pagination`() = runTest {
        val result = mediator.load(LoadType.PREPEND, state)

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertTrue(remote.searchCalls.isEmpty())
    }

    @Test
    fun `network failures are reported and leave the cache untouched`() = runTest {
        local.results["daft punk"] = mutableListOf(song(1))
        remote.failure = TunesException.NoConnection()

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertTrue((result as RemoteMediator.MediatorResult.Error).throwable is TunesException.NoConnection)
        assertEquals(listOf(1L), local.results["daft punk"]?.map { it.id })
    }

    @Test
    fun `storage failures are reported instead of crashing the pager`() = runTest {
        remote.pages["daft punk" to 0] = SongsPage(listOf(song(1)), endReached = true)
        local.storeFailure = IllegalStateException("database is read-only")

        val result = mediator.load(LoadType.REFRESH, state)

        assertTrue((result as RemoteMediator.MediatorResult.Error).throwable is IllegalStateException)
    }

    @Test
    fun `cancellation is not swallowed as a load error`() = runTest {
        remote.pages["daft punk" to 0] = SongsPage(listOf(song(1)), endReached = true)
        local.storeFailure = CancellationException("collector left")

        val thrown = runCatching { mediator.load(LoadType.REFRESH, state) }.exceptionOrNull()

        assertTrue(thrown is CancellationException)
    }
}
