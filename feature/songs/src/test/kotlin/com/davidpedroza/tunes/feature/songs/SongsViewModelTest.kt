package com.davidpedroza.tunes.feature.songs

import androidx.lifecycle.SavedStateHandle
import androidx.paging.testing.asSnapshot
import com.davidpedroza.tunes.core.testing.FakePlaybackController
import com.davidpedroza.tunes.core.testing.FakeRecentlyPlayedRepository
import com.davidpedroza.tunes.core.testing.FakeSongsRepository
import com.davidpedroza.tunes.core.testing.MainDispatcherRule
import com.davidpedroza.tunes.core.testing.TestData.song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val songsRepository = FakeSongsRepository()
    private val recentlyPlayedRepository = FakeRecentlyPlayedRepository()
    private val playbackController = FakePlaybackController()

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = SongsViewModel(
        songsRepository = songsRepository,
        recentlyPlayedRepository = recentlyPlayedRepository,
        playbackController = playbackController,
        savedStateHandle = savedStateHandle,
    )

    @Test
    fun `home shows recently played songs when the query is blank`() = runTest {
        recentlyPlayedRepository.recentlyPlayed.value = listOf(song(1), song(2))
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearching)
        assertEquals(listOf(1L, 2L), state.recentlyPlayed.map { it.id })
    }

    @Test
    fun `query text updates immediately but the search only starts after the debounce`() = runTest {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.onQueryChange("Daft ")
        advanceTimeBy(100)
        assertEquals("Daft ", viewModel.uiState.value.query)
        assertEquals("", viewModel.uiState.value.activeSearch)

        advanceTimeBy(SongsViewModel.SEARCH_DEBOUNCE.inWholeMilliseconds + 1)
        assertEquals("daft", viewModel.uiState.value.activeSearch)
        assertTrue(viewModel.uiState.value.isSearching)
    }

    @Test
    fun `search results come from the repository for the normalized query`() = runTest {
        songsRepository.results["daft punk"] = listOf(song(1), song(2))
        val viewModel = viewModel(SavedStateHandle(mapOf("query" to "  Daft   PUNK ")))

        val items = viewModel.searchResults.asSnapshot()

        assertEquals(listOf(1L, 2L), items.map { it.id })
        assertEquals(listOf("daft punk"), songsRepository.queries)
    }

    @Test
    fun `clearing the query goes back to the recently played list`() = runTest {
        val viewModel = viewModel(SavedStateHandle(mapOf("query" to "prince")))
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSearching)

        viewModel.onClearQuery()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.isSearching)
    }

    @Test
    fun `selecting a song starts playback of the list at that index`() = runTest {
        val viewModel = viewModel()
        val songs = listOf(song(1), song(2), song(3))

        viewModel.onSongSelected(songs, 2)

        assertEquals(listOf("play(2)"), playbackController.calls)
        assertEquals(3L, playbackController.state.value.currentSong?.id)
    }

    @Test
    fun `the now playing song is exposed to highlight it in lists`() = runTest {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        playbackController.play(listOf(song(1), song(2)), 1)
        advanceUntilIdle()

        assertEquals(2L, viewModel.uiState.value.nowPlayingId)
    }
}
