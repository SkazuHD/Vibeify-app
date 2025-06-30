package de.hsb.vibeify.data.model

data class Genre(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val count: Int? = 0,
    val imageUrl: String? = null
) {
    override fun toString(): String {
        return "Genre(id='$id', name='$name', description=$description, count=$count, imageUrl=$imageUrl)"
    }
}