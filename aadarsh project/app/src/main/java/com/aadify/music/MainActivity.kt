package com.aadify.music

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aadify.music.data.MusicRepository
import com.aadify.music.data.Song
import com.aadify.music.player.PlayerController
import com.aadify.music.ui.HomeScreen
import com.aadify.music.ui.MiniPlayer
import com.aadify.music.ui.PlayerScreen
import com.aadify.music.ui.SplashScreen
import com.aadify.music.ui.theme.AadifyTheme
import com.aadify.music.ui.theme.MutedGray
import com.aadify.music.ui.theme.PrimaryWhite
import com.aadify.music.ui.theme.SpotifyVibrantGreen
import com.aadify.music.ui.theme.SurfaceDarkCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AadifyTheme {
                AadifyApp()
            }
        }
    }
}

@Composable
fun AadifyApp() {
    val context = LocalContext.current.applicationContext
    val prefs = remember { context.getSharedPreferences("aadify_prefs", Context.MODE_PRIVATE) }
    val repository = remember { MusicRepository.create(context) }
    val playerController = remember { PlayerController() }

    val recentSongs by repository.recentFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val allSongs = remember { mutableStateOf<List<Song>>(emptyList()) }
    val playerState by playerController.state.collectAsStateWithLifecycle()

    var splashComplete by rememberSaveable { mutableStateOf(false) }
    var userName by rememberSaveable { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var showPlayer by rememberSaveable { mutableStateOf(false) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        playerController.connect(context)
        val songs = withContext(Dispatchers.IO) {
            repository.getDeviceSongs(context)
        }
        allSongs.value = songs
        if (!hasPermissions(context, permissions)) {
            permissionLauncher.launch(permissions)
        }
    }

    DisposableEffect(playerController) {
        onDispose { playerController.disconnect() }
    }

    when {
        !splashComplete -> {
            SplashScreen(onComplete = { splashComplete = true })
        }

        userName.isBlank() -> {
            OnboardingScreen(
                onContinue = { entered ->
                    val cleaned = entered.trim()
                    if (cleaned.isNotEmpty()) {
                        prefs.edit { putString("user_name", cleaned) }
                        userName = cleaned
                    }
                }
            )
        }

        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = com.aadify.music.ui.theme.PureBlack)
            ) {
                if (showPlayer && playerState.song != null) {
                    PlayerScreen(
                        song = playerState.song,
                        positionMs = playerState.positionMs,
                        durationMs = playerState.durationMs,
                        isPlaying = playerState.isPlaying,
                        onCollapse = { showPlayer = false },
                        onTogglePlayPause = { playerController.togglePlayPause() },
                        onPrevious = { playerController.previous() },
                        onNext = { playerController.next() },
                        onSeek = { ms -> playerController.seekTo(ms) }
                    )
                } else {
                    HomeScreen(
                        userName = userName,
                        recentSongs = recentSongs,
                        allSongs = allSongs.value,
                        onSongClick = { song, queue ->
                            showPlayer = true
                            playerController.playSong(song, queue)
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                repository.addRecent(song)
                            }
                        }
                    )

                    if (playerState.song != null) {
                        MiniPlayer(
                            song = playerState.song,
                            isPlaying = playerState.isPlaying,
                            onExpand = { showPlayer = true },
                            onTogglePlayPause = { playerController.togglePlayPause() },
                            onNext = { playerController.next() },
                            onLikeToggle = {
                                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                    repository.toggleFavorite(playerState.song)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
    return permissions.all { permission ->
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun OnboardingScreen(onContinue: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Welcome to AADIFY",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "What should we call you?",
                color = MutedGray,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Your name", color = MutedGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotifyVibrantGreen,
                    unfocusedBorderColor = SurfaceDarkCard,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = SpotifyVibrantGreen
                )
            )

            Button(
                onClick = { if (name.isNotBlank()) onContinue(name) },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotifyVibrantGreen,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
