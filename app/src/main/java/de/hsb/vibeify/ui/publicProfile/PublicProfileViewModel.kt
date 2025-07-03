package de.hsb.vibeify.ui.publicProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicProfileUiState(
    val user: User? = null,
    val playlists: List<Playlist> = emptyList(), // Assuming playlists are represented by their IDs
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistService: PlaylistService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState = _uiState.asStateFlow()


     fun loadUser(userId: String) {
         viewModelScope.launch {
             _uiState.value = PublicProfileUiState(isLoading = true)
             try {
                 val user = userRepository.getUserById(userId)
                 _uiState.value = PublicProfileUiState(user = user, isLoading = false)
             } catch (e: Exception) {
                 _uiState.value = PublicProfileUiState(error = e.message, isLoading = false)
             }
         }
     }

    fun loadPlaylists(userId: String) {
        viewModelScope.launch {
            try {
                val playlists = playlistService.getPlaylistCreatedByUser(userId)
                _uiState.value = _uiState.value.copy(playlists = playlists)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}