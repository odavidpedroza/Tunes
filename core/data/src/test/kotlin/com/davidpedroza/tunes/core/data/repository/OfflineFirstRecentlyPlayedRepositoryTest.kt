package com.davidpedroza.tunes.core.data.repository

import com.davidpedroza.tunes.core.data.testing.FakeRecentlyPlayedDao
import com.davidpedroza.tunes.core.data.testing.FakeSongDao
import com.davidpedroza.tunes.core.data.testing.FakeTimeProvider
import com.davidpedroza.tunes.core.data.testing.song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineFirstRecentlyPlayedRepositoryTest {

    private val songDao = FakeSongDao()
    private val recentlyPlayedDao = FakeRecentlyPlayedDao(songDao)
    private val time = FakeTimeProvider(now = 1_000)
    private val repository = OfflineFirstRecentlyPlayedRepository(recentlyPlayedDao, songDao, time)

    @Test
    fun `markPlayed stores the song and lists it most recent first`() = runTest {
        repository.markPlayed(song(1))
        time.now = 2_000
        repository.markPlayed(song(2))

        val recent = repository.observeRecentlyPlayed().first()

        assertEquals(listOf(2L, 1L), recent.map { it.id })
        assertEquals("Song 2", recent.first().title)
    }

    @Test
    fun `playing a song again moves it to the top`() = runTest {
        repository.markPlayed(song(1))
        time.now = 2_000
        repository.markPlayed(song(2))
        time.now = 3_000
        repository.markPlayed(song(1))

        assertEquals(listOf(1L, 2L), repository.observeRecentlyPlayed().first().map { it.id })
    }

    @Test
    fun `observeRecentlyPlayed honours the limit`() = runTest {
        (1L..5L).forEach { id ->
            time.now = id
            repository.markPlayed(song(id))
        }

        assertEquals(listOf(5L, 4L), repository.observeRecentlyPlayed(limit = 2).first().map { it.id })
    }
}
