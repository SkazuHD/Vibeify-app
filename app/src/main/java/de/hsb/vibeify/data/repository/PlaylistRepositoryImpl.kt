package de.hsb.vibeify.data.repository

import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor() : PlaylistRepository {
    private val playlists = listOf(
        Playlist(
            id = "123",
            title = "Absolute banger",
            description = "Playlist from Vibeify",
            imageRes = R.drawable.ic_launcher_background,
            songs = listOf(
                Song(name = "Song Name 1", duration = 153),
                Song(name = "Song Name 2", duration = 215),
                Song(name = "Song Name 3", duration = 391)
            )
        ),
        Playlist(
            id = "456",
            title = "Chill Vibes",
            description = "Relax and enjoy!",
            imageRes = R.drawable.ic_launcher_background,
            songs = listOf(
                Song(name = "Chill Song 1", duration = 200),
                Song(name = "Chill Song 2", duration = 180)
            )
        )
    )

    override suspend fun getPlaylistById(id: String): Playlist? {
        return playlists.find { it.id == id }
    }
}

