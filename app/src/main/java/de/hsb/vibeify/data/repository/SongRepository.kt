package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.Filter
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
    suspend fun refreshRandomSong(): Song?
    val currentRandomSong: StateFlow<Song?>
}

@Singleton
class SongRepositoryImpl @Inject constructor() : SongRepository {

    private val db = Firestore.getInstance()
    private val collectionName = "songs"

    private val _currentRandomSong = MutableStateFlow<Song?>(null)
    override val currentRandomSong: StateFlow<Song?> = _currentRandomSong

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
        if (res.isEmpty) {
            return emptyList()
        }
        return res.documents.mapNotNull { it.toObject(Song::class.java) }
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
        db.collection(collectionName).document(song.id).set(song).await()
        return true
    }

    override suspend fun updateSong(song: Song): Boolean {
        db.collection(collectionName).document(song.id).set(song).await()
        return true
    }

    override suspend fun deleteSong(id: String): Boolean {
        db.collection(collectionName).document(id).delete().await()
        return true
    }

    override suspend fun getRandomSongs(limit: Int): List<Song> {


        val res = db.collection(collectionName)
            .get()
            .await()

        if (res.isEmpty) {
            return emptyList()
        }

        val songs =
            res.documents.mapNotNull { it.toObject(Song::class.java) }.shuffled().take(limit)


        return songs.shuffled().take(limit)
    }


    override suspend fun refreshRandomSong(): Song? {
        _currentRandomSong.update { currentRandomSong ->
            val res = db.collection(collectionName)
                .get()
                .await()

            if (res.isEmpty) {
                return null
            }
            res.documents.mapNotNull { it.toObject(Song::class.java) }.shuffled().first()
        }
        return _currentRandomSong.value
    }
}
