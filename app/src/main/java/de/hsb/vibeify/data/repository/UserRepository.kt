package de.hsb.vibeify.data.repository

import com.google.firebase.auth.FirebaseAuth
import de.hsb.vibeify.data.model.User
import javax.inject.Inject


interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String): Result<User>
    suspend fun signOut()
    fun getCurrentUser(): User?
}

class FirebaseAuthRepo : AuthRepository {

    @Inject
    constructor()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signIn(email: String, password: String): Result<User> {
        return Result.success(User("123", "test", "", null))
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        return Result.success(User("123", "test", "", null))
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): User? {
        return null
        //return auth.currentUser?.let { User(it.uid, it.email.toString(), "") }
    }


}