package com.davidpedroza.tunes.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davidpedroza.tunes.core.database.entity.RecentlyPlayedEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentlyPlayedDaoTest : DatabaseTest() {

    @Test
    fun observeRecentlyPlayedIsOrderedByMostRecentFirst() = runTest {
        db.songDao().upsertAll(listOf(song(1), song(2), song(3)))
        db.recentlyPlayedDao().upsert(RecentlyPlayedEntity(songId = 1, playedAtMillis = 10))
        db.recentlyPlayedDao().upsert(RecentlyPlayedEntity(songId = 2, playedAtMillis = 30))
        db.recentlyPlayedDao().upsert(RecentlyPlayedEntity(songId = 3, playedAtMillis = 20))

        val songs = db.recentlyPlayedDao().observeRecentlyPlayed(limit = 10).first()

        assertEquals(listOf(2L, 3L, 1L), songs.map { it.id })
    }

    @Test
    fun replayingASongMovesItToTheTop() = runTest {
        db.songDao().upsertAll(listOf(song(1), song(2)))
        db.recentlyPlayedDao().upsert(RecentlyPlayedEntity(songId = 1, playedAtMillis = 10))
        db.recentlyPlayedDao().upsert(RecentlyPlayedEntity(songId = 2, playedAtMillis = 20))

        db.recentlyPlayedDao().upsert(RecentlyPlayedEntity(songId = 1, playedAtMillis = 30))

        val songs = db.recentlyPlayedDao().observeRecentlyPlayed(limit = 10).first()
        assertEquals(listOf(1L, 2L), songs.map { it.id })
    }

    @Test
    fun trimKeepsOnlyTheMostRecentEntries() = runTest {
        db.songDao().upsertAll((1L..5L).map { song(it) })
        (1L..5L).forEach { db.recentlyPlayedDao().upsert(RecentlyPlayedEntity(songId = it, playedAtMillis = it)) }

        db.recentlyPlayedDao().trim(keep = 2)

        val songs = db.recentlyPlayedDao().observeRecentlyPlayed(limit = 10).first()
        assertEquals(listOf(5L, 4L), songs.map { it.id })
    }
}
