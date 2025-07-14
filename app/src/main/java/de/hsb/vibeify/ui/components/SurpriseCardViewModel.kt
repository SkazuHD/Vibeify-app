package de.hsb.vibeify.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.services.RandomSongService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the state of the SurpriseCard.
 *
 * This ViewModel interacts with the RandomSongService to fetch a random song
 * and provides state flows for the current random song, loading state, and error messages.
 *
 * @property randomSongManager The service used to fetch random songs.
 */
@HiltViewModel
class SurpriseCardViewModel @Inject constructor(
    private val randomSongManager: RandomSongService
) : ViewModel() {

    val randomSong: StateFlow<Song?> = randomSongManager.currentRandomSong

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadRandomSong() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                randomSongManager.refreshRandomSong()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load random song"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
