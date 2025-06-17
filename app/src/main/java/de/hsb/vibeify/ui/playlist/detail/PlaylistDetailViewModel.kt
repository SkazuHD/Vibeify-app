package de.hsb.vibeify.ui.playlist.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.PlaylistRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
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

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            val playlist = playlistRepository.getPlaylistById(playlistId)
            playlist?.let {
                playlistTitle = it.title
                playlistDescription = it.description
                playlistImage = it.imageRes
                songs = it.songs
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
}