package de.hsb.vibeify.ui.Views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home

import androidx.compose.ui.graphics.vector.ImageVector

enum class Destinations(val route: String) {
     Guard("guard"),
     MainView("main"),
     LoginView("login_view"),
     RegisterView("register_view"),
     ProfileView("profile_view"),
     PlaylistView("playlist_view"),
     PlaylistDetailView("playlist_detail_view"),
     SearchView("search_view");
}

enum class NavbarDestinations(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    SONGS(Destinations.MainView.route, "Songs", Icons.Default.Home, "Songs"),
    SEARCH(Destinations.SearchView.route, "Search", Icons.Default.Home, "Search"),
    PLAYLISTS(Destinations.PlaylistView.route, "Playlist", Icons.Default.Home, "Playlist")
}
