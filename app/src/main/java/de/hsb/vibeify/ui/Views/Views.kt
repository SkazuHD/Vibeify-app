package de.hsb.vibeify.ui.Views

sealed class Views(val route: String) {
    object MainView : Views("main")
    object LoginView : Views("login_view")
    object RegisterView : Views("register_view")
    object ProfileView : Views("profile_view")
    object PlaylistView : Views("playlist_view")
    object PlaylistDetailView : Views("playlist_detail_view")
}