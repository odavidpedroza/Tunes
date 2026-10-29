package com.davidpedroza.tunes.core.database

import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davidpedroza.tunes.core.database.entity.SearchRemoteKeyEntity
import com.davidpedroza.tunes.core.database.entity.SearchResultEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchDaoTest : DatabaseTest() {

    @Test
    fun pagingSourceReturnsSongsOfTheQueryInPositionOrder() = runTest {
        db.songDao().upsertAll(listOf(song(1), song(2), song(3)))
        db.searchDao().insertResults(
            listOf(
                SearchResultEntity(query = "a", songId = 3, position = 0),
                SearchResultEntity(query = "a", songId = 1, position = 1),
                SearchResultEntity(query = "b", songId = 2, position = 0),
            ),
        )

        val page = db.searchDao().pagingSource("a")
            .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false))
            as PagingSource.LoadResult.Page

        assertEquals(listOf(3L, 1L), page.data.map { it.id })
    }

    @Test
    fun clearResultsOnlyAffectsTheGivenQuery() = runTest {
        db.songDao().upsertAll(listOf(song(1), song(2)))
        db.searchDao().insertResults(
            listOf(
                SearchResultEntity(query = "a", songId = 1, position = 0),
                SearchResultEntity(query = "b", songId = 2, position = 0),
            ),
        )

        db.searchDao().clearResults("a")

        assertEquals(0, db.searchDao().countResults("a"))
        assertEquals(1, db.searchDao().countResults("b"))
    }

    @Test
    fun remoteKeyRoundTrip() = runTest {
        val key = SearchRemoteKeyEntity(query = "a", nextPage = 2, endReached = false, lastUpdatedMillis = 42)

        db.searchDao().upsertRemoteKey(key)
        assertEquals(key, db.searchDao().getRemoteKey("a"))

        db.searchDao().clearRemoteKey("a")
        assertNull(db.searchDao().getRemoteKey("a"))
    }
}
