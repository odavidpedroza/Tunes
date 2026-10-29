package com.davidpedroza.tunes.core.network.itunes

import com.davidpedroza.tunes.core.common.TunesException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class ItunesSongsRemoteDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: ItunesSongsRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false }
        val service: ItunesService = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
        dataSource = ItunesSongsRemoteDataSource(service, ItunesConfig(country = "BR"))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `searchSongs sends the term, store front and emulated limit`() = runTest {
        server.enqueue(jsonResponse(searchBody(songs = 0)))

        dataSource.searchSongs(term = "daft punk", page = 1, pageSize = 25)

        val request = server.takeRequest()
        val url = requireNotNull(request.requestUrl)
        assertEquals("/search", url.encodedPath)
        assertEquals("daft punk", url.queryParameter("term"))
        assertEquals("BR", url.queryParameter("country"))
        assertEquals("song", url.queryParameter("entity"))
        assertEquals("50", url.queryParameter("limit"))
    }

    @Test
    fun `searchSongs maps songs and drops results that are not songs`() = runTest {
        server.enqueue(jsonResponse(searchBody(songs = 2, extra = listOf(musicVideoJson(id = 99)))))

        val page = dataSource.searchSongs(term = "x", page = 0, pageSize = 25)

        assertEquals(listOf(1L, 2L), page.songs.map { it.id })
        val song = page.songs.first()
        assertEquals("Song 1", song.title)
        assertEquals("Artist 1", song.artist)
        assertEquals(1000L, song.albumId)
        assertEquals("Album 1", song.albumTitle)
        assertEquals("https://example.com/1/100x100bb.jpg", song.artworkUrl)
        assertEquals("https://example.com/1/preview.m4a", song.previewUrl)
        assertEquals(200_000L, song.durationMillis)
        assertTrue(page.endReached)
    }

    @Test
    fun `searchSongs returns only the items of the requested page`() = runTest {
        server.enqueue(jsonResponse(searchBody(songs = 50)))

        val page = dataSource.searchSongs(term = "x", page = 1, pageSize = 25)

        assertEquals((26L..50L).toList(), page.songs.map { it.id })
        assertEquals(false, page.endReached)
    }

    @Test
    fun `searchSongs with an empty response reaches the end`() = runTest {
        server.enqueue(jsonResponse("""{"resultCount":0,"results":[]}"""))

        val page = dataSource.searchSongs(term = "nothing", page = 0, pageSize = 25)

        assertTrue(page.songs.isEmpty())
        assertTrue(page.endReached)
    }

    @Test
    fun `http errors become Server exceptions`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))

        try {
            dataSource.searchSongs(term = "x", page = 0, pageSize = 25)
            fail("expected TunesException.Server")
        } catch (e: TunesException.Server) {
            assertEquals(503, e.code)
        }
    }

    @Test
    fun `connection failures become NoConnection exceptions`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        try {
            dataSource.searchSongs(term = "x", page = 0, pageSize = 25)
            fail("expected TunesException.NoConnection")
        } catch (e: TunesException.NoConnection) {
            assertNotNull(e.cause)
        }
    }

    @Test
    fun `getAlbum maps the collection and its songs sorted by track number`() = runTest {
        server.enqueue(jsonResponse(resource("lookup_album.json")))

        val details = requireNotNull(dataSource.getAlbum(617154241))

        val request = server.takeRequest()
        assertEquals("/lookup", request.requestUrl?.encodedPath)
        assertEquals("617154241", request.requestUrl?.queryParameter("id"))
        assertEquals("song", request.requestUrl?.queryParameter("entity"))

        assertEquals("Random Access Memories", details.album.title)
        assertEquals("Daft Punk", details.album.artist)
        assertEquals(14, details.album.trackCount)
        assertEquals(2013, details.album.releaseYear)
        assertEquals("Pop", details.album.genre)
        // Music video (trackId 999) excluded, songs ordered 1 then 8.
        assertEquals(listOf(617154242L, 617154366L), details.tracks.map { it.id })
    }

    @Test
    fun `getAlbum returns null when the store does not know the id`() = runTest {
        server.enqueue(jsonResponse("""{"resultCount":0,"results":[]}"""))

        assertNull(dataSource.getAlbum(42))
    }


    private fun jsonResponse(body: String) = MockResponse()
        .setHeader("Content-Type", "text/javascript; charset=utf-8") // what itunes.apple.com really sends
        .setBody(body)

    private fun searchBody(songs: Int, extra: List<String> = emptyList()): String {
        val items = (1..songs).map { songJson(it.toLong()) } + extra
        return """{"resultCount":${items.size},"results":[${items.joinToString(",")}]}"""
    }

    private fun songJson(id: Long) = """
        {"wrapperType":"track","kind":"song","trackId":$id,"collectionId":1000,"artistName":"Artist $id",
         "trackName":"Song $id","collectionName":"Album $id","artworkUrl100":"https://example.com/$id/100x100bb.jpg",
         "previewUrl":"https://example.com/$id/preview.m4a","trackTimeMillis":200000,"trackNumber":1,"primaryGenreName":"Pop"}
    """.trimIndent()

    private fun musicVideoJson(id: Long) = """
        {"wrapperType":"track","kind":"music-video","trackId":$id,"artistName":"Artist","trackName":"Video $id"}
    """.trimIndent()

    private fun resource(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "missing test resource $name" }
            .bufferedReader().use { it.readText() }

}
