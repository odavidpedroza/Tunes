package com.davidpedroza.tunes.core.data.repository

import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.data.testing.FakeAlbumDao
import com.davidpedroza.tunes.core.data.testing.FakeSongDao
import com.davidpedroza.tunes.core.data.testing.FakeSongsRemoteDataSource
import com.davidpedroza.tunes.core.data.testing.FakeTimeProvider
import com.davidpedroza.tunes.core.data.testing.FakeTransactionRunner
import com.davidpedroza.tunes.core.data.testing.album
import com.davidpedroza.tunes.core.data.testing.song
import com.davidpedroza.tunes.core.model.AlbumDetails
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class OfflineFirstAlbumRepositoryTest {

    private val remote = FakeSongsRemoteDataSource()
    private val songDao = FakeSongDao()
    private val albumDao = FakeAlbumDao(songDao)
    private val transaction = FakeTransactionRunner()
    private val repository = OfflineFirstAlbumRepository(remote, albumDao, songDao, transaction, FakeTimeProvider())

    @Test
    fun `observeAlbum is null until the album is cached`() = runTest {
        assertNull(repository.observeAlbum(100).first())
    }

    @Test
    fun `refreshAlbum caches the album and its tracks in order`() = runTest {
        remote.albums[100] = AlbumDetails(album(100), listOf(song(7), song(3)))

        val found = repository.refreshAlbum(100)

        assertTrue(found)
        val details = requireNotNull(repository.observeAlbum(100).first())
        assertEquals("Album 100", details.album.title)
        assertEquals(listOf(7L, 3L), details.tracks.map { it.id })
        assertEquals(1, transaction.transactions)
    }

    @Test
    fun `refreshAlbum replaces a previously cached track list`() = runTest {
        remote.albums[100] = AlbumDetails(album(100), listOf(song(1), song(2)))
        repository.refreshAlbum(100)
        remote.albums[100] = AlbumDetails(album(100), listOf(song(2)))

        repository.refreshAlbum(100)

        assertEquals(listOf(2L), repository.observeAlbum(100).first()!!.tracks.map { it.id })
    }

    @Test
    fun `refreshAlbum returns false for an unknown album`() = runTest {
        assertFalse(repository.refreshAlbum(42))
        assertNull(repository.observeAlbum(42).first())
    }

    @Test
    fun `refreshAlbum propagates network failures`() = runTest {
        remote.failure = TunesException.Server(500)

        try {
            repository.refreshAlbum(100)
            fail("expected TunesException")
        } catch (e: TunesException.Server) {
            assertEquals(500, e.code)
        }
    }
}
