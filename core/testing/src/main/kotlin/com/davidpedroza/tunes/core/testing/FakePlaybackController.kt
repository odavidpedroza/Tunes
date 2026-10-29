package com.davidpedroza.tunes.core.testing

import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.player.PlaybackController
import com.davidpedroza.tunes.core.player.PlaybackState
import com.davidpedroza.tunes.core.player.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakePlaybackController : PlaybackController {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state

    val calls = mutableListOf<String>()

    fun setState(state: PlaybackState) {
        _state.value = state
    }

    override fun play(songs: List<Song>, startIndex: Int) {
        calls += "play($startIndex)"
        _state.update { PlaybackState(queue = songs, currentIndex = startIndex, isPlaying = true, repeatMode = it.repeatMode) }
    }

    override fun togglePlayPause() {
        calls += "togglePlayPause"
        _state.update { it.copy(isPlaying = !it.isPlaying) }
    }

    override fun seekTo(positionMillis: Long) {
        calls += "seekTo($positionMillis)"
        _state.update { it.copy(positionMillis = positionMillis) }
    }

    override fun skipToNext() {
        calls += "skipToNext"
        _state.update { if (it.currentIndex < it.queue.lastIndex) it.copy(currentIndex = it.currentIndex + 1) else it }
    }

    override fun skipToPrevious() {
        calls += "skipToPrevious"
        _state.update { if (it.currentIndex > 0) it.copy(currentIndex = it.currentIndex - 1) else it }
    }

    override fun toggleRepeatMode() {
        calls += "toggleRepeatMode"
        _state.update {
            it.copy(
                repeatMode = when (it.repeatMode) {
                    RepeatMode.OFF -> RepeatMode.ALL
                    RepeatMode.ALL -> RepeatMode.ONE
                    RepeatMode.ONE -> RepeatMode.OFF
                },
            )
        }
    }

    override fun release() {
        calls += "release"
    }
}
