package com.davidpedroza.tunes.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = TunesColors.OnSurface,
    onPrimary = TunesColors.Black,
    primaryContainer = TunesColors.SurfaceContainerHigh,
    onPrimaryContainer = TunesColors.OnSurface,
    secondary = TunesColors.Accent,
    onSecondary = TunesColors.Black,
    background = TunesColors.Black,
    onBackground = TunesColors.OnSurface,
    surface = TunesColors.Black,
    onSurface = TunesColors.OnSurface,
    surfaceVariant = TunesColors.SurfaceContainer,
    onSurfaceVariant = TunesColors.OnSurfaceVariant,
    surfaceContainer = TunesColors.SurfaceContainer,
    surfaceContainerLow = TunesColors.SurfaceContainer,
    surfaceContainerHigh = TunesColors.SurfaceContainerHigh,
    surfaceContainerHighest = TunesColors.SurfaceContainerHighest,
    outline = TunesColors.Outline,
    outlineVariant = TunesColors.SurfaceContainerHigh,
    error = TunesColors.Error,
    onError = TunesColors.Black,
)

val TunesShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** The app is dark by design, regardless of the system setting. */
@Composable
fun TunesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = TunesTypography,
        shapes = TunesShapes,
        content = content,
    )
}
