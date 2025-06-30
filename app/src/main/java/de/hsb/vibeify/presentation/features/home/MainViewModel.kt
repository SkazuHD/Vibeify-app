package de.hsb.vibeify.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.RecentActivity
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistRepository: PlaylistRepository,
    private val playlistService: PlaylistService,
    private val songRepository: SongRepository
) : ViewModel() {

    val recentActivityItems = MutableStateFlow<List<RecentActivityItem>>(emptyList())
    val isLoading = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            launch {
                userRepository.state.map { it.currentUser }.distinctUntilChanged().collect { user ->
                    if (user != null) {
                        loadRecentActivities(user.recentActivities)
                    } else {
                        recentActivityItems.value = emptyList()
                    }
                    isLoading.value = false
                }
            }

        }
    }

    private suspend fun loadRecentActivities(activities: List<RecentActivity>) {
        Log.d("MainViewModel", "Loading recent activities: ${activities.size} activities found")
        val sortedActivities =
            activities.sortedByDescending { it.timestamp }.distinctBy { it.id }.take(8)

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

        recentActivityItems.value = activityItems
    }
}