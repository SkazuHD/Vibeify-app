package de.hsb.vibeify.data.model

data class Song(
                val id: String = "",
                val name: String = "",
                val artist: String? = null,
                val album: String? = null,
                val imageUrl: String? = null,
                val filePath: String? = null,
                val duration: Int = 0) {
    override fun toString(): String {
        return "Song(name='$name', artist='$artist', album='$album', imageUrl='$imageUrl', filePath='$filePath', duration=$duration)"
    }
}
