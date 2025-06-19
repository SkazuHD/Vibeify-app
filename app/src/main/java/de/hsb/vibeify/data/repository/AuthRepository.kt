package de.hsb.vibeify.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


interface AuthRepository {
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun signOut()
    val state: MutableStateFlow<AuthRepositoryState>
}

data class AuthRepositoryState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val isAuthResolved: Boolean = false,
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

        Log.d("FirebaseAuthRepo", "Auth state changed: ${user?.email}")

        state.update {
            it.copy(currentUser = user, isAuthResolved = true)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override suspend fun signIn(email: String, password: String) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                state.update {
                    Log.d("AuthDebug", "User signed in successfully: ${user.email}")
                    AuthRepositoryState(currentUser = user)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Login fehlgeschlagen")
        }
    }

    override suspend fun signUp(email: String, password: String) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                state.update {
                    Log.d("AuthDebug", "User registered and signed in successfully: ${user.email}")
                    AuthRepositoryState(currentUser = user)
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Registrierung fehlgeschlagen")
        }
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
            state.update {
                AuthRepositoryState(currentUser = null)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "Abmelden fehlgeschlagen")
        }
    }
}