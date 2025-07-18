package de.hsb.vibeify.services

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import de.hsb.vibeify.data.model.Genre
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// This service is responsible for loading and managing discovery content such as trending songs, featured playlists, and genres.

@Singleton
class DiscoveryService @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository
) {
    var trendingSongs = mutableStateOf<List<Song>>(emptyList())
        private set

    var featuredPlaylists = mutableStateOf<List<Playlist>>(emptyList())
        private set

    var randomSongs = mutableStateOf<List<Song>>(emptyList())
        private set

    private val _genreList = MutableStateFlow<List<Genre>>(emptyList())
    val genreList: StateFlow<List<Genre>> = _genreList.asStateFlow()

    var isLoading = mutableStateOf(false)
        private set

    private val _allSongs = MutableStateFlow(
        emptyList<Song>()
    )

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var _job: Job? = null

    init {
        loadDiscoveryContent()
    }

    // Expose the allSongs state as a StateFlow
    private fun loadDiscoveryContent() {
        _job = scope.launch {
            isLoading.value = true
            try {
                Log.d("DiscoveryService", "Loading discovery content")
                val allSongs = songRepository.getAllSongs()
                _allSongs.update { allSongs }
            } catch (e: Exception) {
            } finally {

            }
        }
        // Wait for the initial load to complete before loading other content
        scope.launch {
            _job?.join()
            launch { loadTrendingSongs() }
            launch { loadGenres() }
            launch { loadFeaturedPlaylists() }
            launch { loadRandomSongs() }
            isLoading.value = false
        }
    }

    fun refreshContent() {
        scope.launch {
            loadTrendingSongs()
            loadFeaturedPlaylists()
            loadRandomSongs()
        }
    }

    /**
     * Generates a list of random songs from the allSongs list.
     * @param limit The number of random songs to return.
     * @return A list of random songs, or an empty list if an error occurs.
     */
    suspend fun generateRandomSongs(limit: Int): List<Song> {
        _job?.join()

        return try {
            _allSongs.value.shuffled().take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Loads trending songs and updates the trendingSongs state.
     */
    private suspend fun loadTrendingSongs() {
        try {
            trendingSongs.value = generateRandomSongs(5)
        } catch (e: Exception) {
            trendingSongs.value = emptyList()
        }
    }

    /**
     * Loads featured playlists and updates the featuredPlaylists state.
     */
    private suspend fun loadFeaturedPlaylists() {
        try {
            val playlists = playlistRepository.getAllPlaylists()
            featuredPlaylists.value = playlists.shuffled().take(4)
        } catch (e: Exception) {
            featuredPlaylists.value = emptyList()
        }
    }

    /**
     * Loads random songs and updates the randomSongs state.
     */
    private suspend fun loadRandomSongs() {
        try {
            randomSongs.value = generateRandomSongs(3)
        } catch (e: Exception) {
            randomSongs.value = emptyList()
        }
    }

    private suspend fun loadGenres() {
        try {
            getGenreList()
        } catch (e: Exception) {
        }
    }

    suspend fun getSongsByGenre(genre: String): List<Song> {
        _job?.join()
        return try {
            _allSongs.value.filter { song ->
                song.genre?.equals(genre, ignoreCase = true) ?: false
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Retrieves a list of genres from the allSongs list.
     * Each genre is represented by a Genre object with a unique ID, name, description, count, and image URL.
     * @return A list of Genre objects representing the genres found in the allSongs list.
     */
    suspend fun getGenreList(): List<Genre> {
        _job?.join()
        val allSongs = _allSongs.value
        val genres = allSongs.mapNotNull { it.genre }
            .distinct()
            .filter { it.isNotBlank() }
            .sorted().map {
                Genre(
                    id = UUID.randomUUID().toString(),
                    name = it,
                    description = "Genre: $it",
                    count = allSongs.count { song -> song.genre == it },
                    imageUrl = null
                )
            }

        _genreList.value = genres
        Log.d(
            "DiscoveryService",
            "Loaded ${genres.size} genres found"
        )
        return genres

    }
}