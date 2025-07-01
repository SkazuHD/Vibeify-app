package de.hsb.vibeify.services

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.Firebase
import com.google.firebase.database.database
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceService @Inject constructor(
    private val userRepository: UserRepository,
    private val playerService: PlayerServiceV2
) : DefaultLifecycleObserver {
    private val database =
        Firebase.database(" https://vibeify-16f99-default-rtdb.europe-west1.firebasedatabase.app")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentUserId: String? = null
    private var isAppInForeground = false
    private var isMusicPlaying = false

    init {
        database.setPersistenceEnabled(true)
        Log.d("PresenceService", "Firebase Realtime Database persistence enabled")

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        scope.launch {
            userRepository.state.map { it.currentUser }.collect { user ->
                currentUserId = user?.id
                if (user != null) {
                    updateOnlineStatus()
                    setupOnDisconnect(user.id)
                }
            }
        }

        scope.launch {
            playerService.isPlaying.collect { playing ->
                isMusicPlaying = playing
                Log.d("PresenceService", "Music playing state changed: $playing")
                currentUserId?.let { updateOnlineStatus() }
            }
        }

        scope.launch {
            playerService.currentSong.collect { song ->
                currentUserId?.let { userId ->
                    updateCurrentlyPlaying(userId, song)
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("PresenceService", "App moved to foreground")
        isAppInForeground = true
        currentUserId?.let { updateOnlineStatus() }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("PresenceService", "App moved to background")
        isAppInForeground = false
        currentUserId?.let { updateOnlineStatus() }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d("PresenceService", "App destroyed")
        currentUserId?.let { setUserOffline(it) }
    }

    private fun updateOnlineStatus() {
        currentUserId?.let { userId ->
            val shouldBeOnline = isAppInForeground || isMusicPlaying

            if (shouldBeOnline) {
                setUserOnline(userId)
            } else {
                setUserOffline(userId)
            }
        }
    }

    private fun updateCurrentlyPlaying(userId: String, song: Song?) {
        val currentlyPlayingRef = database.getReference("users/$userId/currentlyPlaying")
        Log.d("PresenceService", "Updating currently playing for user: $userId")
        Log.d("PresenceService", "Is music playing: $isMusicPlaying")
        Log.d("PresenceService", "Current song: ${song?.name ?: "None"}")
        if (song != null) {
            val playbackData = mapOf(
                "songId" to song.id,
                "songName" to song.name,
                "artist" to song.artist,
                "album" to song.album,
                "imageUrl" to song.imageUrl,
                "startTime" to System.currentTimeMillis(),
                "isPlaying" to true
            )
            currentlyPlayingRef.setValue(playbackData)
            Log.d("PresenceService", "Updated currently playing: ${song.name}")
        } else {
            currentlyPlayingRef.removeValue()
            Log.d("PresenceService", "Cleared currently playing")
        }
    }

    private fun setupOnDisconnect(userId: String) {
        val userRef = database.getReference("users/$userId/online")
        val lastSeenRef = database.getReference("users/$userId/lastSeen")

        userRef.onDisconnect().setValue(false)
        lastSeenRef.onDisconnect().setValue(System.currentTimeMillis())
    }

    fun setUserOnline(userId: String) {
        val userRef = database.getReference("users/$userId/online")
        val lastSeenRef = database.getReference("users/$userId/lastSeen")

        userRef.setValue(true)
        lastSeenRef.setValue(System.currentTimeMillis())

        Log.d("PresenceService", "User $userId set to online")
    }

    fun setUserOffline(userId: String) {
        val userRef = database.getReference("users/$userId/online")
        val lastSeenRef = database.getReference("users/$userId/lastSeen")

        userRef.setValue(false)
        lastSeenRef.setValue(System.currentTimeMillis())

        Log.d("PresenceService", "User $userId set to offline")
    }

    fun cleanup() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        currentUserId?.let { setUserOffline(it) }
    }
}