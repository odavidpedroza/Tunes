package com.davidpedroza.tunes.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.davidpedroza.tunes.feature.album.AlbumRoute
import com.davidpedroza.tunes.feature.album.albumScreen
import com.davidpedroza.tunes.feature.player.PlayerRoute
import com.davidpedroza.tunes.feature.player.playerScreen
import com.davidpedroza.tunes.feature.songs.SongsRoute
import com.davidpedroza.tunes.feature.songs.songsScreen
import com.davidpedroza.tunes.splash.SplashRoute
import com.davidpedroza.tunes.splash.splashScreen

@Composable
fun TunesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300)) },
    ) {
        splashScreen(
            onFinished = {
                navController.navigate(SongsRoute) {
                    popUpTo<SplashRoute> { inclusive = true }
                }
            },
        )
        songsScreen(
            onSongSelected = { navController.navigate(PlayerRoute) { launchSingleTop = true } },
            onViewAlbum = { albumId -> navController.navigate(AlbumRoute(albumId)) { launchSingleTop = true } },
        )
        playerScreen(
            onBack = navController::popBackStack,
            onViewAlbum = { albumId -> navController.navigate(AlbumRoute(albumId)) { launchSingleTop = true } },
        )
        albumScreen(
            onBack = navController::popBackStack,
            onSongSelected = { navController.navigate(PlayerRoute) { launchSingleTop = true } },
        )
    }
}
