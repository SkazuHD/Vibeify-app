package de.hsb.vibeify.ui.playlist.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.LIKED_SONGS_PLAYLIST_ID
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var playlistTitle by mutableStateOf("")
        private set
    var playlistDescription by mutableStateOf("")
        private set
    var playlistImage by mutableIntStateOf(R.drawable.ic_launcher_background)
        private set
    var songs by mutableStateOf(
        listOf<Song>()
    )
        private set
    var isFavorite by mutableStateOf(false)
        private set
    var isFavoriteAble by mutableStateOf(true)
        private set
    var isLoadingPlayList by mutableStateOf(true)
        private set
    var isLoadingSongs by mutableStateOf(true)
        private set

    var isPlaylistOwner by mutableStateOf(false)
        private set


    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            isLoadingPlayList = true
            if (playlistId == LIKED_SONGS_PLAYLIST_ID) {
                val likedSongIds = userRepository.getLikedSongIds()
                Log.d("PlaylistDetailViewModel", "Liked song IDs: $likedSongIds")
                val likedSongsPlaylist = playlistRepository.getLikedSongsPlaylist(likedSongIds)

                playlistTitle = likedSongsPlaylist.title
                playlistDescription = likedSongsPlaylist.description ?: ""
                playlistImage = likedSongsPlaylist.imagePath ?: R.drawable.ic_launcher_background
                isLoadingPlayList = false
                isLoadingSongs = true
                songs = songRepository.getSongsByIds(likedSongIds)
                isFavorite = false
                isFavoriteAble = false
                isLoadingSongs = false
                isPlaylistOwner = true
            } else {
                val playlist = playlistRepository.getPlaylistById(playlistId)
                isPlaylistOwner = userRepository.state.value.currentUser?.id == playlist?.userId
                playlist?.let {
                    playlistTitle = it.title
                    playlistDescription = it.description ?: ""
                    playlistImage = it.imagePath ?: R.drawable.ic_launcher_background
                    isLoadingPlayList = false
                    isLoadingSongs = true
                    songs = songRepository.getSongsByIds(it.songIds)
                }
                isFavorite = userRepository.isPlaylistFavorite(playlistId)
                isFavoriteAble = !isPlaylistOwner
                isLoadingSongs = false
            }
        }
    }

    val playlistDurationText: String
        get() {
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

    fun toggleFavorite(playlistId: String) {
        viewModelScope.launch {
            if (isFavorite) {
                userRepository.removePlaylistFromFavorites(playlistId)
            } else {
                userRepository.addPlaylistToFavorites(playlistId)
            }
            isFavorite = !isFavorite
        }
    }

    fun addSongToFavorites(song: Song) {
        viewModelScope.launch {
            userRepository.addSongToFavorites(song.id)
        }
    }

    fun removeSongFromFavorites(song: Song) {
        viewModelScope.launch {
            userRepository.removeSongFromFavorites(song.id)
        }
    }

    fun isSongFavorite(song: Song): Boolean {
        return userRepository.isSongFavorite(song.id)
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, song.id)
            songs = songs + song
        }
    }
}