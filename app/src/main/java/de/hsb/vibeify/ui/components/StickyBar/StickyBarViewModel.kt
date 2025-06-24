package de.hsb.vibeify.ui.components.StickyBar

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
class StickyBarViewModel @Inject constructor(
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

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState

    private var controller: MediaController? = null

    init {
        viewModelScope.launch {
            controller = playerServiceV2.awaitController()
            while (true) {
                controller?.let {
                    _position.value = it.currentPosition
                    _duration.value = it.duration.coerceAtLeast(1L)
                    _isPlaying.value = it.isPlaying
                    _playbackState.value = it.playbackState
                }
                kotlinx.coroutines.delay(300)
            }
        }
    }


    fun play() {
        controller?.play()
        _isPlaying.value = true
    }

    fun pause() {
        controller?.pause()
        _isPlaying.value = false
    }

    fun stop() {
        controller?.stop()
        _isPlaying.value = false
    }

    fun clearMediaItems() {
        controller?.clearMediaItems()
    }

    fun seekTo(pos: Long) {
        controller?.seekTo(pos)
        _position.value = pos
    }
}