package com.davidpedroza.tunes.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.davidpedroza.tunes.core.database.entity.SongEntity
import org.junit.After
import org.junit.Before

/** Base class giving each test a fresh in-memory [TunesDatabase]. */
abstract class DatabaseTest {

    protected lateinit var db: TunesDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TunesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    protected fun song(id: Long, title: String = "Song $id") = SongEntity(
        id = id,
        title = title,
        artist = "Artist $id",
        albumId = 100,
        albumTitle = "Album",
        artworkUrl = null,
        previewUrl = null,
        durationMillis = 1000,
        trackNumber = id.toInt(),
        genre = null,
    )
}
