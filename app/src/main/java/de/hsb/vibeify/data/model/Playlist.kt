package de.hsb.vibeify.data.model

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val imageRes: Int,
    val songs: List<Song>
)
