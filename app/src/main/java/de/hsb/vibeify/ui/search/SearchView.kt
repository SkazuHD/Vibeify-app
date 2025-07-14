package de.hsb.vibeify.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.core.navigation.navigateToArtistDetail
import de.hsb.vibeify.core.navigation.navigateToPlaylistDetail
import de.hsb.vibeify.core.navigation.navigateToPublicProfile
import de.hsb.vibeify.ui.player.PlaybackViewModel
import de.hsb.vibeify.ui.search.discovery.DiscoverySection
import de.hsb.vibeify.ui.search.searchbar.SearchbarViewModel
import de.hsb.vibeify.ui.search.searchbar.SimpleSearchBar
import java.net.URLEncoder

@Composable
fun SearchView(
    modifier: Modifier = Modifier,
    vm: SearchbarViewModel = hiltViewModel(),  // ViewModel for handling search state and logic
    vm2: PlaybackViewModel = hiltViewModel(),  // ViewModel for handling playback control
    navController: NavController? = null
) {
    // Collect state flows from ViewModel
    val textFieldState = remember { TextFieldState("") }
    val searchResults by vm.searchResults
    val recentSearches = vm.recentSearches.collectAsState(initial = emptyList())
    val isLoading by vm.isLoading.collectAsState(initial = false)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            // Search bar handles user queries, triggers search logic, and processes result item actions
            SimpleSearchBar(
                textFieldState = textFieldState,
                onSearch = { query ->
                    vm.onSearch(query)
                },
                searchResults = searchResults,
                recentSearches = recentSearches.value,
                isLoading = isLoading,
                onPlaylistClick = { playlist ->
                    navController?.navigateToPlaylistDetail(playlist.id)
                    vm.clearSearchResults()
                },
                onSongClick = { song ->
                    vm2.play(song)
                },
                onGenreClick = { genre ->
                    val encodedGenreName = URLEncoder.encode(genre.name, "UTF-8")
                    navController?.navigateToPlaylistDetail(encodedGenreName)
                    vm.clearSearchResults()
                },
                onProfileClick = { profile ->
                    navController?.navigateToPublicProfile(profile.id)
                    vm.clearSearchResults()
                },
                onArtistClick = { artist ->
                    navController?.navigateToArtistDetail(artist.id)
                    vm.clearSearchResults()
                },
                onClose = {
                    textFieldState.edit { replace(0, length, "") }
                    vm.clearSearchResults()

                }
            )
        }

        DiscoverySection(
            onSongClick = { song ->
                vm2.play(song)
            },
            onPlaylistClick = { playlist ->
                navController?.navigateToPlaylistDetail(playlist.id)
            },
            onGenreClick = { genre ->
                val genreRoute = "genre_${URLEncoder.encode(genre.name, "UTF-8")}"
                navController?.navigateToPlaylistDetail(genreRoute)

            }
        )

    }
}
