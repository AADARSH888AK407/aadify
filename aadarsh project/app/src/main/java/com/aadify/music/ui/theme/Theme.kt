package com.aadify.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PureBlack = Color(0xFF121212)
val DeepCharcoal = Color(0xFF181818)
val SurfaceDarkCard = Color(0xFF242424)
val SpotifyVibrantGreen = Color(0xFF1DB954)
val PrimaryWhite = Color(0xFFFFFFFF)
val MutedGray = Color(0xFFB3B3B3)

private val AadifyDarkColorScheme = darkColorScheme(
    primary = SpotifyVibrantGreen,
    onPrimary = PureBlack,
    primaryContainer = SpotifyVibrantGreen,
    onPrimaryContainer = PrimaryWhite,
    secondary = SpotifyVibrantGreen,
    onSecondary = PureBlack,
    surface = DeepCharcoal,
    onSurface = PrimaryWhite,
    background = PureBlack,
    onBackground = PrimaryWhite,
    error = Color(0xFFCF6679),
    onError = PrimaryWhite
)

@Composable
fun AadifyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AadifyDarkColorScheme,
        content = content
    )
}
