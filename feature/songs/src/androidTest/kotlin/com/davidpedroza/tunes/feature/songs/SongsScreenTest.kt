package com.davidpedroza.tunes.feature.songs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.davidpedroza.tunes.core.designsystem.R as DesignR
import androidx.paging.PagingData
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.designsystem.theme.TunesTheme
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.testing.TestData.song
import com.davidpedroza.tunes.core.testing.failedPagingDataOf
import com.davidpedroza.tunes.core.testing.pagingDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SongsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun setContent(
        uiState: SongsUiState,
        searchResults: Flow<PagingData<Song>> = flowOf(pagingDataOf(emptyList())),
        onSongClick: (List<Song>, Int) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            TunesTheme {
                SongsScreen(
                    uiState = uiState,
                    searchResults = searchResults,
                    onQueryChange = {},
                    onClearQuery = {},
                    onSongClick = onSongClick,
                    onViewAlbum = {},
                )
            }
        }
    }

    @Test
    fun recentlyPlayedSongsAreListedAndClickable() {
        var clicked: Pair<List<Song>, Int>? = null
        val songs = listOf(song(1), song(2))
        setContent(SongsUiState(recentlyPlayed = songs), onSongClick = { list, index -> clicked = list to index })

        composeRule.onNodeWithTag(SongsTestTags.RECENTLY_PLAYED_LIST).assertIsDisplayed()
        composeRule.onNodeWithText("Song 2").assertIsDisplayed()
        composeRule.onNodeWithText("Song 2").performClick()

        assertEquals(songs to 1, clicked)
    }

    @Test
    fun emptyHistoryShowsTheOnboardingState() {
        setContent(SongsUiState())

        composeRule.onNodeWithTag(SongsTestTags.EMPTY_STATE).assertIsDisplayed()
    }

    @Test
    fun searchResultsAreListed() {
        setContent(
            uiState = SongsUiState(query = "daft", activeSearch = "daft"),
            searchResults = flowOf(pagingDataOf(song(10), song(11))),
        )

        composeRule.onNodeWithTag(SongsTestTags.RESULTS_LIST).assertIsDisplayed()
        composeRule.onNodeWithText("Song 10").assertIsDisplayed()
        composeRule.onNodeWithText("Song 11").assertIsDisplayed()
    }

    @Test
    fun failedSearchWithoutCacheShowsErrorWithRetry() {
        setContent(
            uiState = SongsUiState(query = "daft", activeSearch = "daft"),
            searchResults = flowOf(failedPagingDataOf(TunesException.NoConnection())),
        )

        composeRule.onNodeWithTag(SongsTestTags.ERROR_STATE).assertIsDisplayed()
        composeRule.onNodeWithText(string(DesignR.string.ds_retry)).assertIsDisplayed()
    }

    @Test
    fun failedSearchWithCacheShowsResultsAndOfflineBanner() {
        setContent(
            uiState = SongsUiState(query = "daft", activeSearch = "daft"),
            searchResults = flowOf(failedPagingDataOf(TunesException.NoConnection(), listOf(song(10)))),
        )

        composeRule.onNodeWithTag(SongsTestTags.OFFLINE_BANNER).assertIsDisplayed()
        composeRule.onNodeWithText("Song 10").assertIsDisplayed()
    }

    @Test
    fun searchWithoutMatchesShowsNoResults() {
        setContent(
            uiState = SongsUiState(query = "zzz", activeSearch = "zzz"),
            searchResults = flowOf(pagingDataOf(emptyList())),
        )

        composeRule.onNodeWithTag(SongsTestTags.EMPTY_RESULTS).assertIsDisplayed()
    }
}
