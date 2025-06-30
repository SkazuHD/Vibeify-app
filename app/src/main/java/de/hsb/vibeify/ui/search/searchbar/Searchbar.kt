package de.hsb.vibeify.ui.search.searchbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.data.model.Genre
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.services.SearchResult
import de.hsb.vibeify.ui.components.LoadingIndicator
import de.hsb.vibeify.ui.components.PlaylistCard
import de.hsb.vibeify.ui.components.songCard.SmartSongCard
import de.hsb.vibeify.ui.player.PlaybackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchBar(
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit,
    searchResults: SearchResult,
    modifier: Modifier = Modifier,
    recentSearches: List<String>? = emptyList(),
    isLoading: Boolean = false,
    onPlaylistClick: (Playlist) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    onGenreClick: (Genre) -> Unit = {},
    onClose: () -> Unit = {},
    playbackViewModel: PlaybackViewModel = hiltViewModel(),
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    var showAllSongs by rememberSaveable { mutableStateOf(false) }
    var showAllArtists by rememberSaveable { mutableStateOf(false) }
    var showAllPlaylists by rememberSaveable { mutableStateOf(false) }
    var showAllAlbums by rememberSaveable { mutableStateOf(false) }
    var showAllGenres by rememberSaveable { mutableStateOf(false) }

    val resultLimit = 3

    Box(
        modifier
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                    onSearch = {
                        onSearch(textFieldState.text.toString())
                        expanded = true
                        showAllSongs = false
                        showAllArtists = false
                        showAllPlaylists = false
                        showAllAlbums = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search") },
                    trailingIcon = {},
                    leadingIcon = {
                        if (expanded)
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable(
                                        onClick = {
                                            expanded = false
                                            textFieldState.edit { replace(0, length, "") }
                                            onClose()
                                        }
                                    )
                            )
                        else
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.padding(8.dp)
                            )
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (isLoading) {
                    LoadingIndicator(
                        Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp)
                    )
                } else if (
                    searchResults.songs.isEmpty() &&
                    searchResults.playlists.isEmpty() &&
                    searchResults.artists.isEmpty()
                ) {
                    return@SearchBar Column {
                        recentSearches?.reversed()?.forEach { recentSearch ->
                            ListItem(
                                modifier = Modifier
                                    .clickable {
                                        textFieldState.edit { replace(0, length, recentSearch) }
                                        onSearch(recentSearch)
                                    }
                                    .semantics { traversalIndex = 0f },
                                headlineContent = { Text(recentSearch) },
                                trailingContent = { Text("Recent") }
                            )
                        }
                        if (recentSearches.isNullOrEmpty()) {
                            Text(
                                text = "No recent searches",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {


                    if (searchResults.songs.isNotEmpty()) {
                        ListItem(
                            modifier = Modifier.semantics { traversalIndex = 1f },
                            headlineContent = { Text("Songs") },
                            trailingContent = { Text("${searchResults.songs.size} results") }
                        )
                        searchResults.songs.take(if (showAllSongs) Int.MAX_VALUE else resultLimit)
                            .forEach { song ->
                                SmartSongCard(
                                    song = song,
                                    playbackViewModel = playbackViewModel,
                                    shape = RoundedCornerShape(0.dp),
                                    onClick = {
                                        if (onSongClick != {}) {
                                            onSongClick(song)
                                        }
                                        expanded = true
                                    },
                                )
                            }
                        if (!showAllSongs && searchResults.songs.size > resultLimit) {
                            Text(
                                text = "Show all ${searchResults.songs.size} songs",
                                modifier = Modifier
                                    .clickable { showAllSongs = true }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (searchResults.genres.isNotEmpty()) {
                        ListItem(
                            modifier = Modifier.semantics { traversalIndex = 1f },
                            headlineContent = { Text("Genres") },
                            trailingContent = { Text("${searchResults.genres.size} results") }
                        )
                        searchResults.genres.take(if (showAllGenres) Int.MAX_VALUE else resultLimit - 1)
                            .forEach { genre ->
                                ListItem(
                                    modifier = Modifier
                                        .clickable {
                                            onGenreClick(genre)
                                            expanded = false
                                            textFieldState.edit { replace(0, length, "") }
                                        },
                                    headlineContent = { Text(genre.name) },
                                    trailingContent = { Text("${genre.count} Songs") }
                                )
                            }
                    }

                    if (searchResults.artists.isNotEmpty()) {
                        ListItem(
                            modifier = Modifier.semantics { traversalIndex = 2f },
                            headlineContent = { Text("Artists") },
                            trailingContent = { Text("${searchResults.artists.size} results") }
                        )
                        searchResults.artists.take(if (showAllArtists) Int.MAX_VALUE else resultLimit)
                            .forEach { artist ->
                                ListItem(
                                    modifier = Modifier,
                                    headlineContent = { Text(artist.name) },
                                    trailingContent = { Text("View Artist") }
                                )
                            }
                        if (!showAllArtists && searchResults.artists.size > resultLimit) {
                            Text(
                                text = "Show all ${searchResults.artists.size} artists",
                                modifier = Modifier
                                    .clickable { showAllArtists = true }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (searchResults.playlists.isNotEmpty()) {
                        ListItem(
                            modifier = Modifier.semantics { traversalIndex = 2f },
                            headlineContent = { Text("Playlists") },
                            trailingContent = { Text("${searchResults.playlists.size} results") }
                        )

                        searchResults.playlists.take(if (showAllPlaylists) Int.MAX_VALUE else resultLimit)
                            .forEach { playlist ->
                                PlaylistCard(
                                    modifier = Modifier
                                        .clickable {
                                            onPlaylistClick(playlist)
                                            expanded = false
                                            textFieldState.edit { replace(0, length, "") }
                                        },
                                    playlistDescription = playlist.description ?: "No description",
                                    playlistName = playlist.title,
                                    shape = RoundedCornerShape(0.dp),
                                )
                            }
                        if (!showAllPlaylists && searchResults.playlists.size > resultLimit) {
                            Text(
                                text = "Show all ${searchResults.playlists.size} playlists",
                                modifier = Modifier
                                    .clickable { showAllPlaylists = true }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (searchResults.albums.isNotEmpty()) {
                        ListItem(
                            modifier = Modifier.semantics { traversalIndex = 3f },
                            headlineContent = { Text("Albums") },
                            trailingContent = { Text("${searchResults.albums.size} results") }
                        )
                        searchResults.albums.take(if (showAllAlbums) Int.MAX_VALUE else resultLimit)
                            .forEach { album ->
                                ListItem(
                                    modifier = Modifier,
                                    headlineContent = { Text(album.title) },
                                    trailingContent = { Text("View Album") }
                                )
                            }
                        if (!showAllAlbums && searchResults.albums.size > resultLimit) {
                            Text(
                                text = "Show all ${searchResults.albums.size} albums",
                                modifier = Modifier
                                    .clickable { showAllAlbums = true }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun SimpleSearchBarPreview() {
    val textFieldState = TextFieldState("Search")

    SimpleSearchBar(
        textFieldState = textFieldState,
        onSearch = { /* Handle search */ },
        searchResults = SearchResult(),
        modifier = Modifier.fillMaxWidth()
    )
}
