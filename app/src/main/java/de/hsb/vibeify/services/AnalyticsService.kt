package de.hsb.vibeify.services

import android.util.Log
import de.hsb.vibeify.data.model.RecentActivity
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor(userRepository: UserRepository, playerServiceV2: PlayerServiceV2) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        Log.d("AnalyticsService", "init block entered")
        scope.launch {
            launch {
                playerServiceV2.currentSong.map { it?.id }.distinctUntilChanged().collect {
                    Log.d("AnalyticsService", "Current song changed: $it")
                    if (it != null) {
                        userRepository.addRecentActivity(
                            RecentActivity(
                                id = it,
                                timestamp = System.currentTimeMillis(),
                                type = RecentActivity.TYPE_SONG
                            )
                        )
                    }
                }
            }
            launch {
                playerServiceV2.currentPlaylistId.collect { playlistId ->
                    if (playlistId != null) {
                        Log.d("AnalyticsService", "Current playlist changed: $playlistId")
                        userRepository.addRecentActivity(
                            RecentActivity(
                                id = playlistId,
                                timestamp = System.currentTimeMillis(),
                                type = RecentActivity.TYPE_PLAYLIST
                            )
                        )
                    }
                }
            }

        }
    }

}