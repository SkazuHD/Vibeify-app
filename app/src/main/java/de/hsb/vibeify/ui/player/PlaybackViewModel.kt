package de.hsb.vibeify.ui.player
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
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


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    private val _duration = MutableStateFlow(1L)
    val duration: StateFlow<Long> = _duration

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    var controller: MediaController? = null

    init {
        viewModelScope.launch {
            controller = playerServiceV2.awaitController()
            _currentSong.value = playerServiceV2.currentSong
            while (true) {
                controller?.let {
                    _position.value = it.currentPosition
                    _duration.value = it.duration.coerceAtLeast(1L)
                    _isPlaying.value = it.isPlaying
                    _currentSong.value = playerServiceV2.currentSong
                }
                kotlinx.coroutines.delay(300)
            }
        }
    }
    fun play(song: Song) {
        viewModelScope.launch {
            val ctrl = controller ?: playerServiceV2.awaitController()
            if (_currentSong.value?.name != song.name || controller?.currentMediaItem == null) {
                val mediaItem = playerServiceV2.buildMediaItem(song)
                ctrl.setMediaItem(mediaItem)
                ctrl.repeatMode = Player.REPEAT_MODE_ONE
                ctrl.prepare()
                ctrl.seekTo(_position.value)
                ctrl.play()
                playerServiceV2.currentSong = song
                _currentSong.value = song
            } else {
                ctrl.play()
            }
            _isPlaying.value = true
        }
    }


    fun pause() {
        controller?.pause()
        _isPlaying.value = false
    }

    fun seekTo(pos: Long) {
        controller?.seekTo(pos)
        _position.value = pos
    }

    override fun onCleared() {
        super.onCleared()
    }
}