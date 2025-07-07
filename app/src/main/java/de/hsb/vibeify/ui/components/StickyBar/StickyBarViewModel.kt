package de.hsb.vibeify.ui.components.StickyBar

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.PlayerServiceV2
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine


@HiltViewModel
class StickyBarViewModel @Inject constructor(
    private val playerServiceV2: PlayerServiceV2,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _isPlaying = playerServiceV2.isPlaying
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _position = playerServiceV2.position
    val position: StateFlow<Long> = _position

    private val _duration = playerServiceV2.duration
    val duration: StateFlow<Long> = _duration

    private val _playerState = playerServiceV2.playerState

    val mediaController = playerServiceV2.player
    val playerState: StateFlow<Int> = _playerState

    val currentSong = playerServiceV2.currentSong

    val isCurrentSongFavorite = combine(
        currentSong,
        userRepository.state
    ) { currentSong, userState ->
        currentSong?.let { song ->
            userRepository.isSongFavorite(song.id)
        } ?: false
    }

    fun play() {
        playerServiceV2.resume()
    }

    fun seekTo(pos: Long) {
        playerServiceV2.seekTo(pos)
    }
}