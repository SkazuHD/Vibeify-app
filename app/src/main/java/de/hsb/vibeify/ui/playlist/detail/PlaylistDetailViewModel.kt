package de.hsb.vibeify.ui.playlist.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistService: PlaylistService,
) : ViewModel() {
    var playlistTitle by mutableStateOf("")
        private set
    var playlistDescription by mutableStateOf("")
        private set
    var playlistImage by mutableStateOf("")
        private set
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    val playlistDurationText: StateFlow<String> = songs.map { songList ->
        if (songList.isEmpty()) {
            "0 Sekunden"
        } else {
            playlistService.getPlaylistDurationText(songList)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "0 Sekunden"
    )

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
            val playsListData = playlistService.getPlaylistDetail(playlistId)
            playsListData.let {
                Log.d("PlaylistDetailViewModel", "Loaded playlist data: $it")
                playlistTitle = it?.title ?: ""
                playlistDescription = it?.description ?: ""
                Log.d(
                    "PlaylistDetailViewModel",
                    "Playlist image URL: $playlistImage -> ${it?.imageUrl}"
                )
                playlistImage = it?.imageUrl ?: ""
                isLoadingPlayList = false
                isLoadingSongs = true

                _songs.value = it?.songs ?: emptyList()
                isLoadingSongs = false
                isFavorite = it?.isFavorite ?: false
                isFavoriteAble = it?.isFavoriteAble ?: true
                isPlaylistOwner = it?.isOwner ?: false
            }
        }
    }

    fun togglePlaylistFavorite(playlistId: String) {
        viewModelScope.launch {
            isFavorite = playlistService.togglePlaylistFavorite(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            playlistService.addSongToPlaylist(playlistId, song.id)
            _songs.value = _songs.value + song
        }
    }

    fun removeSongFromPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            playlistService.removeSongFromPlaylist(playlistId, song.id)
            _songs.value = _songs.value.filter { it.id != song.id }
        }
    }

    fun removePlaylist(playlistId: String): Boolean {
        viewModelScope.launch {
            playlistService.removePlaylist(playlistId)
        }
        return true
    }
}