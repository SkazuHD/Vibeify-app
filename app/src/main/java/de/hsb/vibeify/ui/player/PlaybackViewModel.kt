package de.hsb.vibeify.ui.player
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.services.PlayerServiceV2
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
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

    enum class PlaybackMode {
        SHUFFLE, NONE
    }

    enum class RepeatMode {
        ALL, LOOP, NONE
    }

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode
    private val _playbackMode = MutableStateFlow(PlaybackMode.NONE)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode

    init {
        viewModelScope.launch {
            playerServiceV2.playbackMode.collect { serviceMode ->
                _playbackMode.value = when (serviceMode) {
                    PlayerServiceV2.PlaybackMode.SHUFFLE -> PlaybackMode.SHUFFLE
                    PlayerServiceV2.PlaybackMode.NONE -> PlaybackMode.NONE
                }
            }
        }
        viewModelScope.launch {
            playerServiceV2.repeatMode.collect { serviceMode ->
                _repeatMode.value = when (serviceMode) {
                    PlayerServiceV2.RepeatMode.ALL -> RepeatMode.ALL
                    PlayerServiceV2.RepeatMode.LOOP -> RepeatMode.LOOP
                    PlayerServiceV2.RepeatMode.NONE -> RepeatMode.NONE
                }
            }
        }
    }

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

    fun toggleRepeatMode() {
        playerServiceV2.toggleRepeatMode()
    }

    override fun onCleared() {
        super.onCleared()
    }
}
