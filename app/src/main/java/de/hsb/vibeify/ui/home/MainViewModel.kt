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

data class MainViewUIState(
    val recentActivityItems: List<RecentActivityItem> = emptyList(),
    val recommendations: List<Song> = emptyList(),
    val isLoadingActivities: Boolean = true,
    val isLoadingRecommendations: Boolean = true
)

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


    init {
        _uiState.update { it.copy(isLoadingActivities = true, isLoadingRecommendations = true) }
        viewModelScope.launch {
            launch {
                userRepository.state.map { it.currentUser }.distinctUntilChanged().collect { user ->
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