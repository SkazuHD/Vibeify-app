package de.hsb.vibeify.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.FollowService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class
FollowType {
    FOLLOWING,
    FOLLOWERS
}

@HiltViewModel
class FollowDialogViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val followService: FollowService
) : ViewModel() {

    private val _followList = MutableStateFlow<List<User>>(emptyList())
    val followList: StateFlow<List<User>> = _followList.asStateFlow()

    // Function to load the follow list based on user ID and type
    fun loadFollowList(userId: String, type: FollowType) {
        viewModelScope.launch {
            val userIds = when (type) {
                FollowType.FOLLOWING -> followService.getFollowing(userId)
                FollowType.FOLLOWERS -> followService.getFollowers(userId)
            }

            // Convert user IDs to User objects
            val users = userIds.mapNotNull { userId ->
                try {
                    userRepository.getUserById(userId)
                } catch (e: Exception) {
                    null
                }
            }

            _followList.value = users
        }
    }

}