package de.hsb.vibeify.ui.playlist.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Song
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor() : ViewModel() {
    var playlistTitle by mutableStateOf("Absolute banger")
        private set
    var playlistDescription by mutableStateOf("Playlist from Vibeify")
        private set
    var playlistImage by mutableIntStateOf(R.drawable.ic_launcher_background)
        private set
    var songs by mutableStateOf(
        listOf(
            Song(
                name = "Song Name 1",
                duration = 153,
            ),
            Song(
                name = "Song Name 2",
                duration = 215,
            ),
            Song(
                name = "Song Name 3",
                duration = 391,
            )
        )
    )
        private set

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