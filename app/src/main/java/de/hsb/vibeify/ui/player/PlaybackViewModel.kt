package de.hsb.vibeify.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.PlayerServiceV2
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val playerServiceV2: PlayerServiceV2,
    private val userRepository: UserRepository,
) : ViewModel() {

    val mediaController = playerServiceV2.player
    private val _isPlaying = playerServiceV2.isPlaying
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val position: StateFlow<Long> = playerServiceV2.position
    val duration: StateFlow<Long> = playerServiceV2.duration

    private val _currentSong = playerServiceV2.currentSong
    val currentSong: StateFlow<Song?> = _currentSong

    val currentPlaylistId = playerServiceV2.currentPlaylistId

    val upcomingSongs = playerServiceV2.upcomingSongs
    val currentSongList = playerServiceV2.currentSongList

    val isCurrentSongFavorite = combine(
        _currentSong,
        userRepository.state
    ) { currentSong, userState ->
        currentSong?.let { song ->
            userRepository.isSongFavorite(song.id)
        } ?: false
    }


    fun play(song: Song) {
        viewModelScope.launch {
            playerServiceV2.play(song)
        }
    }

    fun play(songs: List<Song>, startIndex: Int = 0, playlistId: String? = null) {
        viewModelScope.launch {
            playerServiceV2.play(songs, startIndex, playlistId)
        }
    }

    fun seekTo(pos: Long) {
        playerServiceV2.seekTo(pos)
    }

    fun toggleFavorite() {
        _currentSong.value?.let { song ->
            viewModelScope.launch {
                if (userRepository.isSongFavorite(song.id)) {
                    userRepository.removeSongFromFavorites(song.id)
                } else {
                    userRepository.addSongToFavorites(song.id)
                }
            }
        }
    }

    fun isSongFavorite(song: Song): Boolean {
        return userRepository.isSongFavorite(song.id)
    }


}
