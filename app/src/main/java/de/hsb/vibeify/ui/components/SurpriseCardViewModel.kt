package de.hsb.vibeify.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurpriseCardViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _randomSong = songRepository.currentRandomSong
    val randomSong: StateFlow<Song?> = _randomSong

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadRandomSong() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                songRepository.refreshRandomSong()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load random song"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
