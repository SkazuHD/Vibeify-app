package de.hsb.vibeify.data.repository

import android.util.Log
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


data class UserRepositoryState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

interface UserRepository {

}

@Singleton
class UserRepositoryImpl
@Inject constructor(
    private val authRepository: AuthRepository, private val firestoreRepository: FirestoreRepo

) : UserRepository {

    private val _state = MutableStateFlow(UserRepositoryState())
    val state: StateFlow<UserRepositoryState> = _state

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
}