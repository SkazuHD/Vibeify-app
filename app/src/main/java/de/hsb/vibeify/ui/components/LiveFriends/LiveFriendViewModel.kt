package de.hsb.vibeify.ui.components.LiveFriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.services.PresenceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// Represents a live friend with their details.
data class LiveFriend(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val isOnline: Boolean,
    val currentSong: Song?,
    val email: String,
    val lastSeen: Long,
)

// Represents the UI state for live friends, including a list of friends, loading state, and error message.
data class LiveFriendUiState(
    val liveFriends: List<LiveFriend> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)


// ViewModel for managing the state of live friends.

@HiltViewModel
class LiveFriendsViewModel @Inject constructor(
    private val presenceService: PresenceService

) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LiveFriendUiState(
            isLoading = true,
            error = null
        )
    )
    val uiState: StateFlow<LiveFriendUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = LiveFriendUiState(isLoading = true)
        observeLiveFriends()
    }

    // Observes the live friends from the PresenceService and updates the UI state accordingly.
    private fun observeLiveFriends() {
        viewModelScope.launch {
            presenceService.getLiveFriendsFlow().map { it ->
                it.sortedByDescending { it.lastSeen }.distinctBy { it.id }
            }.distinctUntilChanged().collect { friends ->
                _uiState.update {
                    it.copy(liveFriends = friends, isLoading = false, error = null)
                }
            }
        }
    }


}