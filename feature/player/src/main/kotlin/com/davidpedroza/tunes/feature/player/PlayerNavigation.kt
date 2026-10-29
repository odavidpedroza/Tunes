package com.davidpedroza.tunes.feature.player

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object PlayerRoute

fun NavGraphBuilder.playerScreen(
    onBack: () -> Unit,
    onViewAlbum: (albumId: Long) -> Unit,
) {
    composable<PlayerRoute> {
        PlayerRoute(onBack = onBack, onViewAlbum = onViewAlbum)
    }
}
