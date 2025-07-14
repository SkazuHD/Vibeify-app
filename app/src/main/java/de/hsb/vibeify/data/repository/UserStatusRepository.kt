package de.hsb.vibeify.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hsb.vibeify.data.model.CurrentlyPlaying
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


// user status repository for managing user presence, currently playing songs, and followers/following relationships
// the data is stored in Firebase Realtime Database
@Singleton
class UserStatusRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    companion object {
        private const val TAG = "PresenceRepository"
        private const val USERS_PATH = "users"
        private const val ONLINE_PATH = "online"
        private const val LAST_SEEN_PATH = "lastSeen"
        private const val CURRENTLY_PLAYING_PATH = "currentlyPlaying"
        private const val FOLLOWERS_PATH = "followers"
        private const val FOLLOWING_PATH = "following"
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
                val playbackData = song
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

    /**
     * Sets up onDisconnect handlers for the user to mark them as offline and update last seen time
     * This is useful for ensuring the user's status is updated even if they disconnect unexpectedly
     */
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

    suspend fun addFollower(userId: String, followerId: String): Result<Unit> {
        return try {
            val userRef = database.getReference("$USERS_PATH/$userId/$FOLLOWERS_PATH/$followerId")
            userRef.setValue(true).await()
            Log.d(TAG, "Added follower $followerId to user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add follower", e)
            Result.failure(e)
        }
    }

    suspend fun removeFollower(userId: String, followerId: String): Result<Unit> {
        return try {
            val userRef = database.getReference("$USERS_PATH/$userId/$FOLLOWERS_PATH/$followerId")
            userRef.removeValue().await()
            Log.d(TAG, "Removed follower $followerId from user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove follower", e)
            Result.failure(e)
        }
    }

    suspend fun addFollowing(userId: String, followingId: String): Result<Unit> {
        return try {
            val userRef = database.getReference("$USERS_PATH/$userId/$FOLLOWING_PATH/$followingId")
            userRef.setValue(true).await()
            Log.d(TAG, "Added following $followingId for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add following", e)
            Result.failure(e)
        }
    }

    suspend fun removeFollowing(userId: String, followingId: String): Result<Unit> {
        return try {
            val userRef = database.getReference("$USERS_PATH/$userId/$FOLLOWING_PATH/$followingId")
            userRef.removeValue().await()
            Log.d(TAG, "Removed following $followingId for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove following", e)
            Result.failure(e)
        }
    }

    suspend fun isUserOnline(userId: String): Boolean {
        return try {
            val userRef = database.getReference("$USERS_PATH/$userId/$ONLINE_PATH")
            val snapshot = userRef.get().await()
            snapshot.getValue(Boolean::class.java) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if user is online", e)
            false
        }
    }

    suspend fun getLastSeen(userId: String): Long {
        return try {
            val userRef = database.getReference("$USERS_PATH/$userId/$LAST_SEEN_PATH")
            val snapshot = userRef.get().await()
            snapshot.getValue(Long::class.java) ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last seen time", e)
            0L
        }
    }

    suspend fun getCurrentlyPlaying(userId: String): CurrentlyPlaying? {
        return try {
            val currentlyPlayingRef =
                database.getReference("$USERS_PATH/$userId/$CURRENTLY_PLAYING_PATH")
            val snapshot = currentlyPlayingRef.get().await()
            snapshot.getValue(CurrentlyPlaying::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get currently playing", e)
            null
        }
    }

    suspend fun getFollowers(userId: String): List<String> {
        return try {
            val followersRef = database.getReference("$USERS_PATH/$userId/$FOLLOWERS_PATH")
            val snapshot = followersRef.get().await()
            snapshot.children.mapNotNull { it.key }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get followers", e)
            emptyList()
        }
    }

    suspend fun getFollowing(userId: String?): List<String> {
        return try {
            val followingRef = database.getReference("$USERS_PATH/$userId/$FOLLOWING_PATH")
            val snapshot = followingRef.get().await()
            snapshot.children.mapNotNull { it.key }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get following", e)
            emptyList()
        }
    }

    suspend fun isFollowing(userId: String, targetUserId: String): Boolean {
        return try {
            val followingRef =
                database.getReference("$USERS_PATH/$userId/$FOLLOWING_PATH/$targetUserId")
            val snapshot = followingRef.get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if user is following", e)
            false
        }
    }

    suspend fun isFollower(userId: String, targetUserId: String): Boolean {
        return try {
            val followersRef =
                database.getReference("$USERS_PATH/$userId/$FOLLOWERS_PATH/$targetUserId")
            val snapshot = followersRef.get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if user is a follower", e)
            false
        }
    }

    // get followers and following as flows to observe changes in real-time for the live friends element in main view
    fun getFollowersFlow(userId: String): Flow<List<String>> = callbackFlow {
        val followersRef = database.getReference("$USERS_PATH/$userId/$FOLLOWERS_PATH")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val followers = snapshot.children.mapNotNull { it.key }
                trySend(followers)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        followersRef.addValueEventListener(listener)
        awaitClose { followersRef.removeEventListener(listener) }
    }

    fun getFollowingFlow(userId: String): Flow<List<String>> = callbackFlow {
        val followingRef = database.getReference("$USERS_PATH/$userId/$FOLLOWING_PATH")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val following = snapshot.children.mapNotNull { it.key }
                trySend(following)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        followingRef.addValueEventListener(listener)
        awaitClose { followingRef.removeEventListener(listener) }
    }

    // get currently playing song and online status as flows to observe changes in real-time
    fun getCurrentlyPlayingFlow(userId: String): Flow<Song?> = callbackFlow {
        val currentlyPlayingRef =
            database.getReference("$USERS_PATH/$userId/$CURRENTLY_PLAYING_PATH")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentlyPlaying = snapshot.getValue(Song::class.java)
                trySend(currentlyPlaying)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        currentlyPlayingRef.addValueEventListener(listener)
        awaitClose { currentlyPlayingRef.removeEventListener(listener) }
    }

    fun getOnlineStatusFlow(userId: String): Flow<Boolean> = callbackFlow {
        val onlineRef = database.getReference("$USERS_PATH/$userId/$ONLINE_PATH")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                trySend(isOnline)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        onlineRef.addValueEventListener(listener)
        awaitClose { onlineRef.removeEventListener(listener) }
    }

    fun getLastSeenFlow(userId: String): Flow<Long> = callbackFlow {
        val lastSeenRef = database.getReference("$USERS_PATH/$userId/$LAST_SEEN_PATH")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lastSeen = snapshot.getValue(Long::class.java) ?: 0L
                trySend(lastSeen)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        lastSeenRef.addValueEventListener(listener)
        awaitClose { lastSeenRef.removeEventListener(listener) }
    }


}
