package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import de.hsb.vibeify.data.model.Playlist
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun getPlaylistsByIds(playlistIds: List<String>): List<Playlist>
    suspend fun getLikedSongsPlaylist(likedSongIds: List<String>): Playlist
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : PlaylistRepository {
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

    override suspend fun getPlaylistsByIds(playlistIds: List<String>): List<Playlist> {
        if (playlistIds.isEmpty()) return emptyList()

        val batchSize = 30

        return coroutineScope {
            playlistIds.chunked(batchSize).map { batch ->
                async {
                    val res = db.collection(collectionName)
                        .whereIn("id", batch)
                        .get()
                        .await()

                    if (!res.isEmpty) {
                        res.documents.mapNotNull { it.toObject(Playlist::class.java) }
                    } else {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()
        }
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
        db.collection(collectionName).document(playlistId)
            .update("songIds", FieldValue.arrayUnion(songId)).await()
        return true
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Boolean {
        db.collection(collectionName).document(playlistId)
            .update("songIds", FieldValue.arrayRemove(songId)).await()
        return true
    }

    override suspend fun getLikedSongsPlaylist(likedSongIds: List<String>): Playlist {
        return Playlist(
            id = LIKED_SONGS_PLAYLIST_ID,
            userId = "",
            title = "Liked Songs",
            description = "Playlist of liked songs",
            imageUrl = "",
            songIds = likedSongIds.toMutableList()
        )
    }
}
