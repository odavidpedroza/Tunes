package com.davidpedroza.tunes.core.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.davidpedroza.tunes.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** [PlaybackController] backed by Media3 ExoPlayer, streaming the store's 30-second previews. */
@Singleton
internal class ExoPlayerPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
) : PlaybackController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var positionTicker: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { it.addListener(listener) }
    }

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            _state.update { current ->
                current.copy(
                    currentIndex = player.currentMediaItemIndex.takeIf { player.mediaItemCount > 0 } ?: -1,
                    isPlaying = player.isPlaying,
                    isBuffering = player.playbackState == Player.STATE_BUFFERING,
                    positionMillis = player.currentPosition.coerceAtLeast(0L),
                    durationMillis = player.duration.takeIf { it != C.TIME_UNSET }?.coerceAtLeast(0L)
                        ?: current.currentSongDuration(),
                    repeatMode = player.repeatMode.toRepeatMode(),
                    error = if (events.contains(Player.EVENT_PLAYER_ERROR)) current.error else current.error.takeUnless {
                        events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) && it is PlaybackError.Playback
                    },
                )
            }
            if (events.containsAny(Player.EVENT_IS_PLAYING_CHANGED, Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                updateTicker()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.update { it.copy(error = PlaybackError.Playback(error.localizedMessage), isPlaying = false) }
        }
    }

    override fun play(songs: List<Song>, startIndex: Int) {
        val prepared = PlaybackQueue.prepare(songs, startIndex)
        if (prepared == null) {
            // Still expose the selected song so the player screen can show it with an explanation.
            _state.update {
                PlaybackState(
                    queue = songs,
                    currentIndex = startIndex,
                    repeatMode = it.repeatMode,
                    durationMillis = songs.getOrNull(startIndex)?.durationMillis ?: 0L,
                    error = PlaybackError.PreviewUnavailable,
                )
            }
            player.stop()
            player.clearMediaItems()
            return
        }

        _state.update {
            PlaybackState(
                queue = prepared.songs,
                currentIndex = prepared.startIndex,
                isBuffering = true,
                repeatMode = it.repeatMode,
                durationMillis = prepared.songs[prepared.startIndex].durationMillis ?: 0L,
            )
        }
        player.setMediaItems(prepared.songs.map { it.toMediaItem() }, prepared.startIndex, C.TIME_UNSET)
        player.prepare()
        player.playWhenReady = true
    }

    override fun togglePlayPause() {
        if (_state.value.error == PlaybackError.PreviewUnavailable) return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
            player.play()
        }
    }

    override fun seekTo(positionMillis: Long) {
        if (player.mediaItemCount == 0) return
        player.seekTo(positionMillis.coerceIn(0L, player.duration.takeIf { it != C.TIME_UNSET } ?: positionMillis))
        _state.update { it.copy(positionMillis = positionMillis) }
    }

    override fun skipToNext() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    override fun skipToPrevious() {
        // Like most players: restart the song unless we are at its very beginning.
        if (player.currentPosition > RESTART_THRESHOLD_MS || !player.hasPreviousMediaItem()) {
            player.seekTo(0)
        } else {
            player.seekToPreviousMediaItem()
        }
    }

    override fun toggleRepeatMode() {
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _state.update { it.copy(repeatMode = player.repeatMode.toRepeatMode()) }
    }

    override fun release() {
        positionTicker?.cancel()
        player.release()
    }

    /** Polls the position while playing; ExoPlayer has no position callback. */
    private fun updateTicker() {
        positionTicker?.cancel()
        if (!player.isPlaying) return
        positionTicker = scope.launch {
            while (isActive) {
                _state.update { it.copy(positionMillis = player.currentPosition.coerceAtLeast(0L)) }
                delay(POSITION_TICK_MS)
            }
        }
    }

    private fun PlaybackState.currentSongDuration(): Long = currentSong?.durationMillis ?: 0L

    private fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(previewUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(albumTitle)
                .build(),
        )
        .build()

    private fun @receiver:Player.RepeatMode Int.toRepeatMode(): RepeatMode = when (this) {
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        else -> RepeatMode.OFF
    }

    private companion object {
        const val POSITION_TICK_MS = 250L
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}
