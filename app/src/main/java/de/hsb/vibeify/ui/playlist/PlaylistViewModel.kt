package de.hsb.vibeify.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistService: PlaylistService
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
        _playlists.value = playlistService.getUserPlaylists(userId)
    }

    fun createPlaylist(playlistName: String, description: String) {
        viewModelScope.launch {
            playlistService.createPlaylist(
                title = playlistName,
                description = description,
                userId = userRepository.state.value.currentUser?.id ?: "",
            )

            loadPlaylists(userRepository.state.value.currentUser?.id)
        }
    }

}