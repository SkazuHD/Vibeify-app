package de.hsb.vibeify.data.model

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

// Song data class representing a song in the application

data class Song(
    val id: String = "",
    val name: String = "",
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val imageUrl: String? = null,
    val filePath: String? = null,
    val duration: Int = 0,
    val year: String? = null,
) {
    override fun toString(): String {
        return "Song(name='$name', artist='$artist', album='$album', imageUrl='$imageUrl', filePath='$filePath', duration=$duration)"
    }

    /**
     * Converts the Song object to a MediaItem for use in media playback.
     *
     * @return MediaItem representing the song.
     */

    companion object {
        fun toMediaItem(song: Song): MediaItem {
            return MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(song.filePath?.toUri())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.name)
                        .setArtist(song.artist ?: "Unknown Artist")
                        .setAlbumTitle(song.album ?: "Unknown Album")
                        .setArtworkUri(song.imageUrl?.toUri())
                        .build()
                )
                .build()
        }
    }

}
