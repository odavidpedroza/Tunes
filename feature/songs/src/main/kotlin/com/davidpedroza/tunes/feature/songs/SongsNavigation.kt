package com.davidpedroza.tunes.feature.songs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SongsRoute

fun NavGraphBuilder.songsScreen(
    onSongSelected: () -> Unit,
    onViewAlbum: (albumId: Long) -> Unit,
) {
    composable<SongsRoute> {
        SongsRoute(onSongSelected = onSongSelected, onViewAlbum = onViewAlbum)
    }
}
