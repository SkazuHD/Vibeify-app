package de.hsb.vibeify.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Type-safe navigation destinations using sealed classes
 * This eliminates hardcoded route strings and provides compile-time safety
 */
sealed class NavigationDestination(val route: String) {

    // Auth flow destinations
    data object Login : NavigationDestination("auth/login")
    data object Register : NavigationDestination("auth/register")

    // Main app destinations with bottom navigation
    sealed class Main(route: String, val label: String, val icon: ImageVector) :
        NavigationDestination(route) {
        data object Home : Main("main/home", "Songs", Icons.Default.Home)
        data object Search : Main("main/search", "Search", Icons.Default.Search)
        data object Playlists : Main("main/playlists", "Playlists", Icons.Default.LibraryMusic)
        data object Profile : Main("main/profile", "Profile", Icons.Default.Person)
    }

    sealed class Detail(route: String) : NavigationDestination(route) {
        data class PublicProfile(val userId: String) : Detail("detail/public_profile/{userId}") {
            companion object {
                const val ROUTE_TEMPLATE = "detail/public_profile/{userId}"
                fun createRoute(userId: String) = "detail/public_profile/${userId.urlEncode()}"
            }
        }

        data class PlaylistDetail(val playlistId: String) : Detail("detail/playlist/{playlistId}") {
            companion object {
                const val ROUTE_TEMPLATE = "detail/playlist/{playlistId}"
                fun createRoute(playlistId: String) = "detail/playlist/${playlistId.urlEncode()}"
            }
        }

        data class ArtistDetail(val artistId: String) : Detail("detail/artist/{artistId}") {
            companion object {
                const val ROUTE_TEMPLATE = "detail/artist/{artistId}"
                fun createRoute(artistId: String) = "detail/artist/${artistId.urlEncode()}"
            }
        }

        data object Playback : Detail("detail/playback")
    }
}


val bottomNavDestinations = listOf(
    NavigationDestination.Main.Home,
    NavigationDestination.Main.Search,
    NavigationDestination.Main.Playlists,
    NavigationDestination.Main.Profile
)

/**
 * Extension functions for type-safe navigation
 */
fun NavigationDestination.Detail.PublicProfile.Companion.fromRoute(userId: String): NavigationDestination.Detail.PublicProfile {
    return NavigationDestination.Detail.PublicProfile(userId)
}

fun NavigationDestination.Detail.PlaylistDetail.Companion.fromRoute(playlistId: String): NavigationDestination.Detail.PlaylistDetail {
    return NavigationDestination.Detail.PlaylistDetail(playlistId)
}

fun NavigationDestination.Detail.ArtistDetail.Companion.fromRoute(artistId: String): NavigationDestination.Detail.ArtistDetail {
    return NavigationDestination.Detail.ArtistDetail(artistId)
}
