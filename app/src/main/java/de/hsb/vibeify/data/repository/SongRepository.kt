package de.hsb.vibeify.data.repository

import com.google.firebase.firestore.Filter
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Genre-spezifische Methoden mit Caching
    suspend fun getAvailableGenres(): List<String>
    suspend fun getSongsByGenre(genre: String): List<Song>
    suspend fun getGenreStatistics(): Map<String, Int>
    suspend fun refreshGenreCache()

    // StateFlow für reactive UI
    val availableGenresFlow: StateFlow<List<String>>
    val genreStatisticsFlow: StateFlow<Map<String, Int>>
}

@Singleton
class SongRepositoryImpl @Inject constructor() : SongRepository {

    private val db = Firestore.getInstance()
    private val collectionName = "songs"

    // Cache für Genres
    private val _availableGenresFlow = MutableStateFlow<List<String>>(emptyList())
    override val availableGenresFlow: StateFlow<List<String>> = _availableGenresFlow.asStateFlow()

    private val _genreStatisticsFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val genreStatisticsFlow: StateFlow<Map<String, Int>> =
        _genreStatisticsFlow.asStateFlow()

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

    // Genre-spezifische Implementierungen mit Caching
    override suspend fun getAvailableGenres(): List<String> {
        // Verwende Cache wenn verfügbar und gültig
        if (_availableGenresFlow.value.isNotEmpty() && isCacheValid()) {
            return _availableGenresFlow.value
        }

        return try {
            val allSongs = getAllSongs()
            val genres = allSongs.mapNotNull { it.genre }
                .distinct()
                .filter { it.isNotBlank() }
                .sorted()

            // Update Cache und StateFlow
            _availableGenresFlow.value = genres
            genreCacheLastUpdated = System.currentTimeMillis()

            genres
        } catch (e: Exception) {
            // Fallback zu Standard-Genres
            val fallbackGenres = listOf(
                "Rock", "Pop", "Hip-Hop", "Jazz", "Classical",
                "Electronic", "Country", "R&B", "Reggae", "Blues"
            )
            _availableGenresFlow.value = fallbackGenres
            fallbackGenres
        }
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

    override suspend fun getGenreStatistics(): Map<String, Int> {
        // Verwende Cache wenn verfügbar und gültig
        if (_genreStatisticsFlow.value.isNotEmpty() && isCacheValid()) {
            return _genreStatisticsFlow.value
        }

        return try {
            val allSongs = getAllSongs()
            val statistics = allSongs.groupBy { it.genre ?: "Unbekannt" }
                .mapValues { it.value.size }
                .toSortedMap()

            // Update Cache und StateFlow
            _genreStatisticsFlow.value = statistics
            genreCacheLastUpdated = System.currentTimeMillis()

            statistics
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override suspend fun refreshGenreCache() {
        genreCacheLastUpdated = 0 // Cache invalidieren
        // Neulade die Daten
        getAvailableGenres()
        getGenreStatistics()
    }
}
