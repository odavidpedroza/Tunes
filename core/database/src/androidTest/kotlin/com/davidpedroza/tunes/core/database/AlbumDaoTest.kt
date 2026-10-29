package com.davidpedroza.tunes.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.davidpedroza.tunes.core.database.entity.AlbumEntity
import com.davidpedroza.tunes.core.database.entity.AlbumTrackEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumDaoTest : DatabaseTest() {

    @Test
    fun tracksAreReturnedInPositionOrder() = runTest {
        db.songDao().upsertAll(listOf(song(1), song(2), song(3)))
        db.albumDao().upsertAlbum(
            AlbumEntity(100, "Album", "Artist", null, 3, 2020, "Pop", null, lastUpdatedMillis = 0),
        )
        db.albumDao().insertTracks(
            listOf(
                AlbumTrackEntity(albumId = 100, songId = 2, position = 0),
                AlbumTrackEntity(albumId = 100, songId = 3, position = 1),
                AlbumTrackEntity(albumId = 100, songId = 1, position = 2),
            ),
        )

        assertEquals("Album", db.albumDao().observeAlbum(100).first()?.title)
        assertEquals(listOf(2L, 3L, 1L), db.albumDao().observeTracks(100).first().map { it.id })
    }
}
