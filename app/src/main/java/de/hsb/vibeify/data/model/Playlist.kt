package de.hsb.vibeify.data.model

data class Playlist(
    val id: String,
    val userId : String = "TODO()",
    val title: String,
    val description: String?,
    val imagePath: Int?,
    var songs: List<Song>
)
