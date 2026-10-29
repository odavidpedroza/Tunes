package com.davidpedroza.tunes.feature.album

import androidx.lifecycle.SavedStateHandle
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.testing.FakeAlbumRepository
import com.davidpedroza.tunes.core.testing.FakePlaybackController
import com.davidpedroza.tunes.core.testing.MainDispatcherRule
import com.davidpedroza.tunes.core.testing.TestData.albumDetails
import com.davidpedroza.tunes.core.testing.TestData.song
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AlbumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val albumRepository = FakeAlbumRepository()
    private val playbackController = FakePlaybackController()

    private fun viewModel(albumId: Long = 100L) = AlbumViewModel(
        savedStateHandle = SavedStateHandle(mapOf("albumId" to albumId)),
        albumRepository = albumRepository,
        playbackController = playbackController,
    )

    @Test
    fun `shows the album once the refresh succeeds`() = runTest {
        albumRepository.remoteAlbums[100L] = albumDetails(100L)
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumUiState.Content
        assertEquals("Album 100", state.details.album.title)
        assertEquals(listOf(1L, 2L), state.details.tracks.map { it.id })
        assertNull(state.refreshError)
        assertEquals(1, albumRepository.refreshCalls)
    }

    @Test
    fun `reports not found when the catalog has no album`() = runTest {
        val viewModel = viewModel(albumId = 42L)
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(AlbumUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `reports an error when nothing is cached and the network fails`() = runTest {
        albumRepository.refreshFailure = TunesException.NoConnection()
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumUiState.Error
        assertTrue(state.throwable is TunesException.NoConnection)
    }

    @Test
    fun `keeps showing cached content when a refresh fails`() = runTest {
        albumRepository.albums.value = mapOf(100L to albumDetails(100L))
        albumRepository.refreshFailure = TunesException.Server(500)
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value as AlbumUiState.Content
        assertEquals("Album 100", state.details.album.title)
        assertTrue(state.refreshError is TunesException.Server)
    }

    @Test
    fun `retry refreshes again`() = runTest {
        albumRepository.refreshFailure = TunesException.NoConnection()
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        albumRepository.refreshFailure = null
        albumRepository.remoteAlbums[100L] = albumDetails(100L)
        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AlbumUiState.Content)
        assertEquals(2, albumRepository.refreshCalls)
    }

    @Test
    fun `selecting a track plays the album from that track`() = runTest {
        val tracks = listOf(song(1), song(2), song(3))
        val viewModel = viewModel()

        viewModel.onTrackSelected(tracks, 1)

        assertEquals(listOf("play(1)"), playbackController.calls)
        assertEquals(2L, playbackController.state.value.currentSong?.id)
    }
}
