package de.hsb.vibeify.core.navigation

import androidx.navigation.NavController

/**
 * Extension functions to help migrate from old navigation system to new type-safe system
 * These provide a bridge while transitioning the codebase
 */

/**
 * Navigate to public profile with type safety
 */
fun NavController.navigateToPublicProfile(userId: String) {
    val destination = NavigationDestination.Detail.PublicProfile(userId)
    navigate(NavigationDestination.Detail.PublicProfile.createRoute(userId))
}

/**
 * Navigate to playlist detail with type safety
 */
fun NavController.navigateToPlaylistDetail(playlistId: String) {
    val destination = NavigationDestination.Detail.PlaylistDetail(playlistId)
    navigate(NavigationDestination.Detail.PlaylistDetail.createRoute(playlistId))
}

/**
 * Navigate to playback view
 */
fun NavController.navigateToPlayback() {
    navigate(NavigationDestination.Detail.Playback.route)
}

/**
 * Navigate to register from login
 */
fun NavController.navigateToRegister() {
    navigate(NavigationDestination.Register.route)
}

/**
 * Navigate back to login from register
 */
fun NavController.navigateToLogin() {
    navigate(NavigationDestination.Login.route) {
        popUpTo(NavigationDestination.Register.route) {
            inclusive = true
        }
    }
}
