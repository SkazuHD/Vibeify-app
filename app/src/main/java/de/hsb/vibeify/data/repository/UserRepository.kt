package de.hsb.vibeify.data.repository

import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import de.hsb.vibeify.api.generated.apis.DefaultApi
import de.hsb.vibeify.data.model.RecentActivity
import de.hsb.vibeify.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

// Represents the state of the UserRepository

data class UserRepositoryState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

interface UserRepository {

    suspend fun getUsers(): List<User>
    suspend fun insertUser(user: User): User?
    suspend fun getUserById(userId: String): User?

    suspend fun searchUsers(query: String): List<User>
    fun isPlaylistFavorite(playlistId: String): Boolean
    suspend fun removePlaylistFromFavorites(playlistId: String)
    suspend fun addPlaylistToFavorites(playlistId: String)
    fun isSongFavorite(songId: String): Boolean
    suspend fun removeSongFromFavorites(songId: String)
    suspend fun addSongToFavorites(songId: String)
    fun getLikedSongIds(): List<String>
    suspend fun updateUser(user: User?)
    suspend fun uploadPhoto(id: String, imageUrl: String): String
    suspend fun addRecentSearch(searchTerm: String)
    suspend fun addRecentActivity(activity: RecentActivity)
    val state: StateFlow<UserRepositoryState>
}


