package com.davidpedroza.tunes.core.data.testing

import com.davidpedroza.tunes.core.common.TimeProvider
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.data.local.TransactionRunner
import com.davidpedroza.tunes.core.data.paging.SearchLocalDataSource
import com.davidpedroza.tunes.core.database.dao.AlbumDao
import com.davidpedroza.tunes.core.database.dao.RecentlyPlayedDao
import com.davidpedroza.tunes.core.database.dao.SongDao
import com.davidpedroza.tunes.core.database.entity.AlbumEntity
import com.davidpedroza.tunes.core.database.entity.AlbumTrackEntity
import com.davidpedroza.tunes.core.database.entity.RecentlyPlayedEntity
import com.davidpedroza.tunes.core.database.entity.SearchRemoteKeyEntity
import com.davidpedroza.tunes.core.database.entity.SongEntity
import com.davidpedroza.tunes.core.model.Album
import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.network.SongsPage
import com.davidpedroza.tunes.core.network.SongsRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

fun song(id: Long, albumId: Long? = 100, previewUrl: String? = "https://preview/$id.m4a") = Song(
    id = id,
    title = "Song $id",
    artist = "Artist $id",
    albumId = albumId,
    albumTitle = "Album",
    artworkUrl = "https://art/$id/100x100bb.jpg",
    previewUrl = previewUrl,
    durationMillis = 180_000,
    trackNumber = id.toInt(),
    genre = "Pop",
)

fun album(id: Long = 100) = Album(
    id = id,
    title = "Album $id",
    artist = "Artist",
    artworkUrl = "https://art/$id/100x100bb.jpg",
    trackCount = 2,
    releaseYear = 2020,
    genre = "Pop",
    copyright = null,
)

class FakeTimeProvider(var now: Long = 1_000_000L) : TimeProvider {
    override fun nowMillis(): Long = now
}

class FakeSongsRemoteDataSource : SongsRemoteDataSource {
    /** (term, page) -> page to return. */
    val pages = mutableMapOf<Pair<String, Int>, SongsPage>()
    val albums = mutableMapOf<Long, AlbumDetails>()
    var failure: TunesException? = null
    val searchCalls = mutableListOf<Triple<String, Int, Int>>()

    override suspend fun searchSongs(term: String, page: Int, pageSize: Int): SongsPage {
        searchCalls += Triple(term, page, pageSize)
        failure?.let { throw it }
        return pages[term to page] ?: SongsPage(emptyList(), endReached = true)
    }

    override suspend fun getAlbum(albumId: Long): AlbumDetails? {
        failure?.let { throw it }
        return albums[albumId]
    }
}

class FakeSearchLocalDataSource : SearchLocalDataSource {
    val keys = mutableMapOf<String, SearchRemoteKeyEntity>()
    val results = mutableMapOf<String, MutableList<Song>>()
    var now: Long = 0

    override suspend fun getRemoteKey(query: String): SearchRemoteKeyEntity? = keys[query]

    override suspend fun storePage(query: String, songs: List<Song>, nextPage: Int, endReached: Boolean, clearExisting: Boolean) {
        if (clearExisting) results.remove(query)
        results.getOrPut(query) { mutableListOf() } += songs
        keys[query] = SearchRemoteKeyEntity(query, nextPage, endReached, now)
    }
}

class FakeTransactionRunner : TransactionRunner {
    var transactions = 0
    override suspend fun <R> invoke(block: suspend () -> R): R {
        transactions++
        return block()
    }
}

class FakeSongDao : SongDao {
    val songs = MutableStateFlow<Map<Long, SongEntity>>(emptyMap())

    override suspend fun upsertAll(songs: List<SongEntity>) {
        this.songs.value = this.songs.value + songs.associateBy { it.id }
    }

    override fun observeSong(id: Long): Flow<SongEntity?> = songs.map { it[id] }

    override suspend fun getSongs(ids: List<Long>): List<SongEntity> = ids.mapNotNull { songs.value[it] }
}

class FakeRecentlyPlayedDao(private val songDao: FakeSongDao) : RecentlyPlayedDao {
    val entries = MutableStateFlow<Map<Long, RecentlyPlayedEntity>>(emptyMap())

    override suspend fun upsert(entry: RecentlyPlayedEntity) {
        entries.value = entries.value + (entry.songId to entry)
    }

    override fun observeRecentlyPlayed(limit: Int): Flow<List<SongEntity>> = entries.map { map ->
        map.values.sortedByDescending { it.playedAtMillis }.take(limit).mapNotNull { songDao.songs.value[it.songId] }
    }

    override suspend fun trim(keep: Int) {
        entries.value = entries.value.values.sortedByDescending { it.playedAtMillis }.take(keep).associateBy { it.songId }
    }
}

class FakeAlbumDao(private val songDao: FakeSongDao) : AlbumDao {
    val albums = MutableStateFlow<Map<Long, AlbumEntity>>(emptyMap())
    val tracks = MutableStateFlow<List<AlbumTrackEntity>>(emptyList())

    override suspend fun upsertAlbum(album: AlbumEntity) {
        albums.value = albums.value + (album.id to album)
    }

    override suspend fun insertTracks(tracks: List<AlbumTrackEntity>) {
        this.tracks.value = this.tracks.value + tracks
    }

    override suspend fun clearTracks(albumId: Long) {
        tracks.value = tracks.value.filterNot { it.albumId == albumId }
    }

    override fun observeAlbum(albumId: Long): Flow<AlbumEntity?> = albums.map { it[albumId] }

    override fun observeTracks(albumId: Long): Flow<List<SongEntity>> = tracks.map { list ->
        list.filter { it.albumId == albumId }.sortedBy { it.position }.mapNotNull { songDao.songs.value[it.songId] }
    }
}
