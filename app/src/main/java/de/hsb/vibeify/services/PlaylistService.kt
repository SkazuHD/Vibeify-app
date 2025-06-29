package de.hsb.vibeify.services

import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.LIKED_SONGS_PLAYLIST_ID
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class PlaylistDetailData(
    val title: String,
    val description: String,
    val imagePath: Int?,
    val songs: List<Song>,
    val isFavorite: Boolean,
    val isFavoriteAble: Boolean,
    val isOwner: Boolean
)

interface PlaylistService {
    suspend fun getUserPlaylists(userId: String?): List<Playlist>
    suspend fun getPlaylistDetail(playlistId: String): PlaylistDetailData?
    suspend fun createPlaylist(title: String, description: String, userId: String): Playlist
    suspend fun togglePlaylistFavorite(playlistId: String): Boolean
    suspend fun addSongToPlaylist(playlistId: String, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
    fun getPlaylistDurationText(songs: List<Song>): String
}

@Singleton
class PlaylistServiceImpl @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val userRepository: UserRepository
) : PlaylistService {

    override suspend fun getUserPlaylists(userId: String?): List<Playlist> {
        return if (userId != null) {
            val currentUser = userRepository.state.value.currentUser
            if (currentUser != null) {
                val userPlaylists = playlistRepository.getPlaylistsForUser(currentUser.playlists)
                val likedSongsPlaylist = playlistRepository.getLikedSongsPlaylist(emptyList())
                listOf(likedSongsPlaylist) + userPlaylists
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun getPlaylistDetail(playlistId: String): PlaylistDetailData? {
        return if (playlistId == LIKED_SONGS_PLAYLIST_ID) {
            val likedSongIds = userRepository.getLikedSongIds()
            val likedSongsPlaylist = playlistRepository.getLikedSongsPlaylist(likedSongIds)
            val songs = songRepository.getSongsByIds(likedSongIds)

            PlaylistDetailData(
                title = likedSongsPlaylist.title,
                description = likedSongsPlaylist.description ?: "",
                imagePath = likedSongsPlaylist.imagePath,
                songs = songs,
                isFavorite = false,
                isFavoriteAble = false,
                isOwner = true
            )
        } else {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            playlist?.let { playlistData ->
                val isOwner = userRepository.state.value.currentUser?.id == playlistData.userId
                val songs = songRepository.getSongsByIds(playlistData.songIds)
                val isFavorite = userRepository.isPlaylistFavorite(playlistId)

                PlaylistDetailData(
                    title = playlistData.title,
                    description = playlistData.description ?: "",
                    imagePath = playlistData.imagePath,
                    songs = songs,
                    isFavorite = isFavorite,
                    isFavoriteAble = !isOwner,
                    isOwner = isOwner
                )
            }
        }
    }

    override suspend fun createPlaylist(title: String, description: String, userId: String): Playlist {
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            description = description,
            imagePath = null,
            songIds = emptyList()
        )
        playlistRepository.createPlaylist(newPlaylist)
        userRepository.addPlaylistToFavorites(newPlaylist.id)
        return newPlaylist
    }

    override suspend fun togglePlaylistFavorite(playlistId: String): Boolean {
        val isFavorite = userRepository.isPlaylistFavorite(playlistId)
        return if (isFavorite) {
            userRepository.removePlaylistFromFavorites(playlistId)
            false
        } else {
            userRepository.addPlaylistToFavorites(playlistId)
            true
        }
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        playlistRepository.addSongToPlaylist(playlistId, songId)
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        playlistRepository.removeSongFromPlaylist(playlistId, songId)
    }

    override fun getPlaylistDurationText(songs: List<Song>): String {
        val playlistDuration = songs.sumOf { it.duration }
        val playlistDurationMinutes = playlistDuration / 60
        val playlistDurationSeconds = playlistDuration % 60

        return when {
            playlistDurationMinutes >= 60 -> {
                val hours = playlistDurationMinutes / 60
                val minutes = playlistDurationMinutes % 60
                "$hours Stunden und $minutes Minuten"
            }
            playlistDurationMinutes > 0 -> "$playlistDurationMinutes Minuten und $playlistDurationSeconds Sekunden"
            else -> "$playlistDurationSeconds Sekunden"
        }
    }
}
