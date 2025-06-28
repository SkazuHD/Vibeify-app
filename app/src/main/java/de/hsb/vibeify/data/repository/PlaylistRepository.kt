package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Playlist
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

    override suspend fun getPlaylistById(id: String): Playlist? {
        val res = db.collection(collectionName).whereEqualTo("id", id).get().await()

        if (res.isEmpty) {
            return null
        }

        return res.firstOrNull()?.toObject(Playlist::class.java)
    }

    override suspend fun getAllPlaylists(): List<Playlist> {
        val res = db.collection(collectionName).get().await()
        if (res.isEmpty) {
            return emptyList()
        }
        return res.documents.mapNotNull { it.toObject(Playlist::class.java) }
    }

    override suspend fun searchPlaylists(query: String): List<Playlist> {
        val res = db.collection(collectionName).limit(
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
        val res = db.collection(collectionName).whereEqualTo("userId", userId).get().await()
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
