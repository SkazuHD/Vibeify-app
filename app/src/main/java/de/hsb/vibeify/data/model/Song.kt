package de.hsb.vibeify.data.model

data class Song(val name: String,
                val artist: String? = null,
                val album: String? = null,
                val duration: Int = 0) {
    override fun toString(): String {
        return "Song(name='$name', artist='$artist', album='$album', duration=$duration)"
    }
}
