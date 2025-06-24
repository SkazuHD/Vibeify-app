package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.FieldValue
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore as Firestore

interface PlaylistRepository {
    suspend fun getPlaylistById(id: String): Playlist?
    suspend fun getAllPlaylists(): List<Playlist> {
        return emptyList()
    }
    suspend fun searchPlaylists(query: String): List<Playlist> {
        return emptyList()
    }
    suspend fun getPlaylistsByUserId(userId: String): List<Playlist> {
        return emptyList()
    }
    suspend fun createPlaylist(playlist: Playlist): Boolean {
        return false
    }
    suspend fun updatePlaylist(playlist: Playlist): Boolean {
        return false
    }
    suspend fun deletePlaylist(id: String): Boolean {
        return false
    }
    suspend fun addSongToPlaylist(playlistId: String, song: Song): Boolean {
        return false
    }
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        return false
    }
}

@Singleton
class PlaylistRepositoryImpl  : PlaylistRepository {

    private val db = Firestore.getInstance()
    private val collectionName = "playlists"

    private val playlists = mutableListOf(
        Playlist(
            id = "123",
            userId = "123",
            title = "Absolute banger",
            description = "Playlist from Vibeify",
            imagePath = R.drawable.ic_launcher_background,
            songs = mutableListOf(
                Song(name = "Song Name 1", duration = 153),
                Song(name = "Song Name 2", duration = 215),
                Song(name = "Song Name 3", duration = 391)
            )
        ),
        Playlist(
            id = "456",
            userId = "123",
            title = "Chill Vibes",
            description = "Relax and enjoy!",
            imagePath = R.drawable.ic_launcher_background,
            songs = mutableListOf(
                Song(name = "Chill Song 1", duration = 200),
                Song(name = "Chill Song 2", duration = 180)
            )
        ),
        Playlist(
            id = "789",
            userId = "123",
            title = "Workout Hits",
            description = "Get pumped up!",
            imagePath = R.drawable.ic_launcher_background,
            songs = mutableListOf(
                Song(name = "Workout Song 1", duration = 240),
                Song(name = "Workout Song 2", duration = 300)
            )
        )
    )
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    @Inject constructor(){
        for (playlist in playlists) {
            scope.launch {
                createPlaylist(playlist)
            }
        }
    }

    override suspend fun getPlaylistById(id: String): Playlist? {
        var res = db.collection(collectionName).whereEqualTo("id", id).get().await()

        if (res.isEmpty) {
            return null
        }

        val playlistData = res.firstOrNull()?.toObject(Playlist::class.java)

        return playlistData
    }

    override suspend fun getAllPlaylists(): List<Playlist> {
        var res = db.collection(collectionName).get().await()
        if (res.isEmpty) {
            return emptyList()
        }
        val playlists = res.documents.mapNotNull { it.toObject(Playlist::class.java) }
        return playlists
    }
    override suspend fun searchPlaylists(query: String): List<Playlist> {
        var res = db.collection(collectionName)
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .get()
            .await()

        if (res.isEmpty) {
            return emptyList()
        }

        val playlists = res.documents.mapNotNull { it.toObject(Playlist::class.java) }

        return playlists
    }
    override suspend fun getPlaylistsByUserId(userId: String): List<Playlist> {
        var res = db.collection(collectionName).whereEqualTo("userId", userId).get().await()
        if (res.isEmpty) {
            return emptyList()
        }
        val playlists = res.documents.mapNotNull { it.toObject(Playlist::class.java) }
        return playlists
    }
    override suspend fun createPlaylist(playlist: Playlist): Boolean {
        db.collection(
            collectionName
        ).document(playlist.id).set(playlist).await()
        return true
    }
    override suspend fun updatePlaylist(playlist: Playlist): Boolean {
        db.collection(collectionName).document(playlist.id).set(playlist).await()
        return  true
    }
    override suspend fun deletePlaylist(id: String): Boolean {
        db.collection(collectionName).document(id).delete().await()
        return  true
    }

    override suspend fun addSongToPlaylist(playlistId: String, song: Song): Boolean {
        db.collection(collectionName).document(playlistId).update("songs", FieldValue.arrayUnion(song)).await()
        return true
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        db.collection(collectionName).document(playlistId).update("songs", FieldValue.arrayRemove(Song(id = songId))).await()
        return true
    }


}

