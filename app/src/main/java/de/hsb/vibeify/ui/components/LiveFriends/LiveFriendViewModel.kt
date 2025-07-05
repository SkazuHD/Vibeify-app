package de.hsb.vibeify.ui.components.LiveFriends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.services.PresenceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class LiveFriend(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val isOnline: Boolean,
    val currentSong: String?,
    val email : String
)

data class LiveFriendUiState(
    val liveFriends: List<LiveFriend> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LiveFriendsViewModel @Inject constructor(
    private val presenceService: PresenceService

) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveFriendUiState())
    val uiState: StateFlow<LiveFriendUiState> = _uiState.asStateFlow()

    init {
        observeLiveFriends()
    }

    private fun observeLiveFriends() {
        viewModelScope.launch {
            presenceService.getLiveFriendsFlow().collect { friends ->
                _uiState.value = LiveFriendUiState(liveFriends = friends)
            }
        }
    }


}