/**Implementation of UserRepository that interacts with Firebase Firestore and retrofit to store images on our server.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val context: android.content.Context,
    private val db: FirebaseFirestore,
    private val webService: DefaultApi
) : UserRepository {

    private val collectionName = "users"
    private val _state = MutableStateFlow(UserRepositoryState())

    companion object {
        private const val MAX_RECENT_ACTIVITIES = 30
    }

    override val state: StateFlow<UserRepositoryState> = _state

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Initialize the repository and collect authentication state. Collect authentication data to create our own user in firestore.
    //This is done, so the we can work with the user data without manipulating the auth user data.

    init {
        Log.d("UserRepository", "init block entered")
        scope.launch {
            authRepository.state.collect { authState ->
                Log.d("UserRepository", "collect: " + authState.currentUser)
                if (authState.currentUser == null) {
                    _state.value = UserRepositoryState(currentUser = null)
                } else {
                    var user = getUserById(authState.currentUser.uid)
                    if (user == null) {
                        var user = insertUser(
                            User(
                                id = authState.currentUser.uid,
                                email = authState.currentUser.email ?: "",
                                name = authState.currentUser.displayName
                                    ?: authState.currentUser.email?.split("@")?.firstOrNull() ?: "",
                                imageUrl = authState.currentUser.photoUrl?.toString() ?: "",
                            )
                        )
                        _state.value = UserRepositoryState(currentUser = user)
                    } else {
                        _state.value = UserRepositoryState(currentUser = user)

                    }
                }
            }
        }
    }

    override suspend fun getUsers(): List<User> {
        val res = db.collection(collectionName).get()
            .addOnSuccessListener { querySnapshot ->
                println("Users retrieved successfully: ${querySnapshot.documents.size} users found.")
            }
            .addOnFailureListener { e ->
                println("Error retrieving users: $e")
            }.await()
        return res.toObjects(User::class.java)
    }

    override suspend fun getUserById(userId: String): User? {
        val res = db.collection(collectionName).whereEqualTo("id", userId).get()
            .addOnSuccessListener { querySnapshot ->
                println("Users retrieved successfully")
            }
            .addOnFailureListener { e ->
                println("Error retrieving users: $e")
            }.await()
        return res.firstOrNull()?.toObject(User::class.java)
    }


    override suspend fun insertUser(user: User): User? {
        Log.d("FirestoreRepository", "insertUser called for user: ${user.id}, ${user.email}")
        db.collection("users").document(user.id).set(user)
            .addOnSuccessListener { _ ->
                Log.d("FirestoreRepository", "User added with ID: ${user.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Error adding user: $e")
            }.await()
        return getUserById(user.id)
    }

    override suspend fun searchUsers(query: String): List<User> {
        Log.d("UserRepository", "searchUsers called with query: $query")
        val res = db.collection(collectionName).where(
            Filter.or(
                Filter.and(
                    Filter.greaterThanOrEqualTo("email", query),
                    Filter.lessThanOrEqualTo("email", query + "\uf8ff")
                ),
                Filter.and(
                    Filter.greaterThanOrEqualTo("name", query),
                    Filter.lessThanOrEqualTo("name", query + "\uf8ff")
                )
            )
        ).get().await()
        Log.d("UserRepository", "searchUsers result size: ${res.size()}")
        if (res.isEmpty) {
            return emptyList()
        }

        return res.documents.mapNotNull { it.toObject(User::class.java) }
    }

    override fun getLikedSongIds(): List<String> {
        Log.d("UserRepository", "getLikedSongIds called")
        return _state.value.currentUser?.likedSongs ?: emptyList()
    }


    //see if a playlist is favorite by checking if the playlistId is in the user's playlists list to show the heart emoji.
    override fun isPlaylistFavorite(playlistId: String): Boolean {
        if (_state.value.currentUser == null) {
            return false
        } else {
            return _state.value.currentUser?.playlists?.contains(playlistId) == true
        }
    }

    override suspend fun removePlaylistFromFavorites(playlistId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id)
            .update("playlists", FieldValue.arrayRemove(playlistId)).await()
        _state.value = _state.value.copy(
            currentUser = user.copy(playlists = user.playlists.filter { it != playlistId })
        )
    }

    override suspend fun addPlaylistToFavorites(playlistId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id)
            .update("playlists", FieldValue.arrayUnion(playlistId)).await()
        _state.value = _state.value.copy(
            currentUser = user.copy(playlists = user.playlists + playlistId)
        )
    }

    //see if a song is favorite by checking if the songId is in the user's likedSongs list to show the heart emoji.
    override fun isSongFavorite(songId: String): Boolean {
        if (_state.value.currentUser == null) {
            return false
        } else {
            return _state.value.currentUser?.likedSongs?.contains(songId) == true
        }

    }

    override suspend fun removeSongFromFavorites(songId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id)
            .update("likedSongs", FieldValue.arrayRemove(songId)).await()
        _state.value = _state.value.copy(
            currentUser = user.copy(likedSongs = user.likedSongs.filter { it != songId })
        )
    }

    override suspend fun addSongToFavorites(songId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id)
            .update("likedSongs", FieldValue.arrayUnion(songId)).await()
        _state.value = _state.value.copy(
            currentUser = user.copy(likedSongs = user.likedSongs + songId)
        )
    }

    override suspend fun updateUser(user: User?) {
        if (user == null) {
            Log.e("UserRepository", "updateUser called with null user")
            return
        }
        Log.d("UserRepository", "updateUser called with user: $user")
        db.collection(collectionName).document(user.id).set(user).await()
        _state.value = _state.value.copy(currentUser = user)
    }


    /**
     * Uploads a photo to the server and updates the user's profile picture URL.
     * Since the location of the image is already know, it is not fetched here, but already assigned to the user.
     * @param id The user ID to associate with the uploaded photo.
     * @param imageUrl The URI of the image to upload.
     * @return The URL of the uploaded photo or an empty string if the upload failed.
     */
    override suspend fun uploadPhoto(id: String, imageUrl: String): String {
        val uri = try {
            imageUrl.toUri()
        } catch (e: Exception) {
            null
        }
        val inputStream = context.contentResolver.openInputStream(uri!!)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes == null) {
            Log.e("UserRepository", "Failed to read bytes from image URI: $uri")
            return ""
        }
        Log.d("UserRepository", "uploadPhoto called with id: $id, imageUrl: $imageUrl")
        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, bytes.size)
        val part = MultipartBody.Part.createFormData("file", "profile.jpg", requestBody)
        val call =
            webService.uploadProfilePictureUploadProfilePictureUserIdPost(id, part).awaitResponse()
        if (call.isSuccessful) {
            Log.d("UserRepository", "Photo uploaded successfully for user: $id")
            _state.value.currentUser?.copy(imageUrl = "https://vibeify-app.skazu.net/picture/$id")
            return "https://vibeify-app.skazu.net/picture/$id"
        } else {
            Log.e("UserRepository", "Failed to upload photo: ${call.errorBody()?.string()}")
            return ""
        }
    }


    /**
     * Adds a recent search term to the user's recent searches.
     * If the user is not logged in, this operation will not be performed.
     * @param searchTerm The search term to add to the user's recent searches.
     */
    override suspend fun addRecentSearch(searchTerm: String) {
        Log.d("UserRepository", "addRecentSearch called with term: $searchTerm")
        _state.update { currentState ->
            val user = currentState.currentUser ?: return@update currentState

            val updatedSearches = (user.recentSearches + searchTerm)
                .takeLast(20)
                .distinct()

            db.collection(collectionName).document(user.id).update(
                "recentSearches", updatedSearches
            ).await()

            currentState.copy(
                currentUser = currentState.currentUser.copy(
                    recentSearches = updatedSearches
                )
            )
        }
    }

    /**
     * Adds a recent activity to the user's recent activities.
     * If the user is not logged in, this operation will not be performed.
     * @param activity The recent activity to add to the user's recent activities.
     */
    override suspend fun addRecentActivity(activity: RecentActivity) {
        _state.update { currentState ->
            Log.d("UserRepository", "addRecentActivity called with activity: $activity")
            val user = currentState.currentUser ?: return@update currentState

            val updatedActivities = (user.recentActivities + activity)
                .sortedByDescending { it.timestamp }
                .take(MAX_RECENT_ACTIVITIES)

            db.collection(collectionName).document(user.id).update(
                "recentActivities", updatedActivities
            ).await()

            currentState.copy(
                currentUser = currentState.currentUser.copy(
                    recentActivities = updatedActivities
                )
            )
        }
    }

}