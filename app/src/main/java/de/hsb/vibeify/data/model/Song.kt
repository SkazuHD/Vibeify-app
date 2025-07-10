package de.hsb.vibeify.data.model

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

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
