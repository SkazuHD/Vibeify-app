package de.hsb.vibeify.ui.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    var trendingSongs = mutableStateOf<List<Song>>(emptyList())
        private set

    var featuredPlaylists = mutableStateOf<List<Playlist>>(emptyList())
        private set

    var randomSongs = mutableStateOf<List<Song>>(emptyList())
        private set

    // StateFlow für reactive Genre-Updates
    val availableGenres: StateFlow<List<String>> = songRepository.availableGenresFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val genreStatistics: StateFlow<Map<String, Int>> = songRepository.genreStatisticsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    var isLoading = mutableStateOf(false)
        private set

    init {
        loadDiscoveryContent()
    }

    private fun loadDiscoveryContent() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Lade verschiedene Inhalte für die Discovery-Seite
                loadTrendingSongs()
                loadFeaturedPlaylists()
                loadRandomSongs()
                loadGenres()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading.value = false
            }
        }
    }

    private suspend fun loadTrendingSongs() {
        try {
            val songs = songRepository.getAllSongs()
            trendingSongs.value = songs.shuffled().take(5)
        } catch (e: Exception) {
            trendingSongs.value = emptyList()
        }
    }

    private suspend fun loadFeaturedPlaylists() {
        try {
            val playlists = playlistRepository.getAllPlaylists()
            featuredPlaylists.value = playlists.shuffled().take(4)
        } catch (e: Exception) {
            featuredPlaylists.value = emptyList()
        }
    }

    private suspend fun loadRandomSongs() {
        try {
            val songs = songRepository.getRandomSongs(3)
            randomSongs.value = songs
        } catch (e: Exception) {
            randomSongs.value = emptyList()
        }
    }

    private suspend fun loadGenres() {
        try {
            songRepository.getAvailableGenres()
            songRepository.getGenreStatistics()
        } catch (e: Exception) {
        }
    }

    suspend fun getSongsByGenre(genre: String): List<Song> {
        return try {
            songRepository.getSongsByGenre(genre)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun refreshContent() {
        viewModelScope.launch {
            songRepository.refreshGenreCache()
            loadDiscoveryContent()
        }
    }
}
