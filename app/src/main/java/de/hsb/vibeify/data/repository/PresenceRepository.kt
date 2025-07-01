package de.hsb.vibeify.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import de.hsb.vibeify.data.model.CurrentlyPlaying
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    companion object {
        private const val TAG = "PresenceRepository"
        private const val USERS_PATH = "users"
        private const val ONLINE_PATH = "online"
        private const val LAST_SEEN_PATH = "lastSeen"
        private const val CURRENTLY_PLAYING_PATH = "currentlyPlaying"
    }

    suspend fun setUserOnline(userId: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val userRef = database.getReference("$USERS_PATH/$userId")

            val updates = mapOf(
                ONLINE_PATH to true,
                LAST_SEEN_PATH to timestamp
            )

            userRef.updateChildren(updates).await()
            Log.d(TAG, "User $userId set to online")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set user online", e)
            Result.failure(e)
        }
    }

    suspend fun setUserOffline(userId: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            val userRef = database.getReference("$USERS_PATH/$userId")

            val updates = mapOf(
                ONLINE_PATH to false,
                LAST_SEEN_PATH to timestamp
            )

            userRef.updateChildren(updates).await()
            Log.d(TAG, "User $userId set to offline")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set user offline", e)
            Result.failure(e)
        }
    }

    suspend fun updateCurrentlyPlaying(userId: String, song: Song?): Result<Unit> {
        return try {
            val currentlyPlayingRef =
                database.getReference("$USERS_PATH/$userId/$CURRENTLY_PLAYING_PATH")

            if (song != null) {
                val playbackData = CurrentlyPlaying.from(song)
                currentlyPlayingRef.setValue(playbackData).await()
                Log.d(TAG, "Updated currently playing: ${song.name}")
            } else {
                currentlyPlayingRef.removeValue().await()
                Log.d(TAG, "Cleared currently playing")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update currently playing", e)
            Result.failure(e)
        }
    }

    fun setupOnDisconnect(userId: String) {
        try {
            val userRef = database.getReference("$USERS_PATH/$userId")
            val timestamp = System.currentTimeMillis()

            userRef.child(ONLINE_PATH).onDisconnect().setValue(false)
            userRef.child(LAST_SEEN_PATH).onDisconnect().setValue(timestamp)

            Log.d(TAG, "OnDisconnect handlers set up for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup onDisconnect handlers", e)
        }
    }
}
