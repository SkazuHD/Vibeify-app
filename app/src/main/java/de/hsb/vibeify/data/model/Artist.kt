package de.hsb.vibeify.data.model

// Artist data class representing an artist in the application

data class Artist(
    val id: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val songIds: List<String> = emptyList(),
    val albumIds: List<String> = emptyList()
)