package de.hsb.vibeify.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.RecentActivity
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.LIKED_SONGS_PLAYLIST_ID
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.DiscoveryService
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//Data class for the UI state of the MainViewModel
data class MainViewUIState(
    val recentActivityItems: List<RecentActivityItem> = emptyList(),
    val recommendations: List<Song> = emptyList(),
    val isLoadingActivities: Boolean = true,
    val isLoadingRecommendations: Boolean = true
)

//MainViewModel class that handles the logic for the main view of the app
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistService: PlaylistService,
    private val songRepository: SongRepository,
    private val discoveryService: DiscoveryService
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MainViewUIState(
            isLoadingActivities = true,
            isLoadingRecommendations = true
        )
    )
    val uiState: StateFlow<MainViewUIState> = _uiState

    // Initialize the ViewModel by loading recent activities and recommendations

    init {
        _uiState.update { it.copy(isLoadingActivities = true, isLoadingRecommendations = true) }
        viewModelScope.launch {
            launch {
                userRepository.state.map { it.currentUser }.distinctUntilChanged().collect { user ->
                    _uiState.update { it.copy(isLoadingActivities = true) }
                    val recentActivityItems = if (user != null) {
                        loadRecentActivities(user.recentActivities)
                    } else {
                        emptyList<RecentActivityItem>()
                    }
                    _uiState.update {
                        it.copy(
                            recentActivityItems = recentActivityItems,
                            isLoadingActivities = false
                        )
                    }
                }
            }
            launch {
                _uiState.update { it.copy(isLoadingRecommendations = true) }
                val recommendations = discoveryService.generateRandomSongs(10)
                _uiState.update {
                    it.copy(
                        recommendations = recommendations,
                        isLoadingRecommendations = false
                    )
                }
            }
        }
    }

    /**
     * Loads recent activities from the user repository and maps them to RecentActivityItem.
     * Filters out the liked songs playlist and limits the number of activities to 8.
     * @param activities List of RecentActivity to process.
     * @return List of RecentActivityItem containing the mapped activities.
     * This function handles both song and playlist activities,
     * ensuring that only the most recent and relevant activities are displayed.
     */
    private suspend fun loadRecentActivities(activities: List<RecentActivity>): List<RecentActivityItem> {
        val sortedActivities =
            activities.sortedByDescending { it.timestamp }.distinctBy { it.id }
                .filter { it.id != LIKED_SONGS_PLAYLIST_ID }.take(8)


        val songIds = sortedActivities.filter { it.type == RecentActivity.TYPE_SONG }.map { it.id }
        val playlistIds =
            sortedActivities.filter { it.type == RecentActivity.TYPE_PLAYLIST }.map { it.id }

        val (songs, playlists) = coroutineScope {
            val songsDeferred = async {
                if (songIds.isNotEmpty()) songRepository.getSongsByIds(songIds) else emptyList()
            }
            val playlistsDeferred = async {
                if (playlistIds.isNotEmpty()) playlistService.getPlaylistsByIds(playlistIds) else emptyList()
            }

            Pair(songsDeferred.await(), playlistsDeferred.await())
        }

        val songMap = songs.associateBy { it.id }
        val playlistMap = playlists.associateBy { it.id }

        val activityItems = sortedActivities.mapNotNull { activity ->
            Log.d("MainViewModel", "Processing activity: ${activity.id} of type ${activity.type}")
            when (activity.type) {
                RecentActivity.TYPE_SONG -> {
                    songMap[activity.id]?.let { song ->
                        RecentActivityItem.SongActivity(
                            recentActivity = activity,
                            song = song
                        )
                    }
                }

                RecentActivity.TYPE_PLAYLIST -> {
                    playlistMap[activity.id]?.let { playlist ->
                        RecentActivityItem.PlaylistActivity(
                            recentActivity = activity,
                            playlist = playlist
                        )
                    }
                }

                else -> null
            }
        }
        Log.d("MainViewModel", "Mapped activities: ${activityItems.size} activity items created")
        return activityItems
    }
}