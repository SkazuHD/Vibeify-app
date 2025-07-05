package de.hsb.vibeify.services

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.media3.common.util.UnstableApi
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.data.repository.UserStatusRepository
import de.hsb.vibeify.ui.components.LiveFriends.LiveFriend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceService @OptIn(UnstableApi::class)
@Inject constructor(
    private val userRepository: UserRepository,
    private val playerService: PlayerServiceV2,
    private val presenceRepository: UserStatusRepository
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "PresenceService"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentUserId: String? = null
    private var isAppInForeground = false
    private var isMusicPlaying = false
    private var isInitialized = false

    init {
        initialize()
    }

    private fun initialize() {
        if (isInitialized) return

        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            observeUserState()
            observePlayerState()
            observeCurrentSong()
            isInitialized = true
            Log.d(TAG, "PresenceService initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PresenceService", e)
        }
    }

    private fun observeUserState() {
        scope.launch {
            try {
                userRepository.state.map { it.currentUser }.collect { user ->
                    val previousUserId = currentUserId
                    currentUserId = user?.id

                    if (user != null && previousUserId != user.id) {
                        updateOnlineStatus()
                        presenceRepository.setupOnDisconnect(user.id)
                        Log.d(TAG, "User changed to: ${user.id}")
                    } else if (user == null && previousUserId != null) {
                        handleUserLogout(previousUserId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing user state", e)
            }
        }
    }

    private fun observePlayerState() {
        scope.launch {
            try {
                playerService.isPlaying.collect { playing ->
                    val previousState = isMusicPlaying
                    isMusicPlaying = playing

                    if (previousState != playing) {
                        Log.d(TAG, "Music playing state changed: $playing")
                        currentUserId?.let { updateOnlineStatus() }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing player state", e)
            }
        }
    }

    private fun observeCurrentSong() {
        scope.launch {
            try {
                playerService.currentSong.collect { song ->
                    currentUserId?.let { userId ->
                        updateCurrentlyPlaying(userId, song)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing current song", e)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App moved to foreground")
        isAppInForeground = true
        currentUserId?.let { updateOnlineStatus() }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App moved to background")
        isAppInForeground = false
        currentUserId?.let { updateOnlineStatus() }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d(TAG, "App destroyed")
        currentUserId?.let { userId ->
            scope.launch {
                setUserOffline(userId)
                updateCurrentlyPlaying(userId, null)
            }
        }
        cleanup()
    }

    private fun updateOnlineStatus() {
        currentUserId?.let { userId ->
            scope.launch {
                try {
                    val shouldBeOnline = isAppInForeground || isMusicPlaying

                    val result = if (shouldBeOnline) {
                        presenceRepository.setUserOnline(userId)
                    } else {
                        presenceRepository.setUserOffline(userId)
                    }

                    result.onFailure { exception ->
                        Log.e(TAG, "Failed to update online status", exception)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating online status", e)
                }
            }
        }
    }

    private fun updateCurrentlyPlaying(userId: String, song: Song?) {
        scope.launch {
            try {
                val result = presenceRepository.updateCurrentlyPlaying(userId, song)
                result.onFailure { exception ->
                    Log.e(TAG, "Failed to update currently playing", exception)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating currently playing", e)
            }
        }
    }

    private suspend fun setUserOffline(userId: String) {
        try {
            val result = presenceRepository.setUserOffline(userId)
            result.onFailure { exception ->
                Log.e(TAG, "Failed to set user offline", exception)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting user offline", e)
        }
    }

    private fun handleUserLogout(userId: String) {
        scope.launch {
            try {
                setUserOffline(userId)
                Log.d(TAG, "User $userId logged out, set to offline")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling user logout", e)
            }
        }
    }


    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    fun getLiveFriendsFlow(): Flow<List<LiveFriend>> = flow {
        userRepository.state.map { it.currentUser }.flatMapLatest { user ->
            flow {
                if (user == null) {
                    emit(emptyList<LiveFriend>())
                    return@flow
                }
                val friendIds = presenceRepository.getFollowing(user.id)
                val friends = friendIds.mapNotNull { friendId ->
                    val friendUser = userRepository.getUserById(friendId)
                    friendUser?.let {
                        val isOnline = presenceRepository.isUserOnline(friendId)
                        val currentlyPlaying = presenceRepository.getCurrentlyPlaying(friendId)
                        LiveFriend(
                            id = it.id,
                            name = it.name,
                            imageUrl = it.imageUrl,
                            isOnline = isOnline,
                            currentSong = currentlyPlaying?.songName ?: "No Song currently playing",
                            email = it.email
                        )
                    }
                }
                emit(friends)
            }
        }.collect { emit(it) }
    }

    fun cleanup() {
        try {
            if (isInitialized) {
                ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
                currentUserId?.let { userId ->
                    scope.launch {
                        setUserOffline(userId)
                    }
                }
                scope.cancel()
                isInitialized = false
                Log.d(TAG, "PresenceService cleaned up")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}