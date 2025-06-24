package de.hsb.vibeify.data.model

data class Playlist(
    val id: String = "",
    val userId : String = "",
    val title: String = "",
    val description: String? = null,
    val imagePath: Int? = null,
    var songIds: List<String> = emptyList()
)
