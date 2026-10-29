package com.davidpedroza.tunes.core.data.repository

import com.davidpedroza.tunes.core.common.TimeProvider
import com.davidpedroza.tunes.core.data.local.TransactionRunner
import com.davidpedroza.tunes.core.data.mapper.toEntity
import com.davidpedroza.tunes.core.data.mapper.toModel
import com.davidpedroza.tunes.core.database.dao.AlbumDao
import com.davidpedroza.tunes.core.database.dao.SongDao
import com.davidpedroza.tunes.core.database.entity.AlbumTrackEntity
import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.network.SongsRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OfflineFirstAlbumRepository @Inject constructor(
    private val remote: SongsRemoteDataSource,
    private val albumDao: AlbumDao,
    private val songDao: SongDao,
    private val transaction: TransactionRunner,
    private val time: TimeProvider,
) : AlbumRepository {

    override fun observeAlbum(albumId: Long): Flow<AlbumDetails?> =
        combine(albumDao.observeAlbum(albumId), albumDao.observeTracks(albumId)) { album, tracks ->
            album?.let { AlbumDetails(album = it.toModel(), tracks = tracks.map { track -> track.toModel() }) }
        }

    override suspend fun refreshAlbum(albumId: Long): Boolean {
        val details = remote.getAlbum(albumId) ?: return false
        transaction {
            albumDao.upsertAlbum(details.album.toEntity(updatedAtMillis = time.nowMillis()))
            songDao.upsertAll(details.tracks.map { it.toEntity() })
            albumDao.clearTracks(albumId)
            albumDao.insertTracks(
                details.tracks.mapIndexed { index, song -> AlbumTrackEntity(albumId = albumId, songId = song.id, position = index) },
            )
        }
        return true
    }
}
