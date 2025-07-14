package de.hsb.vibeify.ui.playlist.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.ui.components.playlistCard.PlaylistCard

@Composable
fun AddSongToPlaylistDialog(
    onDismissRequest: () -> Unit,
    song: Song,
    addSongToPlaylistViewModel: AddSongToPlaylistViewModel = hiltViewModel()
) {

    // Collect the list of playlists from the ViewModel
    val playlists = addSongToPlaylistViewModel.playlists.collectAsState()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Add Song to Playlist",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Display each playlist as a card
                    items(items = playlists.value, key = { playlist -> playlist.id }) { playlist ->
                        val isInPlaylist = playlist.songIds.contains(song.id)
                        PlaylistCard(
                            playlistId = playlist.id,
                            playlistName = playlist.title,
                            playlistImage = playlist.imageUrl,
                            showArrow = !isInPlaylist,
                            enabled = !isInPlaylist,
                            onClick = {
                                if (isInPlaylist) return@PlaylistCard
                                addSongToPlaylistViewModel.addSongToPlaylist(playlist.id, song.id)
                                onDismissRequest()
                            },
                            playlistDescription = playlist.description ?: "",
                            isFavorite = false,
                        )
                    }
                }
            }
        }
    }
}