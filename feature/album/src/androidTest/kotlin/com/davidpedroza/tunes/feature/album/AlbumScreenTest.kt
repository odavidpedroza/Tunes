package com.davidpedroza.tunes.feature.album

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.davidpedroza.tunes.core.designsystem.R as DesignR
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.designsystem.theme.TunesTheme
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.testing.TestData.albumDetails
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AlbumScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun setContent(
        uiState: AlbumUiState,
        onRefresh: () -> Unit = {},
        onTrackClick: (List<Song>, Int) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            TunesTheme {
                AlbumScreen(uiState = uiState, onBack = {}, onRefresh = onRefresh, onTrackClick = onTrackClick)
            }
        }
    }

    @Test
    fun loadingStateIsShown() {
        setContent(AlbumUiState.Loading)

        composeRule.onNodeWithTag(AlbumTestTags.LOADING).assertIsDisplayed()
    }

    @Test
    fun contentShowsAlbumHeaderAndTracks() {
        var clicked: Int? = null
        val details = albumDetails(100L)
        setContent(
            AlbumUiState.Content(details = details, isRefreshing = false, refreshError = null, nowPlayingId = null),
            onTrackClick = { _, index -> clicked = index },
        )

        composeRule.onNodeWithText("Album Artist").assertIsDisplayed()
        composeRule.onNodeWithText("2013 · Pop · " + string(R.string.album_tracks_count, 2)).assertIsDisplayed()
        composeRule.onNodeWithText("Song 2").assertIsDisplayed().performClick()

        assertEquals(1, clicked)
    }

    @Test
    fun errorStateOffersRetry() {
        var retries = 0
        setContent(AlbumUiState.Error(TunesException.NoConnection()), onRefresh = { retries++ })

        composeRule.onNodeWithTag(AlbumTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithText(string(DesignR.string.ds_retry)).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun notFoundStateIsShown() {
        setContent(AlbumUiState.NotFound)

        composeRule.onNodeWithTag(AlbumTestTags.NOT_FOUND).assertIsDisplayed()
    }

    @Test
    fun cachedContentWithFailedRefreshShowsBanner() {
        setContent(
            AlbumUiState.Content(
                details = albumDetails(100L),
                isRefreshing = false,
                refreshError = TunesException.NoConnection(),
                nowPlayingId = null,
            ),
        )

        composeRule.onNodeWithTag(AlbumTestTags.OFFLINE_BANNER).assertIsDisplayed()
        composeRule.onNodeWithText("Song 1").assertIsDisplayed()
    }
}
