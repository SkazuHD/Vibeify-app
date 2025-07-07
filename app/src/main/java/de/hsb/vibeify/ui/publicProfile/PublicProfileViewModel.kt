package de.hsb.vibeify.ui.publicProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.FollowService
import de.hsb.vibeify.services.PlaylistService
import de.hsb.vibeify.services.PresenceService
import de.hsb.vibeify.ui.components.LiveFriends.LiveFriend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicProfileUiState(
    val user: User? = null,
    val playlists: List<Playlist> = emptyList(),
    val liveFriend: LiveFriend? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistService: PlaylistService,
    private val followService: FollowService,
    private val presenceService: PresenceService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState = _uiState.asStateFlow()

    val isFollowing = uiState
        .map { it.user?.id }
        .filterNotNull()
        .flatMapLatest { userId ->
            userRepository.state
                .map { it.currentUser?.id }
                .filterNotNull()
                .flatMapLatest { currentUserId ->
                    followService.isFollowingFlow(currentUserId, userId)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    val liveFriendFlow = uiState.map {
        it.user?.id
    }.filterNotNull().distinctUntilChanged().flatMapLatest { userId ->
        presenceService.getFriendFlow(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


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

    fun followUser(userId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.state.value.currentUser?.id ?: return@launch
                followService.followUser(currentUserId, userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.state.value.currentUser?.id ?: return@launch
                followService.unfollowUser(currentUserId, userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}