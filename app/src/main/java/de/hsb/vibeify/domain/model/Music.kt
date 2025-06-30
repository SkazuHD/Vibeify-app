package de.hsb.vibeify.domain.model

import de.hsb.vibeify.data.model.Genre
import de.hsb.vibeify.data.model.User

/**
 * Clean Domain Model - Keine Android Dependencies!
 * Reine Business Logic ohne Framework-Kram
 */
data class Song(
    val id: String,
    val title: String,
    val artist: Artist,
    val album: Album,
    val duration: Long, // in milliseconds
    val streamUrl: String,
    val imageUrl: String?,
    val genre: Genre,
    val isLiked: Boolean = false,
    val playCount: Long = 0
) {
    val durationFormatted: String
        get() = formatDuration(duration)

    val isShortSong: Boolean
        get() = duration < 180_000 // under 3 minutes

    private fun formatDuration(millis: Long): String {
        val minutes = millis / 60_000
        val seconds = (millis % 60_000) / 1_000
        return "%d:%02d".format(minutes, seconds)
    }
}

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val followerCount: Long = 0,
    val isVerified: Boolean = false
)

data class Album(
    val id: String,
    val title: String,
    val artist: Artist,
    val releaseYear: Int,
    val imageUrl: String?,
    val totalDuration: Long
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val owner: User,
    val songs: List<Song>,
    val imageUrl: String?,
    val isPublic: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
) {
    val totalDuration: Long
        get() = songs.sumOf { it.duration }

    val songCount: Int
        get() = songs.size
}
