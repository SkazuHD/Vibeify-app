package de.hsb.vibeify.data.repository

import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hsb.vibeify.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


interface AuthRepository {
    suspend fun signIn(email: String, password: String): Unit
    suspend fun signUp(email: String, password: String): Unit
    suspend fun signOut()
    fun getCurrentUser(): User?
    val state: MutableStateFlow<AuthRepositoryState>
}

data class AuthRepositoryState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val isAuthResolved: Boolean = false, // <--- NEU: Auth-Status initial geladen?
    val error: String? = null
)
@Singleton
class FirebaseAuthRepo : AuthRepository {

    @Inject
    constructor()

    override val state = MutableStateFlow(AuthRepositoryState())

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        state.update {
            it.copy(currentUser = user, isAuthResolved = true)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override suspend fun signIn(email: String, password: String): Unit {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        state.update {
                            Log.d("AuthDebug", "User signed in successfully: ${user.email}")
                            AuthRepositoryState(currentUser = auth.currentUser)
                        }
                    }
                } else {
                    throw task.exception ?: Exception("Login failed")
                }
            }.await() // Wait for the task to complete
    }

    override suspend fun signUp(email: String, password: String): Unit {
        Log.d("AuthDebug", "Attempting to register user with email: '$email' and password: '$password'")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        state.update {
                            Log.d("AuthDebug", "User signed in successfully: ${user.email}")
                            AuthRepositoryState(currentUser = auth.currentUser)
                        }
                    }
                } else {
                    throw task.exception ?: Exception("Registration failed")
                }
            }.await() // Wait for the task to complete
    }

    override suspend fun signOut() {
        auth.signOut()
        state.update {
            AuthRepositoryState(currentUser = null)
        }
    }

    override fun getCurrentUser(): User? {
        return null
        //return auth.currentUser?.let { User(it.uid, it.email.toString(), "") }
    }


}