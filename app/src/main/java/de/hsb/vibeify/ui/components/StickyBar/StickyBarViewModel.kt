package de.hsb.vibeify.ui.components.StickyBar

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.services.PlayerServiceV2
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel
class StickyBarViewModel @Inject constructor(
    private val playerServiceV2: PlayerServiceV2
) : ViewModel() {

    private val _isPlaying = playerServiceV2.isPlaying
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _position = playerServiceV2.position
    val position: StateFlow<Long> = _position

    private val _duration = playerServiceV2.duration
    val duration: StateFlow<Long> = _duration

    private val _playerState = playerServiceV2.playerState
    val playerState: StateFlow<Int> = _playerState

    val currentSong = playerServiceV2.currentSong


    fun play() {
        playerServiceV2.resume()
    }

    fun pause() {
        playerServiceV2.pause()
    }

    fun stop() {
        playerServiceV2.stop()
    }

    fun clearMediaItems() {
        playerServiceV2.clearMediaItems()
    }

    fun seekTo(pos: Long) {
        playerServiceV2.seekTo(pos)
    }
}