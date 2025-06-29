package de.hsb.vibeify.ui.components.songCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartSongCardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun addSongToFavorites(song: Song) {
        viewModelScope.launch {
            userRepository.addSongToFavorites(song.id)
        }
    }
    fun removeSongFromFavorites(song: Song) {
        viewModelScope.launch {
            userRepository.removeSongFromFavorites(song.id)
        }
    }

    fun isSongFavorite(song: Song): Boolean {
        return userRepository.isSongFavorite(song.id)
    }
}