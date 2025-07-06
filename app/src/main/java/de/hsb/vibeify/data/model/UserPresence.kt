package de.hsb.vibeify.data.model

data class UserPresence(
    val userId: String,
    val isOnline: Boolean,
    val lastSeen: Long,
    val currentlyPlaying: CurrentlyPlaying? = null
)

data class CurrentlyPlaying(
    val songId: String,
    val songName: String,
    val artist: String,
    val album: String,
    val imageUrl: String?,
    val startTime: Long,
    val isPlaying: Boolean
) {

    constructor() : this("", "", "", "", null, 0L, false)
    companion object {
        fun from(song: Song): CurrentlyPlaying {
            return CurrentlyPlaying(
                songId = song.id,
                songName = song.name,
                artist = song.artist ?: "Unknown Artist",
                album = song.album ?: "Unknown Album",
                imageUrl = song.imageUrl,
                startTime = System.currentTimeMillis(),
                isPlaying = true
            )
        }
    }
}
