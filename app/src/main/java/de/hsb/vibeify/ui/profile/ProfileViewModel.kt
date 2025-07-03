package de.hsb.vibeify.ui.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.FollowService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistRepository: PlaylistRepository,
    private val followService: FollowService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    var playlists by mutableStateOf(emptyList<Playlist>())
        private set

    val followersFlow = uiState
        .map { it.user?.id }
        .filterNotNull()
        .flatMapLatest { userId -> followService.followerList(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followingFlow = uiState
        .map { it.user?.id }
        .filterNotNull()
        .flatMapLatest { userId -> followService.followingList(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            launch {
                userRepository.state.collect { repositoryState ->
                    _uiState.value = ProfileUiState(
                        user = repositoryState.currentUser,
                        isLoading = repositoryState.isLoading,
                        error = repositoryState.error
                    )
                }
            }
            launch {
                userRepository.state.collect { userState ->
                    if (userState.currentUser != null) {
                        val userPlaylists =
                            playlistRepository.getPlaylistsByUserId(userState.currentUser.id)
                        playlists = userPlaylists
                    } else {
                        playlists = emptyList()
                    }
                }
            }
        }
    }

    fun onSave(name: String, url: String) {
        Log.d("ProfileViewModel", "onSave called with name: $name, url: $url")
        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch
            if (currentUser.name == name && url == currentUser.imageUrl) {
                Log.d("ProfileViewModel", "No changes to save")
                return@launch // No changes to save
            } else {
                var imageUrl = currentUser.imageUrl
                val uri = try {
                    url.toUri()
                } catch (_: Exception) {
                    null
                }
                if (uri != null && uri.toString().isNotEmpty()) {
                    imageUrl = userRepository.uploadPhoto(currentUser.id, url)
                }
                val updatedUser = currentUser.copy(name = name, imageUrl = imageUrl)
                userRepository.updateUser(updatedUser)
            }
        }
    }

    // Public Zugriff für FollowDialog
    fun getUserRepository() = userRepository
}