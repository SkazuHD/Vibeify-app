package de.hsb.vibeify.data.repository

import android.util.Log
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing songs in the application.
 * Provides methods to interact with the Firestore database.
 */
interface SongRepository {
    suspend fun getSongById(id: String): Song?
    suspend fun getSongsByIds(ids: List<String>): List<Song>
    suspend fun getAllSongs(): List<Song>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun createSong(song: Song): Boolean
    suspend fun updateSong(song: Song): Boolean
    suspend fun deleteSong(id: String): Boolean
    suspend fun getRandomSongs(limit: Int): List<Song>
    suspend fun getSongsByGenre(genre: String): List<Song>

}

// Repository implementation for managing songs in the application.

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : SongRepository {
    private val collectionName = "songs"
    private val _allSongsCache = mutableListOf<Song>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var _job: Job? = null

// Coroutine job to preload all songs into cache
    init {
        Log.d("SongRepository", "Initialized with collection: $collectionName")
        _job = scope.launch {
            try {
                Log.d("SongRepository", "Preloading all songs into cache")
                val songs = withContext(Dispatchers.IO) { fetchAllSongsFromFirestore() }
                _allSongsCache.addAll(songs)
                Log.d("SongRepository", "Preloaded ${_allSongsCache.size} songs into cache")
            } catch (e: Exception) {
                Log.e("SongRepository", "Error preloading songs: ${e.message}")
            }
        }

    }

    /**
     * Fetches all songs from Firestore and returns them as a list.
     * This method is used to initialize the cache when the repository is created.
     */
    private suspend fun fetchAllSongsFromFirestore(): List<Song> {
        Log.d("SongRepository", "Fetching all songs from Firestore")
        val res = db.collection(collectionName).get().await()
        return if (!res.isEmpty) {
            res.documents.mapNotNull { it.toObject(Song::class.java) }
        } else {
            emptyList()
        }
    }

    override suspend fun getSongById(id: String): Song? {
        val res = db.collection(collectionName).document(id).get().await()
        return if (res.exists()) res.toObject(Song::class.java) else null
    }

    override suspend fun getSongsByIds(ids: List<String>): List<Song> {
        if (ids.isEmpty()) return emptyList()

        val batchSize = 30

        return coroutineScope {
            ids.chunked(batchSize).map { batch ->
                async {
                    val res = db.collection(collectionName)
                        .whereIn("id", batch)
                        .get()
                        .await()

                    if (!res.isEmpty) {
                        res.documents.mapNotNull { it.toObject(Song::class.java) }
                    } else {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()
        }
    }

    override suspend fun getAllSongs(): List<Song> {
        _job?.join()

        if (_allSongsCache.isNotEmpty()) {
            Log.d("SongRepository", "Returning cached songs")
            return _allSongsCache
        }

        val songs = fetchAllSongsFromFirestore()
        _allSongsCache.clear()
        _allSongsCache.addAll(songs)
        return songs
    }

    override suspend fun searchSongs(query: String): List<Song> {
        val capitalizedQuery = query.lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }

        //Searching songs in Firestore using multiple filters. It searches for songs by name, artist, and album.

        val res = db.collection(collectionName)
            .where(
                Filter.or(
                    Filter.and(
                        Filter.greaterThanOrEqualTo("name", query),
                        Filter.lessThanOrEqualTo("name", query + "\uf8ff")
                    ),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("name", capitalizedQuery),
                        Filter.lessThanOrEqualTo("name", capitalizedQuery + "\uf8ff")
                    ),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("artist", query),
                        Filter.lessThanOrEqualTo("artist", query + "\uf8ff")
                    ),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("artist", capitalizedQuery),
                        Filter.lessThanOrEqualTo("artist", capitalizedQuery + "\uf8ff")
                    ),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("album", query),
                        Filter.lessThanOrEqualTo("album", query + "\uf8ff")
                    ),
                    Filter.and(
                        Filter.greaterThanOrEqualTo("album", capitalizedQuery),
                        Filter.lessThanOrEqualTo("album", capitalizedQuery + "\uf8ff")
                    )
                )
            )
            .get()
            .await()

        if (res.isEmpty) {
            return emptyList()
        }

        return res.documents.mapNotNull { it.toObject(Song::class.java) }
    }

    override suspend fun createSong(song: Song): Boolean {
        return try {
            db.collection(collectionName).document(song.id).set(song).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateSong(song: Song): Boolean {
        return try {
            db.collection(collectionName).document(song.id).set(song).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteSong(id: String): Boolean {
        return try {
            db.collection(collectionName).document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getRandomSongs(limit: Int): List<Song> {
        val allSongs = getAllSongs()
        return allSongs.shuffled().take(limit)
    }


    override suspend fun getSongsByGenre(genre: String): List<Song> {
        return try {
            val res = db.collection(collectionName)
                .whereEqualTo("genre", genre)
                .get()
                .await()

            if (!res.isEmpty) {
                res.documents.mapNotNull { it.toObject(Song::class.java) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


}
