package de.hsb.vibeify.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistService: PlaylistService
) : ViewModel() {

    private val _playlists = playlistService.playlists
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    fun createPlaylist(playlistName: String, description: String, imageUrl: String? = null) {
        viewModelScope.launch {
            playlistService.createPlaylist(
                title = playlistName,
                description = description,
                imageUrl = imageUrl,
                userId = userRepository.state.value.currentUser?.id ?: "",
            )
        }
    }

}