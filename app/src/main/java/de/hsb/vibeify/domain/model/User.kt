package de.hsb.vibeify.domain.model

/**
 * Clean Domain User Model - Keine Android Dependencies!
 * Business Logic pur, testbar und framework-unabhängig
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val imageUrl: String? = null,
    val playlists: List<String> = emptyList(),
    val likedSongs: List<String> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val recentSearches: List<String> = emptyList(),
) {
    val hasPlaylists: Boolean
        get() = playlists.isNotEmpty()

    val totalLikedSongs: Int
        get() = likedSongs.size

    val isNewUser: Boolean
        get() = recentActivities.isEmpty()
}

data class RecentActivity(
    val type: ActivityType,
    val id: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isRecent: Boolean
        get() = System.currentTimeMillis() - timestamp < 24 * 60 * 60 * 1000 // 24h
}

enum class ActivityType {
    SONG,
    PLAYLIST,
    ALBUM,
    ARTIST
}
