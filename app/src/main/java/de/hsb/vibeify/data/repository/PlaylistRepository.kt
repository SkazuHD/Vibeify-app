package de.hsb.vibeify.data.repository

import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import javax.inject.Inject
import javax.inject.Singleton

interface PlaylistRepository {
    suspend fun getPlaylistById(id: String): Playlist?
    suspend fun getAllPlaylists(): List<Playlist> {
        return emptyList()
    }
    suspend fun searchPlaylists(query: String): List<Playlist> {
        return emptyList()
    }
    suspend fun getPlaylistsByUserId(userId: String): List<Playlist> {
        return emptyList()
    }
    suspend fun createPlaylist(playlist: Playlist): Boolean {
        return false
    }
    suspend fun updatePlaylist(playlist: Playlist): Boolean {
        return false
    }
    suspend fun deletePlaylist(id: String): Boolean {
        return false
    }
    suspend fun addSongToPlaylist(playlistId: String, song: Song): Boolean {
        return false
    }
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        return false
    }
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor() : PlaylistRepository {
    private val playlists = mutableListOf(
        Playlist(
            id = "123",
            title = "Absolute banger",
            description = "Playlist from Vibeify",
            imagePath = R.drawable.ic_launcher_background,
            songs = mutableListOf(
                Song(name = "Song Name 1", duration = 153),
                Song(name = "Song Name 2", duration = 215),
                Song(name = "Song Name 3", duration = 391)
            )
        ),
        Playlist(
            id = "456",
            title = "Chill Vibes",
            description = "Relax and enjoy!",
            imagePath = R.drawable.ic_launcher_background,
            songs = mutableListOf(
                Song(name = "Chill Song 1", duration = 200),
                Song(name = "Chill Song 2", duration = 180)
            )
        ),
        Playlist(
            id = "789",
            title = "Workout Hits",
            description = "Get pumped up!",
            imagePath = R.drawable.ic_launcher_background,
            songs = mutableListOf(
                Song(name = "Workout Song 1", duration = 240),
                Song(name = "Workout Song 2", duration = 300)
            )
        )
    )

    override suspend fun getPlaylistById(id: String): Playlist? {
        return playlists.find { it.id == id }
    }

    override suspend fun getAllPlaylists(): List<Playlist> {
        return playlists
    }
    override suspend fun searchPlaylists(query: String): List<Playlist> {
        return playlists.filter { it.title.contains(query, ignoreCase = true) }
    }
    override suspend fun getPlaylistsByUserId(userId: String): List<Playlist> {
        return playlists
    }
    override suspend fun createPlaylist(playlist: Playlist): Boolean {
        playlists.plus(playlist)
        return true
    }
    override suspend fun updatePlaylist(playlist: Playlist): Boolean {
        val index = playlists.indexOfFirst { it.id == playlist.id }
        if (index != -1) {
            playlists[index] = playlist
            return true
        }
        return false
    }
    override suspend fun deletePlaylist(id: String): Boolean {
        val playlist = playlists.find { it.id == id }
        return if (playlist != null) {
            playlists.remove(playlist)
            true
        } else {
            false
        }
    }

    override suspend fun addSongToPlaylist(playlistId: String, song: Song): Boolean {
        val playlist = playlists.find { it.id == playlistId }
        return if (playlist != null) {
            playlist.songs = playlist.songs + song
            true
        } else {
            false
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        val playlist = playlists.find { it.id == playlistId }
        return if (playlist != null) {
            playlist.songs = playlist.songs.filter { it.id != songId }
            true
        } else {
            false
        }
    }


}

