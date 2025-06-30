package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.Filter
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore as Firestore

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

@Singleton
class SongRepositoryImpl @Inject constructor() : SongRepository {

    private val db = Firestore.getInstance()
    private val collectionName = "songs"


    private var genreCacheLastUpdated: Long = 0
    private val cacheDurationMs = 5 * 60 * 1000L // 5 Minuten Cache

    private fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - genreCacheLastUpdated < cacheDurationMs
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
        val res = db.collection(collectionName).get().await()
        return if (!res.isEmpty) {
            res.documents.mapNotNull { it.toObject(Song::class.java) }
        } else {
            emptyList()
        }
    }

    override suspend fun searchSongs(query: String): List<Song> {
        val capitalizedQuery = query.lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }

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
