package de.hsb.vibeify.core.navigation

import androidx.navigation.NavController
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun NavController.navigateToPublicProfile(userId: String) {
    navigate(NavigationDestination.Detail.PublicProfile.createRoute(userId))
}


fun NavController.navigateToPlaylistDetail(playlistId: String) {
    navigate(NavigationDestination.Detail.PlaylistDetail.createRoute(playlistId))
}

fun NavController.navigateToArtistDetail(artistId: String) {
    navigate(NavigationDestination.Detail.ArtistDetail.createRoute(artistId))
}

fun NavController.navigateToPlayback() {
    navigate(NavigationDestination.Detail.Playback.route)
}

fun NavController.navigateToRegister() {
    navigate(NavigationDestination.Register.route)
}


fun String.urlEncode(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}


fun String.urlDecode(): String {
    return URLDecoder.decode(this, StandardCharsets.UTF_8.toString())
}
