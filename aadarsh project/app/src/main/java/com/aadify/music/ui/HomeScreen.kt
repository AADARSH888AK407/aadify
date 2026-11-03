package com.aadify.music.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aadify.music.R
import com.aadify.music.data.Song
import com.aadify.music.ui.theme.MutedGray
import com.aadify.music.ui.theme.PrimaryWhite
import com.aadify.music.ui.theme.PureBlack
import com.aadify.music.ui.theme.SpotifyVibrantGreen
import com.aadify.music.ui.theme.SurfaceDarkCard

@Composable
fun HomeScreen(
    userName: String,
    recentSongs: List<Song>,
    allSongs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val filteredSongs = if (searchText.isBlank()) {
        allSongs
    } else {
        allSongs.filter { song ->
            song.title.contains(searchText, ignoreCase = true) ||
                song.artist.contains(searchText, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hi There,",
                            color = SpotifyVibrantGreen,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userName,
                            color = PrimaryWhite,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    textStyle = TextStyle(color = PrimaryWhite, fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(SurfaceDarkCard, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Search songs",
                                color = MutedGray,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }

            if (recentSongs.isNotEmpty()) {
                item {
                    Text(
                        text = "Recently Played",
                        color = PrimaryWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(recentSongs.take(5)) { song ->
                    SongRow(
                        song = song,
                        onClick = { onSongClick(song, allSongs) }
                    )
                }
            }

            item {
                Text(
                    text = "All Songs",
                    color = PrimaryWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(filteredSongs) { song ->
                SongRow(
                    song = song,
                    onClick = { onSongClick(song, allSongs) }
                )
            }
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.albumArtUri?.let { Uri.parse(it) } ?: R.drawable.ic_music_note,
            contentDescription = song.title,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp)),
            placeholder = painterResource(id = R.drawable.ic_music_note),
            error = painterResource(id = R.drawable.ic_music_note)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = PrimaryWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = song.artist,
                color = MutedGray,
                fontSize = 14.sp
            )
        }
    }
}
