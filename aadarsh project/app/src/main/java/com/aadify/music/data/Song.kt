package com.aadify.music.data

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtUri: String?,
    val durationMs: Long,
    val uri: String
)
