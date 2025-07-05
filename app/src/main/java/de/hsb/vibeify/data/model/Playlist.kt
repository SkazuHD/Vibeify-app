package de.hsb.vibeify.data.model

data class Playlist(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    var songIds: List<String> = emptyList()
)
