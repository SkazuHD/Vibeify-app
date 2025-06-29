package de.hsb.vibeify.data.model

data class User(
    var id: String = "",
    var email: String = "",
    var name: String = "",
    var imageUrl: String? = "",
    var playlists: List<String> = emptyList(),
    var likedSongs: List<String> = emptyList(),
    var recentActivities: List<RecentActivity> = emptyList()
)

data class RecentActivity(
    val type: String = TYPE_SONG,
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_SONG = "song"
        const val TYPE_PLAYLIST = "playlist"
    }
}
