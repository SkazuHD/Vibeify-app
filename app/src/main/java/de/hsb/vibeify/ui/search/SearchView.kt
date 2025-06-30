package de.hsb.vibeify.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.ui.player.PlaybackViewModel
import de.hsb.vibeify.ui.search.discovery.DiscoverySection
import de.hsb.vibeify.ui.search.searchbar.SearchbarViewModel
import de.hsb.vibeify.ui.search.searchbar.SimpleSearchBar

@Composable
fun SearchView(
    modifier: Modifier = Modifier,
    vm: SearchbarViewModel = hiltViewModel(),
    vm2: PlaybackViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val textFieldState = remember { TextFieldState("") }
    val searchResults by vm.searchResults
    val recentSearches = vm.recentSearches.collectAsState(initial = emptyList())

    Box(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SimpleSearchBar(
                    textFieldState = textFieldState,
                    onSearch = { query ->
                        vm.onSearch(query)
                    },
                    searchResults = searchResults,
                    recentSearches = recentSearches.value,
                    onPlaylistClick = { playlist ->
                        navController?.navigate("playlist_detail_view/${playlist.id}")
                        vm.clearSearchResults()
                    },
                    onSongClick = { song ->
                        vm2.play(song)
                    },
                    onGenreClick = { genre ->
                        navController?.navigate("playlist_detail_view/genre_${genre.name}")
                        vm.clearSearchResults()
                    },

                    )
            }

            if (searchResults.songs.isEmpty() && searchResults.playlists.isEmpty()) {
                DiscoverySection(
                    onSongClick = { song ->
                        vm2.play(song)
                    },
                    onPlaylistClick = { playlist ->
                        navController?.navigate("playlist_detail_view/${playlist.id}")
                    },
                    onGenreClick = { genre ->
                        navController?.navigate("playlist_detail_view/genre_$genre")
                    }
                )
            }
        }
    }
}