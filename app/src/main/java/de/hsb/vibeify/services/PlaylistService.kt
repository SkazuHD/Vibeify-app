package de.hsb.vibeify.services

import android.util.Log
import androidx.core.net.toUri
import de.hsb.vibeify.api.generated.apis.DefaultApi
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.LIKED_SONGS_PLAYLIST_ID
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class PlaylistDetailData(
    val title: String,
    val description: String,
    val imageUrl: String?,
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
    suspend fun createPlaylist(
        title: String,
        description: String,
        imageUrl: String?,
        userId: String
    ): Playlist

    suspend fun updatePlaylist(
        playlistId: String,
        title: String,
        description: String,
        imageUrl: String? = null
    ): Boolean

    suspend fun isPlaylistLiked(playlistId: String): Boolean
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
    private val webService: DefaultApi,
    private val context: android.content.Context,
) : PlaylistService {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val playlists = MutableStateFlow(emptyList<Playlist>())

    init {
        scope.launch {
            userRepository.state
                .map { it.currentUser }
                .distinctUntilChanged()
                .collect { user ->
                    if (user == null) {
                        playlists.value = emptyList()
                    } else {
                        launch {
                            try {
                                playlists.value = getUserPlaylists(user.id)
                            } catch (e: Exception) {
                                Log.e("PlaylistService", "Error loading playlists", e)
                            }
                        }
                    }
                }
        }
    }

    override suspend fun getUserPlaylists(userId: String?): List<Playlist> {
        return if (userId != null) {
            val currentUser = userRepository.state.value.currentUser
            if (currentUser != null) {
                val userPlaylistsDeferred = scope.async {
                    playlistRepository.getPlaylistsByIds(currentUser.playlists)
                }
                val likedSongsDeferred = scope.async {
                    playlistRepository.getLikedSongsPlaylist(emptyList())
                }

                val userPlaylists = userPlaylistsDeferred.await()
                val likedSongsPlaylist = likedSongsDeferred.await()

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

    override suspend fun isPlaylistLiked(playlistId: String): Boolean {
        val playlist = playlistRepository.getPlaylistById(playlistId)
        playlist?.let { playlistData ->
            val isOwner = userRepository.state.value.currentUser?.id == playlistData.userId
            val isFavorite = userRepository.isPlaylistFavorite(playlistId)
            return isFavorite && !isOwner
        }
        return false
    }

    override suspend fun getPlaylistDetail(playlistId: String): PlaylistDetailData? {
        return if (playlistId == LIKED_SONGS_PLAYLIST_ID) {
            val likedSongIds = userRepository.getLikedSongIds()
            val likedSongsPlaylist = playlistRepository.getLikedSongsPlaylist(likedSongIds)
            val songs = songRepository.getSongsByIds(likedSongIds)

            PlaylistDetailData(
                title = likedSongsPlaylist.title,
                description = likedSongsPlaylist.description ?: "",
                imageUrl = likedSongsPlaylist.imageUrl,
                songs = songs,
                isFavorite = false,
                isFavoriteAble = false,
                isOwner = true
            )
        } else if (playlistId.startsWith("genre_")) {
            val genreName = playlistId.removePrefix("genre_")
            getGenreAsPlaylistDetail(genreName)
        } else {
            val playlist = playlists.value.find { it.id == playlistId }
                ?: playlistRepository.getPlaylistById(playlistId)
            playlist?.let { playlistData ->
                val isOwner = userRepository.state.value.currentUser?.id == playlistData.userId
                val songs = songRepository.getSongsByIds(playlistData.songIds)
                val isFavorite = userRepository.isPlaylistFavorite(playlistId)

                PlaylistDetailData(
                    title = playlistData.title,
                    description = playlistData.description ?: "",
                    imageUrl = playlistData.imageUrl,
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

    private suspend fun uploadPhoto(id: String, imageUrl: String): String {
        val uri = try {
            imageUrl.toUri()
        } catch (e: Exception) {
            null
        }
        val inputStream = context.contentResolver.openInputStream(uri!!)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes == null) {
            Log.e("PlaylistService", "Failed to read bytes from image URI: $uri")
            return ""
        }
        Log.d("PlaylistService", "uploadPhoto called with id: $id, imageUrl: $imageUrl")
        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, bytes.size)
        val part = MultipartBody.Part.createFormData("file", "cover_$id.jpg", requestBody)
        val call =
            webService.uploadCoverUploadCoverPlaylistIdPost(id, part).awaitResponse()
        if (call.isSuccessful) {
            Log.d("PlaylistService", "Photo uploaded successfully for Playlist: $id")
            return "https://vibeify-app.skazu.net/cover/playlist/$id?v=${System.currentTimeMillis()}"
        } else {
            Log.e("PlaylistService", "Failed to upload photo: ${call.errorBody()?.string()}")
            return "https://vibeify-app.skazu.net/cover/playlist/$id"
        }
    }

    override suspend fun createPlaylist(
        title: String,
        description: String,
        imageUrl: String?,
        userId: String
    ): Playlist {
        var imageUrl = imageUrl
        val playlistId = UUID.randomUUID().toString()
        if (!imageUrl.isNullOrEmpty()) {
            imageUrl = try {
                uploadPhoto(playlistId, imageUrl)
            } catch (e: Exception) {
                Log.e("PlaylistService", "Error uploading photo: ${e.message}")
                null
            }
        }

        val newPlaylist = Playlist(
            id = playlistId,
            userId = userId,
            title = title,
            description = description,
            imageUrl = imageUrl,
            songIds = emptyList()
        )

        playlistRepository.createPlaylist(newPlaylist)
        userRepository.addPlaylistToFavorites(newPlaylist.id)
        playlists.value = playlists.value + newPlaylist
        return newPlaylist
    }

    override suspend fun updatePlaylist(
        playlistId: String,
        title: String,
        description: String,
        imageUrl: String?
    ): Boolean {
        var updatedImageUrl = imageUrl
        if (!updatedImageUrl.isNullOrEmpty()) {
            updatedImageUrl = try {
                uploadPhoto(playlistId, updatedImageUrl)
            } catch (e: Exception) {
                Log.e("PlaylistService", "Error uploading photo: ${e.message}")
                imageUrl
            }
        }
        val updatedPlaylist = Playlist(
            id = playlistId,
            userId = userRepository.state.value.currentUser?.id ?: "",
            title = title,
            description = description,
            imageUrl = updatedImageUrl,
            songIds = playlists.value.find { it.id == playlistId }?.songIds ?: emptyList()
        )

        return playlistRepository.updatePlaylist(updatedPlaylist).also { success ->
            if (success) {
                playlists.value =
                    playlists.value.map { if (it.id == playlistId) updatedPlaylist else it }
            }
        }
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
        playlistRepository.addSongToPlaylist(playlistId, songId).also {
            // Update the playlist in the local state
            if (playlistId == LIKED_SONGS_PLAYLIST_ID) {
                userRepository.addSongToFavorites(songId)
            } else {
                val updatedPlaylist = playlists.value.find { it.id == playlistId }
                if (updatedPlaylist != null) {
                    val updatedSongs = updatedPlaylist.songIds + songId
                    playlists.value = playlists.value.map {
                        if (it.id == playlistId) it.copy(songIds = updatedSongs) else it
                    }
                }
            }
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        if (playlistId == LIKED_SONGS_PLAYLIST_ID) {
            userRepository.removeSongFromFavorites(songId)
        } else {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)

        }
        val updatedPlaylist = playlists.value.find { it.id == playlistId }
        if (updatedPlaylist != null) {
            val updatedSongs = updatedPlaylist.songIds.filter { it != songId }
            playlists.value = playlists.value.map {
                if (it.id == playlistId) it.copy(songIds = updatedSongs) else it
            }
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
                    imageUrl = null,
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
