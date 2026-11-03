package com.davidpedroza.tunes.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidpedroza.tunes.core.designsystem.component.Artwork
import com.davidpedroza.tunes.core.designsystem.component.EmptyState
import com.davidpedroza.tunes.core.designsystem.component.OptionsBottomSheet
import com.davidpedroza.tunes.core.designsystem.component.SheetOption
import com.davidpedroza.tunes.core.model.Artwork as ArtworkUrls
import com.davidpedroza.tunes.core.model.Song
import com.davidpedroza.tunes.core.player.PlaybackError
import com.davidpedroza.tunes.core.player.PlaybackState
import com.davidpedroza.tunes.core.player.RepeatMode

@Composable
fun PlayerRoute(
    onBack: () -> Unit,
    onViewAlbum: (albumId: Long) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(
        state = state,
        onBack = onBack,
        onViewAlbum = onViewAlbum,
        onPlayPause = viewModel::onPlayPause,
        onSeekTo = viewModel::onSeekTo,
        onSkipNext = viewModel::onSkipNext,
        onSkipPrevious = viewModel::onSkipPrevious,
        onToggleRepeat = viewModel::onToggleRepeat,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    state: PlaybackState,
    onBack: () -> Unit,
    onViewAlbum: (albumId: Long) -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong
    var showOptions by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.player_back))
                    }
                },
                actions = {
                    if (song?.albumId != null) {
                        IconButton(onClick = { showOptions = true }, modifier = Modifier.testTag(PlayerTestTags.MORE)) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.player_more_options))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        if (song == null) {
            EmptyState(
                icon = Icons.Outlined.MusicOff,
                title = stringResource(R.string.player_nothing_playing_title),
                message = stringResource(R.string.player_nothing_playing_message),
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            PlayerContent(
                song = song,
                state = state,
                onPlayPause = onPlayPause,
                onSeekTo = onSeekTo,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onToggleRepeat = onToggleRepeat,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }

    if (showOptions && song != null) {
        OptionsBottomSheet(
            title = song.title,
            subtitle = song.artist,
            onDismiss = { showOptions = false },
            options = listOfNotNull(
                song.albumId?.let { albumId ->
                    SheetOption(Icons.Default.Album, stringResource(R.string.player_view_album)) {
                        showOptions = false
                        onViewAlbum(albumId)
                    }
                },
            ),
        )
    }
}

@Composable
private fun PlayerContent(
    song: Song,
    state: PlaybackState,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        // Short viewports (landscape phones) cannot fit artwork + controls: shrink the artwork and
        // let the content scroll instead of clipping it.
        val compact = maxHeight < 520.dp
        val artworkSize = (maxWidth - 64.dp).coerceAtMost(maxHeight * if (compact) 0.3f else 0.42f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (compact) Modifier.padding(vertical = 16.dp) else Modifier.weight(1f)),
                contentAlignment = Alignment.Center,
            ) {
                Artwork(
                    url = ArtworkUrls.sized(song.artworkUrl, ArtworkUrls.LARGE),
                    contentDescription = null,
                    size = artworkSize,
                    shape = MaterialTheme.shapes.large,
                )
            }

            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(PlayerTestTags.TITLE),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(16.dp))
            state.error?.let { error ->
                Text(
                    text = when (error) {
                        PlaybackError.PreviewUnavailable -> stringResource(R.string.player_preview_unavailable)
                        is PlaybackError.Playback -> stringResource(R.string.player_playback_error)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(PlayerTestTags.ERROR),
                )
                Spacer(Modifier.height(8.dp))
            }

            Timeline(
                positionMillis = state.positionMillis,
                durationMillis = state.durationMillis,
                // Seeking only makes sense once the engine reported the preview length.
                enabled = state.error != PlaybackError.PreviewUnavailable && state.durationMillis > 0,
                onSeekTo = onSeekTo,
            )

            Spacer(Modifier.height(8.dp))
            Controls(
                state = state,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onToggleRepeat = onToggleRepeat,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.player_preview_notice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Timeline(
    positionMillis: Long,
    durationMillis: Long,
    enabled: Boolean,
    onSeekTo: (Long) -> Unit,
) {
    // While the user drags we show the drag position instead of the live one.
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    val duration = durationMillis.coerceAtLeast(1L)
    val shownPosition = dragPosition?.toLong() ?: positionMillis.coerceIn(0L, duration)
    val positionLabel = formatDuration(shownPosition)
    val remainingLabel = "-" + formatDuration(duration - shownPosition)
    val seekLabel = stringResource(R.string.player_seek)
    val stateLabel = stringResource(R.string.player_position_of, positionLabel, formatDuration(duration))
    val interactionSource = remember { MutableInteractionSource() }

    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = dragPosition ?: positionMillis.coerceIn(0L, duration).toFloat(),
            onValueChange = { dragPosition = it },
            onValueChangeFinished = {
                dragPosition?.let { onSeekTo(it.toLong()) }
                dragPosition = null
            },
            valueRange = 0f..duration.toFloat(),
            enabled = enabled,
            interactionSource = interactionSource,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = MaterialTheme.colorScheme.onSurface,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledActiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            thumb = {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant),
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    enabled = enabled,
                    modifier = Modifier.height(4.dp),
                    thumbTrackGapSize = 0.dp,
                    trackInsideCornerSize = 0.dp,
                    drawStopIndicator = null,
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledActiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PlayerTestTags.SLIDER)
                .semantics {
                    contentDescription = seekLabel
                    stateDescription = stateLabel
                },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                positionLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(PlayerTestTags.POSITION),
            )
            Text(remainingLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Controls(
    state: PlaybackState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
) {
    val canPlay = state.error != PlaybackError.PreviewUnavailable
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = onPlayPause,
                enabled = canPlay,
                modifier = Modifier
                    .size(64.dp)
                    .testTag(PlayerTestTags.PLAY_PAUSE),
            ) {
                if (state.isBuffering && !state.isPlaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (state.isPlaying) R.string.player_pause else R.string.player_play),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        IconButton(
            onClick = onSkipPrevious,
            enabled = state.hasPrevious && canPlay,
            modifier = Modifier
                .size(48.dp)
                .testTag(PlayerTestTags.PREVIOUS),
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = stringResource(R.string.player_skip_previous),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp),
            )
        }
        IconButton(
            onClick = onSkipNext,
            enabled = state.hasNext && canPlay,
            modifier = Modifier
                .size(48.dp)
                .testTag(PlayerTestTags.NEXT),
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.player_skip_next),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onToggleRepeat, modifier = Modifier.size(48.dp)) {
            val (icon, label) = when (state.repeatMode) {
                RepeatMode.OFF -> Icons.Default.Repeat to R.string.player_repeat_off
                RepeatMode.ALL -> Icons.Default.Repeat to R.string.player_repeat_all
                RepeatMode.ONE -> Icons.Default.RepeatOne to R.string.player_repeat_one
            }
            Icon(
                imageVector = icon,
                contentDescription = stringResource(label),
                tint = if (state.repeatMode == RepeatMode.OFF) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

object PlayerTestTags {
    const val TITLE = "player:title"
    const val SLIDER = "player:slider"
    const val POSITION = "player:position"
    const val PLAY_PAUSE = "player:playPause"
    const val PREVIOUS = "player:previous"
    const val NEXT = "player:next"
    const val MORE = "player:more"
    const val ERROR = "player:error"
}
