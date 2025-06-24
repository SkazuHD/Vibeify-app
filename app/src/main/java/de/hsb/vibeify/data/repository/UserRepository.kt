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
    fun isPlaylistFavorite(string: String): Boolean
    suspend fun removePlaylistFromFavorites(string: String)
    suspend fun addPlaylistToFavorites(string: String)

    val state: StateFlow<UserRepositoryState>
}

@Singleton
class UserRepositoryImpl
@Inject constructor(
    private val authRepository: AuthRepository, private val firestoreRepository: FirestoreRepo

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

    override fun isPlaylistFavorite(string: String): Boolean {
        if (_state.value.currentUser == null) {
            return false
        }else{
            return _state.value.currentUser?.playlists?.contains(string) == true
        }
    }

    override suspend fun removePlaylistFromFavorites(string: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id).update("playlists", FieldValue.arrayRemove(string)).await()
    }

    override suspend fun addPlaylistToFavorites(playlistId: String) {
        val user = _state.value.currentUser ?: return
        db.collection(collectionName).document(user.id).update("playlists", FieldValue.arrayUnion(playlistId)).await()

    }
}