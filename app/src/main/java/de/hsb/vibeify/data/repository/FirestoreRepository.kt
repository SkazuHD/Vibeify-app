package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.FirebaseFirestore as Firestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import de.hsb.vibeify.data.model.Song
import javax.inject.Inject
import javax.inject.Singleton

interface FirestoreRepo {
    fun getUsers(): Task<QuerySnapshot>
    fun getSongs(): Task<QuerySnapshot>
    fun getPlaylists(): Task<QuerySnapshot>
    fun insertUser(user: Map<String, Any>): Task<DocumentReference?>
    fun insertSong(song: Map<String, Any>): Task<DocumentReference?>
    fun insertPlaylist(playlist: List<Song>): Task<DocumentReference?>
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

    override fun insertUser(user: Map<String, Any>): Task<DocumentReference?> {
        return db.collection("users").add(user)
            .addOnSuccessListener { documentReference ->
                println("User added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding user: $e")
            }
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