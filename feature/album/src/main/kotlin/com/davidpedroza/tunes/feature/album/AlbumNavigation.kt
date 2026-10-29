package com.davidpedroza.tunes.feature.album

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data class AlbumRoute(val albumId: Long)

fun NavGraphBuilder.albumScreen(
    onBack: () -> Unit,
    onSongSelected: () -> Unit,
) {
    composable<AlbumRoute> {
        AlbumRoute(onBack = onBack, onSongSelected = onSongSelected)
    }
}
