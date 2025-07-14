package de.hsb.vibeify.ui.playlist.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSongToPlaylistViewModel @Inject constructor(
    private val playlistService: PlaylistService,
) : ViewModel() {
// ViewModel for managing the state of adding songs to playlists
    private val _playlists = MutableStateFlow(emptyList<Playlist>())
    val playlists: StateFlow<List<Playlist>> = _playlists

    // Exposes the list of playlists created by the current user
    init {
        viewModelScope.launch {
            _playlists.value = playlistService.getPlaylistsCreatedByCurrentUser()
        }
    }
    // Function to add a song to a specific playlist
    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            playlistService.addSongToPlaylist(playlistId, songId)
        }
    }
}