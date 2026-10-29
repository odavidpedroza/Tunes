package com.davidpedroza.tunes.feature.album

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidpedroza.tunes.core.designsystem.component.Artwork
import com.davidpedroza.tunes.core.designsystem.component.EmptyState
import com.davidpedroza.tunes.core.designsystem.component.ErrorState
import com.davidpedroza.tunes.core.designsystem.component.LoadingState
import com.davidpedroza.tunes.core.designsystem.component.OfflineBanner
import com.davidpedroza.tunes.core.designsystem.component.SongListItem
import com.davidpedroza.tunes.core.designsystem.component.toUserMessage
import com.davidpedroza.tunes.core.model.Album
import com.davidpedroza.tunes.core.model.AlbumDetails
import com.davidpedroza.tunes.core.model.Artwork as ArtworkUrls
import com.davidpedroza.tunes.core.model.Song

@Composable
fun AlbumRoute(
    onBack: () -> Unit,
    onSongSelected: () -> Unit,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlbumScreen(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onTrackClick = { tracks, index ->
            viewModel.onTrackSelected(tracks, index)
            onSongSelected()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    uiState: AlbumUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onTrackClick: (tracks: List<Song>, index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = (uiState as? AlbumUiState.Content)?.details?.album?.title ?: stringResource(R.string.album_title_fallback)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.album_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        when (uiState) {
            AlbumUiState.Loading -> LoadingState(Modifier.padding(innerPadding).testTag(AlbumTestTags.LOADING))

            AlbumUiState.NotFound -> EmptyState(
                icon = Icons.Outlined.Album,
                title = stringResource(R.string.album_not_found_title),
                message = stringResource(R.string.album_not_found_message),
                modifier = Modifier.padding(innerPadding).testTag(AlbumTestTags.NOT_FOUND),
            )

            is AlbumUiState.Error -> ErrorState(
                message = uiState.throwable.toUserMessage(),
                onRetry = onRefresh,
                modifier = Modifier.padding(innerPadding).testTag(AlbumTestTags.ERROR),
            )

            is AlbumUiState.Content -> PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(Modifier.fillMaxSize()) {
                    uiState.refreshError?.let { error ->
                        OfflineBanner(message = error.toUserMessage(), onRetry = onRefresh, modifier = Modifier.testTag(AlbumTestTags.OFFLINE_BANNER))
                    }
                    AlbumContent(
                        details = uiState.details,
                        nowPlayingId = uiState.nowPlayingId,
                        onTrackClick = onTrackClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumContent(
    details: AlbumDetails,
    nowPlayingId: Long?,
    onTrackClick: (List<Song>, Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(AlbumTestTags.TRACK_LIST),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item(key = "header") { AlbumHeader(details.album, details.tracks.size) }
        items(details.tracks, key = { it.id }) { track ->
            SongListItem(
                title = track.title,
                artist = track.artist,
                artworkUrl = ArtworkUrls.sized(track.artworkUrl, ArtworkUrls.THUMBNAIL),
                isActive = track.id == nowPlayingId,
                onClick = { onTrackClick(details.tracks, details.tracks.indexOf(track)) },
            )
        }
    }
}

@Composable
private fun AlbumHeader(album: Album, trackCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Artwork(
            url = ArtworkUrls.sized(album.artworkUrl, ArtworkUrls.LARGE),
            contentDescription = null,
            size = 140.dp,
            shape = MaterialTheme.shapes.medium,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val meta = listOfNotNull(
            album.releaseYear?.toString(),
            album.genre,
            stringResource(R.string.album_tracks_count, trackCount),
        ).joinToString(" · ")
        Spacer(Modifier.height(4.dp))
        Text(
            text = meta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

object AlbumTestTags {
    const val LOADING = "album:loading"
    const val NOT_FOUND = "album:notFound"
    const val ERROR = "album:error"
    const val TRACK_LIST = "album:tracks"
    const val OFFLINE_BANNER = "album:offlineBanner"
}
