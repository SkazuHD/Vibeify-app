package de.hsb.vibeify.data.model

//Album data class representing an album in the application
data class Album(
    val id: String = "",
    val title: String = "",
    val artist: String? = null,
    val imageUrl: String? = null,
    val releaseDate: String? = null,
    var songIds: List<String> = emptyList()
)
