package de.hsb.vibeify.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import de.hsb.vibeify.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore as Firestore



data class UserRepositoryState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

interface UserRepository {
    fun isPlaylistFavorite(playlistId: String): Boolean
    suspend fun removePlaylistFromFavorites(playlistId: String)
    suspend fun addPlaylistToFavorites(playlistId: String)
    fun isSongFavorite(songId: String): Boolean
    suspend fun removeSongFromFavorites(songId: String)
    suspend fun addSongToFavorites(songId: String)
    fun getLikedSongIds(): List<String>
    suspend fun updateUser(user: User) {
    }

    val state: StateFlow<UserRepositoryState>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    authRepository: AuthRepository, firestoreRepository: FirestoreRepo
) : UserRepository {

    private val db = Firestore.getInstance()
    private val collectionName = "users"

    private val _state = MutableStateFlow(UserRepositoryState())

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
                    var maybeUser =
                        firestoreRepository.getUserById(authState.currentUser.uid).await()
                    Log.d(
                        "UserRepository",
                        "User query result: ${maybeUser.documents.size} documents found."
                    )
                    if (maybeUser.isEmpty) {
                        var docRef = firestoreRepository.insertUser(
                            User(
                                id = authState.currentUser.uid,
                                email = authState.currentUser.email ?: "unknown",
                                name = authState.currentUser.displayName ?: "unknown",
                                imageUrl = authState.currentUser.photoUrl?.toString() ?: "unknown",
                            )
                        ).await()
                        val userSnapshot = docRef.documents.firstOrNull()
                        var user = userSnapshot?.toObject(User::class.java)

                        _state.value = UserRepositoryState(currentUser = user)
                    } else {
                        val userSnapshot = maybeUser.documents.firstOrNull()
                        val user = userSnapshot?.toObject(User::class.java)
                        _state.value = UserRepositoryState(currentUser = user)
                    }

                }

            }
        }
    }

    override fun getLikedSongIds(): List<String> {
        Log.d("UserRepository", "getLikedSongIds called")
        return _state.value.currentUser?.likedSongs ?: emptyList()
    }

    override fun isPlaylistFavorite(playlistId: String): Boolean {
        if (_state.value.currentUser == null) {
            return false
        }else{
            return _state.value.currentUser?.playlists?.contains(playlistId) == true
        }
    }

    override suspend fun removePlaylistFromFavorites(playlistId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id).update("playlists", FieldValue.arrayRemove(playlistId)).await()
        _state.value = _state.value.copy(
            currentUser = user.copy(playlists = user.playlists.filter { it != playlistId })
        )
    }

    override suspend fun addPlaylistToFavorites(playlistId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id).update("playlists", FieldValue.arrayUnion(playlistId)).await()
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
        db.collection(collectionName).document(user.id).update("likedSongs", FieldValue.arrayRemove(songId)).await()
        _state.value = _state.value.copy(
            currentUser = user.copy(likedSongs = user.likedSongs.filter { it != songId })
        )
    }

    override suspend fun addSongToFavorites(songId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id).update("likedSongs", FieldValue.arrayUnion(songId)).await()
        _state.value = _state.value.copy(
            currentUser = user.copy(likedSongs = user.likedSongs + songId)
        )
    }

    override suspend fun updateUser(user: User) {
        Log.d("UserRepository", "updateUser called with user: $user")
        db.collection(collectionName).document(user.id).set(user).await()
        _state.value = _state.value.copy(currentUser = user)
    }
}