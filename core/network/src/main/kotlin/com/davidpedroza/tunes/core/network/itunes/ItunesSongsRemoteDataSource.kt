package com.davidpedroza.tunes.core.network.itunes

import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.network.SongsPage
import com.davidpedroza.tunes.core.network.SongsRemoteDataSource
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/** [SongsRemoteDataSource] backed by the public iTunes Search API. */
@Singleton
internal class ItunesSongsRemoteDataSource @Inject constructor(
    private val service: ItunesService,
    private val config: ItunesConfig,
) : SongsRemoteDataSource {

    override suspend fun searchSongs(term: String, page: Int, pageSize: Int): SongsPage = safeCall {
        val response = service.search(
            term = term,
            limit = ItunesPagination.limitFor(page, pageSize),
            country = config.country,
        )
        // Slice the raw results first: the API's limit counts every result type, not only songs.
        val raw = ItunesPagination.pageOf(response.results, page, pageSize)
        SongsPage(
            songs = raw.items.mapNotNull { it.toSongOrNull() },
            endReached = raw.endReached,
        )
    }

    override suspend fun getAlbum(albumId: Long): AlbumDetails? = safeCall {
        val results = service.lookup(id = albumId, country = config.country).results
        val album = results.firstNotNullOfOrNull { it.toAlbumOrNull() } ?: return@safeCall null
        val tracks = results
            .mapNotNull { it.toSongOrNull() }
            .sortedBy { it.trackNumber ?: Int.MAX_VALUE }
        AlbumDetails(album = album, tracks = tracks)
    }

    /** Runs [block] translating transport failures into [TunesException]s. */
    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: TunesException) {
        throw e
    } catch (e: HttpException) {
        throw TunesException.Server(e.code(), e)
    } catch (e: IOException) {
        throw TunesException.NoConnection(e)
    } catch (e: SerializationException) {
        throw TunesException.Unknown(e)
    } catch (e: Exception) {
        throw TunesException.Unknown(e)
    }
}
