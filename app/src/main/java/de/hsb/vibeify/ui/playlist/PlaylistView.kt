package de.hsb.vibeify.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import de.hsb.vibeify.R
import de.hsb.vibeify.ui.components.playlistCard.PlaylistCard
import de.hsb.vibeify.ui.playlist.dialogs.CreatePlaylistDialog

@Composable
fun PlaylistView(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PlaylistViewModel = hiltViewModel()
) {

    val playlists = viewModel.playlists.collectAsState()
    val openDialog = remember { mutableStateOf(false) }

    Box(modifier = modifier) {


        when {
            openDialog.value -> {
                CreatePlaylistDialog(
                    onDismissRequest = {
                        openDialog.value = false
                    })
            }
        }


        Column {
            Row {
                Text(
                    text = "Playlists",
                    modifier = Modifier
                        .padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        openDialog.value = true
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Create Playlist")
                }

            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = playlists.value,
                    key = { playlist -> playlist.id }
                ) { playlist ->
                    PlaylistCard(
                        playlistDescription = playlist.description ?: "",
                        playlistName = playlist.title,
                        playlistIcon = R.drawable.ic_launcher_foreground,
                        playlistId = playlist.id,
                        playlistImage = playlist.imageUrl,
                        onClick = {
                            navController.navigate("playlist_detail_view/${playlist.id}")
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PlaylistViewPreview() {
    PlaylistView(navController = TestNavHostController(androidx.compose.ui.platform.LocalContext.current))
}