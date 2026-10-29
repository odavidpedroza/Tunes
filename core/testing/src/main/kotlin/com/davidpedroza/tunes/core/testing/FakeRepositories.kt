package com.davidpedroza.tunes.core.testing

import androidx.paging.PagingData
import com.davidpedroza.tunes.core.data.repository.AlbumRepository
import com.davidpedroza.tunes.core.data.repository.RecentlyPlayedRepository
import com.davidpedroza.tunes.core.data.repository.SongsRepository
import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeSongsRepository : SongsRepository {
    /** Normalized query -> songs to return. */
    val results = mutableMapOf<String, List<Song>>()
    val queries = mutableListOf<String>()
    val songs = MutableStateFlow<Map<Long, Song>>(emptyMap())

    override fun searchSongs(query: String): Flow<PagingData<Song>> {
        if (query.isBlank()) return flowOf(pagingDataOf(emptyList()))
        queries += query
        return flowOf(pagingDataOf(results[query].orEmpty()))
    }

    override fun observeSong(id: Long): Flow<Song?> = songs.map { it[id] }
}

class FakeRecentlyPlayedRepository : RecentlyPlayedRepository {
    val recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val played = mutableListOf<Song>()

    override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> = recentlyPlayed.map { it.take(limit) }

    override suspend fun markPlayed(song: Song) {
        played += song
        recentlyPlayed.value = listOf(song) + recentlyPlayed.value.filterNot { it.id == song.id }
    }
}

class FakeAlbumRepository : AlbumRepository {
    val albums = MutableStateFlow<Map<Long, AlbumDetails>>(emptyMap())

    /** What the next [refreshAlbum] does: store [remoteAlbums] entry, return false, or throw. */
    val remoteAlbums = mutableMapOf<Long, AlbumDetails>()
    var refreshFailure: Throwable? = null
    var refreshCalls = 0

    override fun observeAlbum(albumId: Long): Flow<AlbumDetails?> = albums.map { it[albumId] }

    override suspend fun refreshAlbum(albumId: Long): Boolean {
        refreshCalls++
        refreshFailure?.let { throw it }
        val details = remoteAlbums[albumId] ?: return false
        albums.value = albums.value + (albumId to details)
        return true
    }
}
