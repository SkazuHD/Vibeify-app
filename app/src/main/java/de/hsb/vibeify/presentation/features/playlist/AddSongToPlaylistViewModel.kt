package de.hsb.vibeify.ui.playlist

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

    private val _playlists = MutableStateFlow(emptyList<Playlist>())
    val playlists: StateFlow<List<Playlist>> = _playlists

    init {
        viewModelScope.launch {
            _playlists.value = playlistService.getPlaylistsCreatedByCurrentUser()
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            playlistService.addSongToPlaylist(playlistId, songId)
        }
    }
}