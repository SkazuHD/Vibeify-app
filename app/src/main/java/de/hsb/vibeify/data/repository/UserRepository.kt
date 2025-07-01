package de.hsb.vibeify.data.repository

import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hsb.vibeify.api.generated.apis.DefaultApi
import de.hsb.vibeify.api.retrofit2.src.main.kotlin.de.hsb.vibeify.api.generated.infrastructure.ApiClient
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


data class UserRepositoryState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

interface UserRepository {

    suspend fun getUsers(): List<User>
    suspend fun insertUser(user: User): User?
    suspend fun getUserById(userId: String): User?
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

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val context: android.content.Context,
    private val db: FirebaseFirestore
) : UserRepository {

    private val apiClient = ApiClient(baseUrl = "https://vibeify-app.skazu.net/")
    val webService = apiClient.createService(DefaultApi::class.java)

    private val collectionName = "users"
    private val _state = MutableStateFlow(UserRepositoryState())

    companion object {
        private const val MAX_RECENT_ACTIVITIES = 20
    }

    override val state: StateFlow<UserRepositoryState> = _state

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
                                email = authState.currentUser.email ?: "unknown",
                                name = authState.currentUser.displayName ?: "unknown",
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

    override fun getLikedSongIds(): List<String> {
        Log.d("UserRepository", "getLikedSongIds called")
        return _state.value.currentUser?.likedSongs ?: emptyList()
    }

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


    override suspend fun addRecentSearch(searchTerm: String) {
        Log.d("UserRepository", "addRecentSearch called with term: $searchTerm")
        _state.update { currentState ->
            val user = currentState.currentUser ?: return@update currentState

            val updatedSearches = (user.recentSearches + searchTerm)
                .distinct()
                .takeLast(10)

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