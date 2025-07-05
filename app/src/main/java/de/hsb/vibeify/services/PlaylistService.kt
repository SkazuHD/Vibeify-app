package de.hsb.vibeify.services

import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.LIKED_SONGS_PLAYLIST_ID
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    suspend fun getPlaylistsByIds(playlistIds: List<String>): List<Playlist>
    suspend fun getUserPlaylists(userId: String?): List<Playlist>

    suspend fun getPlaylistCreatedByUser(userId: String): List<Playlist>

    suspend fun getPlaylistsCreatedByCurrentUser(): List<Playlist>
    suspend fun getPlaylistDetail(playlistId: String): PlaylistDetailData?
    suspend fun createPlaylist(title: String, description: String, userId: String): Playlist

    suspend fun removePlaylist(playlistId: String): Boolean
    suspend fun togglePlaylistFavorite(playlistId: String): Boolean
    suspend fun addSongToPlaylist(playlistId: String, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
    fun getPlaylistDurationText(songs: List<Song>): String

    suspend fun getGenreAsPlaylistDetail(genreName: String): PlaylistDetailData?
    val playlists: MutableStateFlow<List<Playlist>>
}

@Singleton
class PlaylistServiceImpl @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val userRepository: UserRepository,
    private val discoveryService: DiscoveryService,
) : PlaylistService {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val playlists = MutableStateFlow(emptyList<Playlist>())

    init {
        scope.launch {
            userRepository.state
                .map { it.currentUser?.id }
                .distinctUntilChanged()
                .collect { userId ->
                    if (userId == null) {
                        playlists.value = emptyList()
                    } else {
                        playlists.value = getUserPlaylists(userId)
                    }
                }
        }
    }

    override suspend fun getUserPlaylists(userId: String?): List<Playlist> {
        return if (userId != null) {
            val currentUser = userRepository.state.value.currentUser
            if (currentUser != null) {
                val userPlaylists = playlistRepository.getPlaylistsByIds(currentUser.playlists)
                val likedSongsPlaylist = playlistRepository.getLikedSongsPlaylist(emptyList())
                listOf(likedSongsPlaylist) + userPlaylists
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun getPlaylistCreatedByUser(userId: String): List<Playlist> {
        return playlistRepository.getPlaylistsByUserId(userId)
    }

    override suspend fun getPlaylistsCreatedByCurrentUser(): List<Playlist> {
        val currentUser = userRepository.state.value.currentUser
        return if (currentUser != null) {
            playlistRepository.getPlaylistsByUserId(currentUser.id)
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
        } else if (playlistId.startsWith("genre_")) {
            val genreName = playlistId.removePrefix("genre_")
            getGenreAsPlaylistDetail(genreName)
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

    override suspend fun getPlaylistsByIds(playlistIds: List<String>): List<Playlist> {
        val genrePlaylists = playlistIds.filter { it.startsWith("genre_") }
            .map { it.removePrefix("genre_") }
            .mapNotNull { getGenreAsPlaylist(it) }
        val otherPlaylists =
            playlistRepository.getPlaylistsByIds(playlistIds.filterNot { it.startsWith("genre_") })
        //Sort to original order
        val sortedPlaylists = playlistIds.mapNotNull { id ->
            when {
                id.startsWith("genre_") -> genrePlaylists.find { it.id == id }
                else -> otherPlaylists.find { it.id == id }
            }
        }
        return sortedPlaylists
    }

    override suspend fun createPlaylist(
        title: String,
        description: String,
        userId: String
    ): Playlist {
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
        playlists.value = playlists.value + newPlaylist
        return newPlaylist
    }

    override suspend fun removePlaylist(playlistId: String): Boolean {
        val isRemoved = playlistRepository.deletePlaylist(playlistId)
        if (isRemoved) {
            userRepository.removePlaylistFromFavorites(playlistId)
            playlists.value = playlists.value.filter { it.id != playlistId }
        }
        return isRemoved
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
        if (playlistId == LIKED_SONGS_PLAYLIST_ID) {
            userRepository.removeSongFromFavorites(songId)
        } else {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)

        }
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

    suspend fun getGenreAsPlaylist(genreName: String): Playlist? {
        val genreList = discoveryService.getGenreList()
        val genre = genreList.find { it.name.equals(genreName, ignoreCase = true) }
        return genre?.let {
            Playlist(
                id = "genre_${it.name}",
                userId = "system",
                title = it.name,
                description = it.description ?: "Alle Songs im Genre ${it.name}",
                songIds = emptyList()
            )
        }
    }

    override suspend fun getGenreAsPlaylistDetail(genreName: String): PlaylistDetailData? {
        return try {
            val songs = discoveryService.getSongsByGenre(genreName)
            if (songs.isNotEmpty()) {
                PlaylistDetailData(
                    title = genreName,
                    description = "Alle Songs im Genre $genreName \n Created by Vibeify",
                    imagePath = null,
                    songs = songs,
                    isFavorite = false,
                    isFavoriteAble = false,
                    isOwner = false
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
