package de.hsb.vibeify.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Navigation controller for Vibeify that handles navigation between different destinations.
 * It provides methods to navigate to specific destinations,
 * navigate back, and check the current destination.
 **/
class VibeifyNavigationController(private val navController: NavController) {

    fun navigateTo(destination: NavigationDestination) {
        when (destination) {
            is NavigationDestination.Detail.PublicProfile -> {
                val encodedUserId = destination.userId.urlEncode()
                navController.navigate("detail/public_profile/$encodedUserId")
            }

            is NavigationDestination.Detail.PlaylistDetail -> {
                val encodedPlaylistId = destination.playlistId.urlEncode()
                navController.navigate("detail/playlist/$encodedPlaylistId")
            }

            is NavigationDestination.Detail.ArtistDetail -> {
                val encodedArtistId = destination.artistId.urlEncode()
                navController.navigate("detail/artist/$encodedArtistId")
            }

            else -> navController.navigate(destination.route)
        }
    }


    fun navigateBack(): Boolean {
        return navController.popBackStack()
    }

    fun navigateToMainAndClearStack(destination: NavigationDestination.Main) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    @Composable
    fun getCurrentDestination(): NavigationDestination? {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        return when (currentRoute) {
            NavigationDestination.Login.route -> NavigationDestination.Login
            NavigationDestination.Register.route -> NavigationDestination.Register
            NavigationDestination.Main.Home.route -> NavigationDestination.Main.Home
            NavigationDestination.Main.Search.route -> NavigationDestination.Main.Search
            NavigationDestination.Main.Playlists.route -> NavigationDestination.Main.Playlists
            NavigationDestination.Main.Profile.route -> NavigationDestination.Main.Profile
            NavigationDestination.Detail.PublicProfile.ROUTE_TEMPLATE -> {
                val userId = navBackStackEntry?.arguments?.getString("userId")?.urlDecode() ?: ""
                NavigationDestination.Detail.PublicProfile(userId)
            }

            NavigationDestination.Detail.PlaylistDetail.ROUTE_TEMPLATE -> {
                val playlistId =
                    navBackStackEntry?.arguments?.getString("playlistId")?.urlDecode() ?: ""
                NavigationDestination.Detail.PlaylistDetail(playlistId)
            }

            NavigationDestination.Detail.ArtistDetail.ROUTE_TEMPLATE -> {
                val artistId =
                    navBackStackEntry?.arguments?.getString("artistId")?.urlDecode() ?: ""
                NavigationDestination.Detail.ArtistDetail(artistId)
            }

            NavigationDestination.Detail.Playback.route -> NavigationDestination.Detail.Playback
            else -> null
        }
    }


    //Check Destination to determine if the appbar/stickybar should be shown
    @Composable
    fun shouldShowAppBar(): Boolean {
        val currentDestination = getCurrentDestination()
        return currentDestination is NavigationDestination.Detail
    }

    @Composable
    fun shouldShowStickyBar(): Boolean {
        val currentDestination = getCurrentDestination()
        return currentDestination != NavigationDestination.Detail.Playback
    }
}

@Composable
fun rememberVibeifyNavigationController(navController: NavController): VibeifyNavigationController {
    return remember(navController) {
        VibeifyNavigationController(navController)
    }
}
