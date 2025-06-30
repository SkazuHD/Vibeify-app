package de.hsb.vibeify.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.ui.components.searchbar.SearchbarViewModel
import de.hsb.vibeify.ui.components.searchbar.SimpleSearchBar
import de.hsb.vibeify.ui.player.PlaybackViewModel
import de.hsb.vibeify.ui.search.discovery.DiscoverySection

@Composable
fun SearchView(
    modifier: Modifier = Modifier,
    vm: SearchbarViewModel = hiltViewModel(),
    vm2: PlaybackViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val textFieldState = remember { TextFieldState("") }
    val searchResults by vm.searchResults

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
                    onPlaylistClick = { playlist ->
                        navController?.navigate("playlist_detail_view/${playlist.id}")
                        vm.clearSearchResults()
                    },
                    onSongClick = { song ->
                        vm2.play(song)
                    }
                )
            }

            // Zeige Discovery-Sektion wenn keine Suchergebnisse vorhanden sind
            if (searchResults.songs.isEmpty() && searchResults.playlists.isEmpty()) {
                DiscoverySection(
                    onSongClick = { song ->
                        vm2.play(song)
                    },
                    onPlaylistClick = { playlist ->
                        navController?.navigate("playlist_detail_view/${playlist.id}")
                    },
                    onGenreClick = { genre ->
                        // Führe eine Genre-basierte Suche durch
                        textFieldState.edit {
                            replace(0, length, genre)
                        }
                        vm.onSearch(genre)
                    }
                )
            }
        }
    }
}