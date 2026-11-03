package com.davidpedroza.tunes.feature.songs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.davidpedroza.tunes.core.designsystem.component.EmptyState
import com.davidpedroza.tunes.core.designsystem.component.ErrorState
import com.davidpedroza.tunes.core.designsystem.component.LoadingMoreIndicator
import com.davidpedroza.tunes.core.designsystem.component.LoadingState
import com.davidpedroza.tunes.core.designsystem.component.OfflineBanner
import com.davidpedroza.tunes.core.designsystem.component.OptionsBottomSheet
import com.davidpedroza.tunes.core.designsystem.component.RetryRow
import com.davidpedroza.tunes.core.designsystem.component.SheetOption
import com.davidpedroza.tunes.core.designsystem.component.SongListItem
import com.davidpedroza.tunes.core.designsystem.component.toUserMessage
import com.davidpedroza.tunes.core.model.Artwork
import com.davidpedroza.tunes.core.model.Song
import kotlinx.coroutines.flow.Flow

@Composable
fun SongsRoute(
    onSongSelected: () -> Unit,
    onViewAlbum: (albumId: Long) -> Unit,
    viewModel: SongsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SongsScreen(
        uiState = uiState,
        searchResults = viewModel.searchResults,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::onClearQuery,
        onSongClick = { songs, index ->
            viewModel.onSongSelected(songs, index)
            onSongSelected()
        },
        onViewAlbum = onViewAlbum,
    )
}

@Composable
fun SongsScreen(
    uiState: SongsUiState,
    searchResults: Flow<PagingData<Song>>,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSongClick: (songs: List<Song>, index: Int) -> Unit,
    onViewAlbum: (albumId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyPagingItems = searchResults.collectAsLazyPagingItems()
    var optionsFor by remember { mutableStateOf<Song?>(null) }
    // The text field owns its text: routing every keystroke through the ViewModel's
    // asynchronous StateFlow drops/reorders characters when typing fast. The ViewModel
    // only receives updates (and keeps the query in SavedStateHandle for process death).
    var queryText by rememberSaveable { mutableStateOf(uiState.query) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Text(
                text = stringResource(R.string.songs_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
                    .semantics { heading() },
            )
            SearchField(
                query = queryText,
                onQueryChange = { value ->
                    queryText = value
                    onQueryChange(value)
                },
                onClear = {
                    queryText = ""
                    onClearQuery()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))

            if (uiState.isSearching) {
                SearchResults(
                    activeSearch = uiState.activeSearch,
                    items = lazyPagingItems,
                    nowPlayingId = uiState.nowPlayingId,
                    onSongClick = onSongClick,
                    onMoreClick = { optionsFor = it },
                )
            } else {
                RecentlyPlayed(
                    songs = uiState.recentlyPlayed,
                    nowPlayingId = uiState.nowPlayingId,
                    onSongClick = onSongClick,
                    onMoreClick = { optionsFor = it },
                )
            }
        }
    }

    optionsFor?.let { song ->
        OptionsBottomSheet(
            title = song.title,
            subtitle = song.artist,
            onDismiss = { optionsFor = null },
            options = buildList {
                song.albumId?.let { albumId ->
                    add(
                        SheetOption(Icons.Default.Album, stringResource(R.string.songs_view_album)) {
                            optionsFor = null
                            onViewAlbum(albumId)
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.testTag(SongsTestTags.SEARCH_FIELD),
        placeholder = { Text(stringResource(R.string.songs_search_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.songs_search_clear))
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
private fun RecentlyPlayed(
    songs: List<Song>,
    nowPlayingId: Long?,
    onSongClick: (List<Song>, Int) -> Unit,
    onMoreClick: (Song) -> Unit,
) {
    if (songs.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.LibraryMusic,
            title = stringResource(R.string.songs_empty_title),
            message = stringResource(R.string.songs_empty_message),
            modifier = Modifier.testTag(SongsTestTags.EMPTY_STATE),
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SongsTestTags.RECENTLY_PLAYED_LIST),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "header") {
            Text(
                text = stringResource(R.string.songs_recently_played),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .semantics { heading() },
            )
        }
        items(songs, key = { it.id }) { song ->
            SongListItem(
                title = song.title,
                artist = song.artist,
                artworkUrl = Artwork.sized(song.artworkUrl, Artwork.THUMBNAIL),
                isActive = song.id == nowPlayingId,
                onClick = { onSongClick(songs, songs.indexOf(song)) },
                // The sheet\'s only action is "View album": songs without one get no menu.
                onMoreClick = song.albumId?.let { { onMoreClick(song) } },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResults(
    activeSearch: String,
    items: LazyPagingItems<Song>,
    nowPlayingId: Long?,
    onSongClick: (List<Song>, Int) -> Unit,
    onMoreClick: (Song) -> Unit,
) {
    val refresh = items.loadState.refresh
    val isRefreshing = refresh is LoadState.Loading
    val hasItems = items.itemCount > 0

    PullToRefreshBox(
        isRefreshing = isRefreshing && hasItems,
        onRefresh = items::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            isRefreshing && !hasItems -> LoadingState(Modifier.testTag(SongsTestTags.LOADING))

            refresh is LoadState.Error && !hasItems -> ErrorState(
                message = refresh.error.toUserMessage(),
                onRetry = items::retry,
                modifier = Modifier.testTag(SongsTestTags.ERROR_STATE),
            )

            refresh is LoadState.NotLoading && !hasItems && items.loadState.append.endOfPaginationReached -> EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = stringResource(R.string.songs_no_results_title, activeSearch),
                message = stringResource(R.string.songs_no_results_message),
                modifier = Modifier.testTag(SongsTestTags.EMPTY_RESULTS),
            )

            else -> Column(Modifier.fillMaxSize()) {
                if (refresh is LoadState.Error) {
                    OfflineBanner(
                        message = refresh.error.toUserMessage(),
                        onRetry = items::retry,
                        modifier = Modifier.testTag(SongsTestTags.OFFLINE_BANNER),
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(SongsTestTags.RESULTS_LIST),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(
                        count = items.itemCount,
                        key = items.itemKey { it.id },
                    ) { index ->
                        val song = items[index] ?: return@items
                        SongListItem(
                            title = song.title,
                            artist = song.artist,
                            artworkUrl = Artwork.sized(song.artworkUrl, Artwork.THUMBNAIL),
                            isActive = song.id == nowPlayingId,
                            onClick = {
                                val loaded = items.itemSnapshotList.items
                                onSongClick(loaded, loaded.indexOfFirst { it.id == song.id })
                            },
                            // The sheet\'s only action is "View album": songs without one get no menu.
                            onMoreClick = song.albumId?.let { { onMoreClick(song) } },
                        )
                    }
                    when (val append = items.loadState.append) {
                        is LoadState.Loading -> item(key = "append-loading") { LoadingMoreIndicator() }
                        is LoadState.Error -> item(key = "append-error") {
                            RetryRow(message = append.error.toUserMessage(), onRetry = items::retry)
                        }
                        is LoadState.NotLoading -> Unit
                    }
                }
            }
        }
    }
}

/** Tags used by UI tests. */
object SongsTestTags {
    const val SEARCH_FIELD = "songs:search"
    const val RECENTLY_PLAYED_LIST = "songs:recentlyPlayed"
    const val RESULTS_LIST = "songs:results"
    const val EMPTY_STATE = "songs:empty"
    const val EMPTY_RESULTS = "songs:emptyResults"
    const val LOADING = "songs:loading"
    const val ERROR_STATE = "songs:error"
    const val OFFLINE_BANNER = "songs:offlineBanner"
}
