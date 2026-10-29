package com.davidpedroza.tunes.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.davidpedroza.tunes.R
import com.davidpedroza.tunes.core.designsystem.theme.TunesColors
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

fun NavGraphBuilder.splashScreen(onFinished: () -> Unit) {
    composable<SplashRoute> {
        SplashScreen(onFinished = onFinished)
    }
}

/**
 * Branded splash from the design: black canvas with a teal glow and the note glyph.
 * It continues the system splash (same colors) and hands over to the home screen shortly after.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit, durationMillis: Long = SPLASH_DURATION_MILLIS) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    var revealed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (revealed) 1f else 0.85f, tween(600), label = "splashScale")
    val alpha by animateFloatAsState(if (revealed) 1f else 0f, tween(600), label = "splashAlpha")

    LaunchedEffect(Unit) {
        revealed = true
        delay(durationMillis)
        currentOnFinished()
    }

    val appName = androidx.compose.ui.res.stringResource(R.string.app_name)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(TunesColors.Accent.copy(alpha = 0.35f), Color(0xFF07262B), TunesColors.Black),
                    center = Offset(0f, 0f),
                    radius = 1800f,
                ),
            )
            .semantics { contentDescription = appName },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            tint = Color(0xFFD9D9D9),
            modifier = Modifier
                .size(96.dp)
                .scale(scale)
                .alpha(alpha),
        )
    }
}

private const val SPLASH_DURATION_MILLIS = 1_100L
