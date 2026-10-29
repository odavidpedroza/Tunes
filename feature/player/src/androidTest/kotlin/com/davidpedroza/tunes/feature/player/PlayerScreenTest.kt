package com.davidpedroza.tunes.feature.player

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.davidpedroza.tunes.core.designsystem.theme.TunesTheme
import com.davidpedroza.tunes.core.player.PlaybackError
import com.davidpedroza.tunes.core.player.PlaybackState
import com.davidpedroza.tunes.core.testing.TestData.song
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlayerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun setContent(
        state: PlaybackState,
        onPlayPause: () -> Unit = {},
        onSkipNext: () -> Unit = {},
    ) {
        composeRule.setContent {
            TunesTheme {
                PlayerScreen(
                    state = state,
                    onBack = {},
                    onViewAlbum = {},
                    onPlayPause = onPlayPause,
                    onSeekTo = {},
                    onSkipNext = onSkipNext,
                    onSkipPrevious = {},
                    onToggleRepeat = {},
                )
            }
        }
    }

    @Test
    fun showsSongMetadataAndTimeline() {
        setContent(
            PlaybackState(
                queue = listOf(song(1), song(2)),
                currentIndex = 0,
                positionMillis = 86_000,
                durationMillis = 260_000,
            ),
        )

        composeRule.onNodeWithTag(PlayerTestTags.TITLE).assertIsDisplayed()
        composeRule.onNodeWithText("Song 1").assertIsDisplayed()
        composeRule.onNodeWithText("Artist 1").assertIsDisplayed()
        composeRule.onNodeWithText("1:26").assertIsDisplayed()
        composeRule.onNodeWithText("-2:54").assertIsDisplayed()
        composeRule.onNodeWithTag(PlayerTestTags.SLIDER).assertIsDisplayed()
    }

    @Test
    fun playButtonReflectsStateAndTogglesPlayback() {
        var toggles = 0
        setContent(PlaybackState(queue = listOf(song(1)), currentIndex = 0, isPlaying = false), onPlayPause = { toggles++ })

        composeRule.onNodeWithContentDescription(string(R.string.player_play)).assertIsDisplayed().performClick()

        assertEquals(1, toggles)
    }

    @Test
    fun pauseIsShownWhilePlaying() {
        setContent(PlaybackState(queue = listOf(song(1)), currentIndex = 0, isPlaying = true))

        composeRule.onNodeWithContentDescription(string(R.string.player_pause)).assertIsDisplayed()
    }

    @Test
    fun nextIsDisabledOnTheLastSong() {
        setContent(PlaybackState(queue = listOf(song(1), song(2)), currentIndex = 1))

        composeRule.onNodeWithTag(PlayerTestTags.NEXT).assertIsNotEnabled()
    }

    @Test
    fun missingPreviewIsExplainedAndPlaybackDisabled() {
        setContent(
            PlaybackState(
                queue = listOf(song(1, previewUrl = null)),
                currentIndex = 0,
                error = PlaybackError.PreviewUnavailable,
            ),
        )

        composeRule.onNodeWithTag(PlayerTestTags.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(PlayerTestTags.PLAY_PAUSE).assertIsNotEnabled()
    }

    @Test
    fun emptyQueueShowsNothingPlaying() {
        setContent(PlaybackState())

        composeRule.onNodeWithText(string(R.string.player_nothing_playing_title)).assertIsDisplayed()
    }
}
