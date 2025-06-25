package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore as Firestore

const val LIKED_SONGS_PLAYLIST_ID = "liked_songs_virtual_playlist"

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
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Boolean {
        return false
    }
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        return false
    }
    suspend fun getPlaylistsForUser(playlistIds: List<String>): List<Playlist>
    suspend fun getLikedSongsPlaylist(likedSongIds: List<String>): Playlist
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val songRepository: SongRepository
) : PlaylistRepository {

    private val db = Firestore.getInstance()
    private val collectionName = "playlists"

    private val playlists = mutableListOf(
        Playlist(
            id = "123",
            userId = "123",
            title = "Absolute banger",
            description = "Playlist from Vibeify",
            imagePath = R.drawable.ic_launcher_background,
            songIds = mutableListOf("0013b362dfcecebbd7794c204fc8954bde648af6", "01c761885705ea7f145776bb0e50767d290330d9", "0804eb8eaac3e727b247142ecb35f99c326489b8")
        ),
        Playlist(
            id = "456",
            userId = "123",
            title = "Chill Vibes",
            description = "Relax and enjoy!",
            imagePath = R.drawable.ic_launcher_background,
            songIds = mutableListOf("10b5773de4a2307280ff8048cc7d6c739c811d6f", "156489a1e356bac8c39c43f1248556cf6354e0da")
        ),
        Playlist(
            id = "789",
            userId = "123",
            title = "Workout Hits",
            description = "Get pumped up!",
            imagePath = R.drawable.ic_launcher_background,
            songIds = mutableListOf("1db767253c94eedc2ac4029d857733fca9be6829", "20efe98d5f33a110f4b390dea15ca33c30e34356")
        )
    )
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {

            playlists.forEach { playlist ->
                createPlaylist(playlist)
            }
        }
    }

    override suspend fun getPlaylistById(id: String): Playlist? {
        var res = db.collection(collectionName).whereEqualTo("id", id).get().await()

        if (res.isEmpty) {
            return null
        }

        return res.firstOrNull()?.toObject(Playlist::class.java)
    }

    override suspend fun getAllPlaylists(): List<Playlist> {
        var res = db.collection(collectionName).get().await()
        if (res.isEmpty) {
            return emptyList()
        }
        return res.documents.mapNotNull { it.toObject(Playlist::class.java) }
    }

    override suspend fun searchPlaylists(query: String): List<Playlist> {
        var res = db.collection(collectionName).limit(
            10
        ).where(
                Filter.and(
                    Filter.greaterThanOrEqualTo("title", query),
                    Filter.lessThanOrEqualTo("title", query + "\uf8ff"),
                )
            )
            .get()
            .await()

        if (res.isEmpty) {
            return emptyList()
        }

        return res.documents.mapNotNull { it.toObject(Playlist::class.java) }
    }

    override suspend fun getPlaylistsByUserId(userId: String): List<Playlist> {
        var res = db.collection(collectionName).whereEqualTo("userId", userId).get().await()
        if (res.isEmpty) {
            return emptyList()
        }
        return res.documents.mapNotNull { it.toObject(Playlist::class.java) }
    }

    override suspend fun getPlaylistsForUser(playlistIds: List<String>): List<Playlist> {
        if (playlistIds.isEmpty()) return emptyList()

        val res = db.collection(collectionName)
            .whereIn("id", playlistIds)
            .get()
            .await()

        if (res.isEmpty) {
            return emptyList()
        }

        return res.documents.mapNotNull { it.toObject(Playlist::class.java) }
    }

    override suspend fun createPlaylist(playlist: Playlist): Boolean {
        db.collection(collectionName).document(playlist.id).set(playlist).await()
        return true
    }

    override suspend fun updatePlaylist(playlist: Playlist): Boolean {
        db.collection(collectionName).document(playlist.id).set(playlist).await()
        return true
    }

    override suspend fun deletePlaylist(id: String): Boolean {
        db.collection(collectionName).document(id).delete().await()
        return true
    }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String): Boolean {
        db.collection(collectionName).document(playlistId).update("songIds", FieldValue.arrayUnion(songId)).await()
        return true
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        db.collection(collectionName).document(playlistId).update("songIds", FieldValue.arrayRemove(songId)).await()
        return true
    }

    override suspend fun getLikedSongsPlaylist(likedSongIds: List<String>): Playlist {
        return Playlist(
            id = LIKED_SONGS_PLAYLIST_ID,
            userId = "",
            title = "Liked Songs",
            description = "Playlist of liked songs",
            imagePath = R.drawable.ic_launcher_background,
            songIds = likedSongIds.toMutableList()
        )
    }
}
