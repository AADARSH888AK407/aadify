package com.aadify.music.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aadify.music.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val song: Song?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val queue: List<Song>
)

class PlayerController {
    private val _state = MutableStateFlow(
        PlayerState(
            song = null,
            isPlaying = false,
            positionMs = 0L,
            durationMs = 0L,
            queue = emptyList()
        )
    )
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var contextRef: Context? = null
    private var controller: MediaController? = null
    private var handler: Handler? = null
    private var pollRunnable: Runnable? = null

    fun connect(context: Context) {
        if (controller != null) return

        contextRef = context.applicationContext
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlayerService::class.java)
        )

        controller = MediaController.Builder(context, sessionToken).build()
        updatePlaybackState()
        startPolling()
    }

    fun startService(context: Context) {
        val intent = Intent(context, MusicPlayerService::class.java)
        context.startForegroundService(intent)
    }

    fun playSong(song: Song, queue: List<Song>) {
        val appContext = contextRef ?: return
        startService(appContext)

        val service = MusicPlayerService.instance
        if (service != null) {
            service.playSong(song, queue)
            _state.value = _state.value.copy(
                song = song,
                queue = queue,
                durationMs = song.durationMs,
                isPlaying = true
            )
        } else {
            val mediaItems = queue.map { item ->
                MediaItem.Builder()
                    .setMediaId(item.id.toString())
                    .setUri(item.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(item.title)
                            .setArtist(item.artist)
                            .setArtworkUri(item.albumArtUri?.let { Uri.parse(it) })
                            .build()
                    )
                    .build()
            }
            val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            controller?.setMediaItems(mediaItems, startIndex, 0L)
            controller?.prepare()
            controller?.play()
        }
    }

    fun togglePlayPause() {
        if (controller?.isPlaying == true) controller?.pause() else controller?.play()
    }

    fun next() {
        controller?.seekToNext()
    }

    fun previous() {
        controller?.seekToPrevious()
    }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms)
    }

    private fun startPolling() {
        handler = Handler(Looper.getMainLooper())
        pollRunnable = object : Runnable {
            override fun run() {
                updatePlaybackState()
                handler?.postDelayed(this, 500L)
            }
        }
        handler?.postDelayed(pollRunnable!!, 500L)
    }

    private fun updatePlaybackState() {
        val mediaController = controller ?: return
        val currentItem = mediaController.currentMediaItem
        val metadata = currentItem?.mediaMetadata

        val song = if (currentItem != null) {
            Song(
                id = currentItem.mediaId.toLongOrNull() ?: -1L,
                title = metadata?.title?.toString() ?: "AADIFY",
                artist = metadata?.artist?.toString() ?: "Unknown Artist",
                albumArtUri = metadata?.artworkUri?.toString(),
                durationMs = mediaController.duration.coerceAtLeast(0L),
                uri = currentItem.localConfiguration?.uri?.toString() ?: ""
            )
        } else {
            null
        }

        _state.value = PlayerState(
            song = song,
            isPlaying = mediaController.isPlaying,
            positionMs = mediaController.currentPosition,
            durationMs = mediaController.duration.coerceAtLeast(0L),
            queue = _state.value.queue
        )
    }

    fun disconnect() {
        pollRunnable?.let { handler?.removeCallbacks(it) }
        pollRunnable = null
        handler = null
        controller?.release()
        controller = null
        contextRef = null
    }
}
