package com.davidpedroza.tunes.core.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.player.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
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
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PlaybackController] that drives the engine living in [PlaybackService] through a Media3
 * [MediaController]. The controller is itself a [Player], so state mapping is the same as it would be
 * against ExoPlayer directly, while the system gets a proper media session (notification, lock screen,
 * headset buttons, background playback).
 *
 * Connecting to the service is asynchronous; commands issued before the connection completes are
 * queued (in order) and replayed as soon as the controller is available.
 */
@Singleton
internal class SessionPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
) : PlaybackController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var positionTicker: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val pendingCommands = ArrayDeque<(Player) -> Unit>()

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
                updateTicker(player)
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
            withPlayer {
                stop()
                clearMediaItems()
            }
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
        withPlayer {
            setMediaItems(prepared.songs.map { it.toMediaItem() }, prepared.startIndex, C.TIME_UNSET)
            prepare()
            playWhenReady = true
        }
    }

    override fun togglePlayPause() {
        if (_state.value.error == PlaybackError.PreviewUnavailable) return
        withPlayer {
            if (isPlaying) {
                pause()
            } else {
                if (playbackState == Player.STATE_IDLE) prepare()
                if (playbackState == Player.STATE_ENDED) seekTo(0)
                play()
            }
        }
    }

    override fun seekTo(positionMillis: Long) {
        _state.update { it.copy(positionMillis = positionMillis.coerceAtLeast(0L)) }
        withPlayer {
            if (mediaItemCount == 0) return@withPlayer
            seekTo(positionMillis.coerceIn(0L, duration.takeIf { it != C.TIME_UNSET } ?: positionMillis))
        }
    }

    override fun skipToNext() = withPlayer {
        if (hasNextMediaItem()) seekToNextMediaItem()
    }

    override fun skipToPrevious() = withPlayer {
        // Like most players: restart the song unless we are at its very beginning.
        if (currentPosition > RESTART_THRESHOLD_MS || !hasPreviousMediaItem()) {
            seekTo(0)
        } else {
            seekToPreviousMediaItem()
        }
    }

    override fun toggleRepeatMode() = withPlayer {
        repeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _state.update { it.copy(repeatMode = repeatMode.toRepeatMode()) }
    }

    override fun release() {
        positionTicker?.cancel()
        pendingCommands.clear()
        controller?.removeListener(listener)
        controllerFuture?.let(MediaController::releaseFuture)
        controller = null
        controllerFuture = null
    }

    /** Runs [command] now if connected, otherwise once the controller connects (the service is started lazily). */
    private fun withPlayer(command: Player.() -> Unit) {
        controller?.let { command(it); return }
        pendingCommands.addLast(command)
        if (controllerFuture == null) connect()
    }

    private fun connect() {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                val connected = try {
                    future.get()
                } catch (e: ExecutionException) {
                    onConnectionFailed(e.cause ?: e)
                    return@addListener
                } catch (e: InterruptedException) {
                    onConnectionFailed(e)
                    return@addListener
                }
                controller = connected
                connected.addListener(listener)
                while (pendingCommands.isNotEmpty()) pendingCommands.removeFirst().invoke(connected)
            },
            MoreExecutors.directExecutor(), // buildAsync() completes on the main looper already
        )
    }

    private fun onConnectionFailed(cause: Throwable) {
        pendingCommands.clear()
        controllerFuture = null
        _state.update { it.copy(isBuffering = false, isPlaying = false, error = PlaybackError.Playback(cause.message)) }
    }

    /** Polls the position while playing; the Player API has no position callback. */
    private fun updateTicker(player: Player) {
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
