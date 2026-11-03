package com.aadify.music.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val database: AppDatabase
) {
    val favoritesFlow: Flow<List<Song>> = database.songDao().getAllFavorites().map { list ->
        list.map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                albumArtUri = entity.albumArtUri,
                durationMs = 0L,
                uri = entity.uri
            )
        }
    }

    val recentFlow: Flow<List<Song>> = database.songDao().getAllRecent().map { list ->
        list.map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                albumArtUri = entity.albumArtUri,
                durationMs = 0L,
                uri = entity.uri
            )
        }
    }

    fun getDeviceSongs(context: Context): List<Song> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val songs = mutableListOf<Song>()
        val resolver = context.contentResolver

        resolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)

                val albumArtUri = "content://media/external/audio/albumart/$albumId"
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()

                songs += Song(
                    id = id,
                    title = title,
                    artist = artist,
                    albumArtUri = albumArtUri,
                    durationMs = duration,
                    uri = uri
                )
            }
        }

        return songs
    }

    suspend fun toggleFavorite(song: Song) {
        val existing = database.songDao().getFavoriteById(song.id)
        if (existing == null) {
            database.songDao().insertFavorite(
                FavoriteEntity(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    albumArtUri = song.albumArtUri,
                    uri = song.uri
                )
            )
        } else {
            database.songDao().deleteFavoriteById(song.id)
        }
    }

    suspend fun addRecent(song: Song) {
        database.songDao().insertRecent(
            RecentEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                albumArtUri = song.albumArtUri,
                uri = song.uri,
                playedAt = System.currentTimeMillis()
            )
        )
    }

    companion object {
        fun create(context: Context): MusicRepository {
            return MusicRepository(AppDatabase.getInstance(context))
        }
    }
}
