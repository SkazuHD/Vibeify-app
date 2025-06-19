package de.hsb.vibeify.data.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore as Firestore

interface FirestoreRepo {
    fun getUsers(): Task<QuerySnapshot>
    fun getSongs(): Task<QuerySnapshot>
    fun getPlaylists(): Task<QuerySnapshot>
    suspend fun insertUser(user: User): Task<QuerySnapshot>
    fun insertSong(song: Map<String, Any>): Task<DocumentReference?>
    fun insertPlaylist(playlist: List<Song>): Task<DocumentReference?>
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

    override fun getSongs(): Task<QuerySnapshot> {
        return db.collection("songs").get()
            .addOnSuccessListener { querySnapshot ->
                println("Songs retrieved successfully: ${querySnapshot.documents.size} songs found.")
            }
            .addOnFailureListener { e ->
                println("Error retrieving songs: $e")
            }
    }

    override fun getPlaylists(): Task<QuerySnapshot> {
        return db.collection("playlists").get()
            .addOnSuccessListener { querySnapshot ->
                println("Playlists retrieved successfully: ${querySnapshot.documents.size} playlists found.")
            }
            .addOnFailureListener { e ->
                println("Error retrieving playlists: $e")
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

    override fun insertSong(song: Map<String, Any>): Task<DocumentReference?> {
        return db.collection("songs").add(song)
            .addOnSuccessListener { documentReference ->
                println("Song added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding song: $e")
            }
    }

    override fun insertPlaylist(playlist: List<Song>): Task<DocumentReference?> {
        return db.collection("playlists").add(playlist)
            .addOnSuccessListener { documentReference ->
                println("Playlist added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding playlist: $e")
            }
    }
}