package de.hsb.vibeify.data.model

data class Playlist(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val imageUrl: String? = "https://vibeify-app.skazu.net/cover/playlist/default",
    var songIds: List<String> = emptyList()
)
