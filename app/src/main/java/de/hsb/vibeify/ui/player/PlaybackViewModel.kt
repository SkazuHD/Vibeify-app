package de.hsb.vibeify.ui.player
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.services.PlayerServiceV2
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val playerServiceV2: PlayerServiceV2
) : ViewModel() {


    private val _isPlaying = playerServiceV2.isPlaying
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val position: StateFlow<Long> = playerServiceV2.position
    val duration: StateFlow<Long> = playerServiceV2.duration

    private val _currentSong = playerServiceV2.currentSong
    val currentSong: StateFlow<Song?> = _currentSong

    val currentPlaylistId = playerServiceV2.currentPlaylistId

    val upcomingSongs = playerServiceV2.upcomingSongs
    val currentSongList = playerServiceV2.currentSongList

    val playbackMode: StateFlow<PlayerServiceV2.PlaybackMode> = playerServiceV2.playbackMode

    fun play(song: Song) {
        viewModelScope.launch {
            playerServiceV2.play(song)
        }
    }
    fun play(songs: List<Song>, startIndex: Int = 0, playlistId : String? = null) {
        viewModelScope.launch {
            playerServiceV2.play(songs, startIndex, playlistId)
        }
    }
    fun resume() {
        playerServiceV2.resume()
    }

    fun skipToNext() {
        playerServiceV2.skipToNext()
    }
    fun skipToPrevious() {
        playerServiceV2.skipToPrevious()
    }


    fun pause() {
        playerServiceV2.pause()
    }

    fun seekTo(pos: Long) {
        playerServiceV2.seekTo(pos)
    }

    fun togglePlaybackMode() {
        playerServiceV2.togglePlaybackMode()
    }

    override fun onCleared() {
        super.onCleared()
    }
}