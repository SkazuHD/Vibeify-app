package de.hsb.vibeify.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class
FollowType {
    FOLLOWING,
    FOLLOWERS
}

@HiltViewModel
class FollowDialogViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val followList = MutableStateFlow<List<User>>(emptyList())

    fun loadFollowList(userId: String, type: FollowType) {
        viewModelScope.launch {
            val flow = when (type) {
                FollowType.FOLLOWING -> userRepository.getFollowing(userId)
                FollowType.FOLLOWERS -> userRepository.getFollowers(userId)
            }
            followList.value = flow

        }
    }

}