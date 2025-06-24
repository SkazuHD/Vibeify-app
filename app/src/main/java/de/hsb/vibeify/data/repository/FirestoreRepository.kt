package de.hsb.vibeify.data.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import de.hsb.vibeify.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore as Firestore

interface FirestoreRepo {
    fun getUsers(): Task<QuerySnapshot>
    suspend fun insertUser(user: User): Task<QuerySnapshot>
    fun getUserById(userId: String): Task<QuerySnapshot>
}

@Singleton
class FirebaseRepository @Inject constructor() : FirestoreRepo {
    private val db = Firestore.getInstance()

    override fun getUsers(): Task<QuerySnapshot> {
        return db.collection("users").get()
            .addOnSuccessListener { querySnapshot ->
                println("Users retrieved successfully: ${querySnapshot.documents.size} users found.")
            }
            .addOnFailureListener { e ->
                println("Error retrieving users: $e")
            }
    }

    override fun getUserById(userId: String): Task<QuerySnapshot> {
        return db.collection("users").whereEqualTo("id", userId).get()
            .addOnSuccessListener { querySnapshot ->
                println("Users retrieved successfully")
            }
            .addOnFailureListener { e ->
                println("Error retrieving users: $e")
            }
    }


    override suspend fun insertUser(user: User): Task<QuerySnapshot> {
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


}