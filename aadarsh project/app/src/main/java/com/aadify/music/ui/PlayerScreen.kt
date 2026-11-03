package com.aadify.music.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronDown
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aadify.music.R
import com.aadify.music.data.Song
import com.aadify.music.ui.theme.DeepCharcoal
import com.aadify.music.ui.theme.MutedGray
import com.aadify.music.ui.theme.PrimaryWhite
import com.aadify.music.ui.theme.PureBlack
import com.aadify.music.ui.theme.SpotifyVibrantGreen
import com.aadify.music.ui.theme.SurfaceDarkCard

@Composable
fun PlayerScreen(
    song: Song?,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    val currentTime = positionMs.coerceIn(0L, safeDuration)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(DeepCharcoal, PureBlack)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Icons.Filled.ChevronDown,
                    contentDescription = "Collapse player",
                    tint = PrimaryWhite
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        AsyncImage(
            model = song?.albumArtUri?.let { Uri.parse(it) } ?: R.drawable.ic_music_note,
            contentDescription = song?.title ?: "Album art",
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .shadow(24.dp, RoundedCornerShape(16.dp)),
            placeholder = painterResource(id = R.drawable.ic_music_note),
            error = painterResource(id = R.drawable.ic_music_note)
        )

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = song?.title ?: "AADIFY",
            color = PrimaryWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song?.artist ?: "Ready to play",
            color = MutedGray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        Slider(
            value = currentTime.toFloat(),
            onValueChange = { value -> onSeek(value.toLong()) },
            valueRange = 0f..safeDuration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = PrimaryWhite,
                activeTrackColor = SpotifyVibrantGreen,
                inactiveTrackColor = SurfaceDarkCard
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentTime),
                color = MutedGray,
                fontSize = 12.sp
            )
            Text(
                text = formatTime(safeDuration),
                color = MutedGray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = PrimaryWhite
                )
            }

            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Filled.FastRewind,
                    contentDescription = "Previous",
                    tint = PrimaryWhite
                )
            }

            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(color = PrimaryWhite, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onTogglePlayPause, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = PureBlack,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Filled.FastForward,
                    contentDescription = "Next",
                    tint = PrimaryWhite
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    tint = PrimaryWhite
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .width(44.dp)
                .height(4.dp)
                .background(color = SurfaceDarkCard, shape = RoundedCornerShape(50))
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
