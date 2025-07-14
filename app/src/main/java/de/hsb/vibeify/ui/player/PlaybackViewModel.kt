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

    // Expose the PlayerServiceV2's player as mediaController
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

    // Combine current song and user repository state to determine if the current song is a favorite
    val isCurrentSongFavorite = combine(
        _currentSong,
        userRepository.state
    ) { currentSong, userState ->
        currentSong?.let { song ->
            userRepository.isSongFavorite(song.id)
        } ?: false
    }

// Expose playback mode from PlayerServiceV2
    fun play(song: Song) {
        viewModelScope.launch {
            playerServiceV2.play(song)
        }
    }

    // Function to play a list of songs starting from a specific index
    fun play(songs: List<Song>, startIndex: Int = 0, playlistId: String? = null) {
        viewModelScope.launch {
            playerServiceV2.play(songs, startIndex, playlistId)
        }
    }

    // Function to play a song at a specific index in the current song list
    fun addToQueue(song: Song) {
        viewModelScope.launch {
            playerServiceV2.insertIntoQueue(song)
        }
    }
    // Function to play the next song in the queue
    fun seekTo(pos: Long) {
        playerServiceV2.seekTo(pos)
    }

    // Function to toggle playback state
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

    // Function to check if a song is a favorite
    fun isSongFavorite(song: Song): Boolean {
        return userRepository.isSongFavorite(song.id)
    }


}
