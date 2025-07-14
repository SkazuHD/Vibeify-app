package de.hsb.vibeify.ui.playlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistService: PlaylistService
) : ViewModel() {

    //
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Add favorites state to be calculated once for all playlists
    private val _playlistFavorites = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val playlistFavorites: StateFlow<Map<String, Boolean>> = _playlistFavorites.asStateFlow()

    // Initialize the ViewModel and load playlists
    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true

                launch {
                    playlistService.playlists.collect { servicePlaylist ->
                        _playlists.value = servicePlaylist
                        loadPlaylistFavorites(servicePlaylist)
                    }
                }
            } catch (e: Exception) {
                Log.d("PlaylistViewModel", "Error loading playlists: ${e.message}")
            } finally {
                _isLoading.value = false
            }

        }
    }

    // Function to refresh playlists
    private fun loadPlaylistFavorites(playlists: List<Playlist>) {
        val favorites = mutableMapOf<String, Boolean>()
        viewModelScope.launch {
            playlists.map { playlist ->
                async {
                    playlist.id to playlistService.isPlaylistLiked(playlist.id)
                }
            }.awaitAll().forEach { (id, isFavorite) ->
                favorites[id] = isFavorite
            }
            _playlistFavorites.value = favorites
        }


    }

    // Function to toggle favorite status of a playlist
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

    // Function to toggle favorite status of a playlist
    fun updatePlaylist(
        playlistId: String,
        playlistName: String,
        description: String,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            playlistService.updatePlaylist(
                playlistId = playlistId,
                title = playlistName,
                description = description,
                imageUrl = imageUrl
            )
        }
    }

}