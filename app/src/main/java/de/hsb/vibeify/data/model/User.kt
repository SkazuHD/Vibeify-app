package de.hsb.vibeify.data.model

//Data class representing a user in the application

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val imageUrl: String? = "",
    val playlists: List<String> = emptyList(),
    val likedSongs: List<String> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val recentSearches: List<String> = emptyList(),
) {
    fun isNewUser(): Boolean {
        return recentActivities.isEmpty()
    }

    fun addRecentActivity(activity: RecentActivity) {
        recentActivities.plus(activity).takeLast(10)
    }

    fun addRecentSearch(search: String) {
        recentSearches.plus(search).takeLast(10)
    }
}

// Data class representing a recent activity of the user shown in the recent activities section of the main page
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
