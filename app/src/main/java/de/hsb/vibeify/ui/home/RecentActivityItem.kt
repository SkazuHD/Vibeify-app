package de.hsb.vibeify.ui.home

import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.RecentActivity
import de.hsb.vibeify.data.model.Song

sealed class RecentActivityItem(
    val activity: RecentActivity
) {
    data class SongActivity(
        val recentActivity: RecentActivity,
        val song: Song
    ) : RecentActivityItem(recentActivity)

    data class PlaylistActivity(
        val recentActivity: RecentActivity,
        val playlist: Playlist
    ) : RecentActivityItem(recentActivity)
}
