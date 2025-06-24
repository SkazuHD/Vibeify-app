package de.hsb.vibeify.ui.playlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var playlists by mutableStateOf(emptyList<Playlist>())
        private set

    init {
        viewModelScope.launch {
            userRepository.state.collect { userState ->
                if (userState.currentUser != null) {
                    val userPlaylists = playlistRepository.getPlaylistsForUser(userState.currentUser.playlists)

                    val likedSongIds = userRepository.getLikedSongIds()
                    val likedSongsPlaylist = playlistRepository.getLikedSongsPlaylist(likedSongIds)

                    playlists = listOf(likedSongsPlaylist) + userPlaylists
                } else {
                    playlists = emptyList()
                }
            }
        }
    }

}