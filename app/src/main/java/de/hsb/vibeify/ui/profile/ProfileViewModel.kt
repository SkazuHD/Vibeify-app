package de.hsb.vibeify.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import de.hsb.vibeify.data.model.Playlist
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import kotlin.math.log


data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    var playlists by mutableStateOf(emptyList<Playlist>())
        private set

    init {
        viewModelScope.launch {
            launch {
                userRepository.state.collect {
                    userRepository.state.collect { repositoryState ->
                        _uiState.value = ProfileUiState(
                            user = repositoryState.currentUser,
                            isLoading = repositoryState.isLoading,
                            error = repositoryState.error
                        )
                    }
                }
            }
            launch {
                userRepository.state.collect { userState ->
                    if (userState.currentUser != null) {
                        val userPlaylists = playlistRepository.getPlaylistsByUserId(userState.currentUser.id)

                        playlists = userPlaylists
                    } else {
                        playlists = emptyList()
                    }

                }
            }
        }
    }

    fun onSave(name: String, url: String) {

        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch
            if (currentUser.name == name ) {
                Log.d("ProfileViewModel", "No changes to save")
                return@launch // No changes to save
            }
            val updatedUser = currentUser.copy(name = name)
            userRepository.updateUser(updatedUser)
        }
    }
}