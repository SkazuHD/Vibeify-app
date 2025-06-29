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
import de.hsb.vibeify.data.repository.UserRepository
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
    private val userRepository: UserRepository
) : ViewModel() {
    var playlistTitle by mutableStateOf("")
        private set
    var playlistDescription by mutableStateOf("")
        private set
    var playlistImage by mutableIntStateOf(R.drawable.ic_launcher_background)
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
                playlistImage = it?.imagePath ?: R.drawable.ic_launcher_background
                isLoadingPlayList = false
                isLoadingSongs = true

                _songs.value = it?.songs ?: emptyList()
                isLoadingSongs = false
                isFavorite = it?.isFavorite ?:  false
                isFavoriteAble = it?.isFavoriteAble ?:  true
                isPlaylistOwner = it?.isOwner ?:  false
            }
        }
    }

    fun toggleFavorite(playlistId: String) {
        viewModelScope.launch {
             isFavorite = playlistService.togglePlaylistFavorite(playlistId)
        }
    }

    fun addSongToFavorites(song: Song) {
        viewModelScope.launch {
            userRepository.addSongToFavorites(song.id)
            _songs.value = _songs.value + song
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
}