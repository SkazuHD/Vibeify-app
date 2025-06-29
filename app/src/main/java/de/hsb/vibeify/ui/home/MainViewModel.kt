package de.hsb.vibeify.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.RecentActivity
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    val recentActivityItems = MutableStateFlow<List<RecentActivityItem>>(emptyList())
    val isLoading = MutableStateFlow(true)

    init {
        viewModelScope.launch {
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

    private suspend fun loadRecentActivities(activities: List<RecentActivity>) {
        Log.d("MainViewModel", "Loading recent activities: ${activities.size} activities found")
        val sortedActivities =
            activities.sortedByDescending { it.timestamp }.distinctBy { it.id }.take(8)
        val activityItems = mutableListOf<RecentActivityItem>()

        for (activity in sortedActivities) {
            when (activity.type) {
                RecentActivity.TYPE_SONG -> {
                    songRepository.getSongById(activity.id)?.let { song ->
                        activityItems.add(
                            RecentActivityItem.SongActivity(
                                recentActivity = activity,
                                song = song
                            )
                        )
                    }
                }

                RecentActivity.TYPE_PLAYLIST -> {
                    playlistRepository.getPlaylistById(activity.id)?.let { playlist ->
                        activityItems.add(
                            RecentActivityItem.PlaylistActivity(
                                recentActivity = activity,
                                playlist = playlist
                            )
                        )
                    }
                }
            }
        }

        recentActivityItems.value = activityItems
    }
}