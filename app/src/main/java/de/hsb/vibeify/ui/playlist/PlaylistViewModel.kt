package de.hsb.vibeify.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _playlists = MutableStateFlow(emptyList<Playlist>())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.state
                .map { it.currentUser?.id }
                .distinctUntilChanged()
                .collect { userId ->
                    loadPlaylists(userId)
                }
        }
    }

    private suspend fun loadPlaylists(userId: String?) {
        if (userId != null) {
            val currentUser = userRepository.state.value.currentUser
            if (currentUser != null) {
                val userPlaylists = playlistRepository.getPlaylistsForUser(currentUser.playlists)
                val likedSongsPlaylist = playlistRepository.getLikedSongsPlaylist(emptyList())
                _playlists.value = listOf(likedSongsPlaylist) + userPlaylists
            }
        } else {
            _playlists.value = emptyList()
        }
    }

    fun createPlaylist(playlistName: String, description: String) {
        viewModelScope.launch {
            val newPlaylist = Playlist(
                id = UUID.randomUUID().toString(),
                userId = userRepository.state.value.currentUser?.id ?: "",
                title = playlistName,
                description = description,
                imagePath = null,
                songIds = emptyList()
            )
            playlistRepository.createPlaylist(newPlaylist)
            userRepository.addPlaylistToFavorites(newPlaylist.id)

            // Refresh playlists after creation
            loadPlaylists(userRepository.state.value.currentUser?.id)
        }
    }

}