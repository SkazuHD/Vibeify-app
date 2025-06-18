package de.hsb.vibeify.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import de.hsb.vibeify.R
import de.hsb.vibeify.ui.components.PlaylistCard

@Composable
fun PlaylistView(modifier : Modifier = Modifier, navController: NavController, viewModel: PlaylistViewModel = hiltViewModel()) {

    val  playlists = viewModel.playlists

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) {
                PlaylistCard(
                    playlistDescription = it.description ?: "",
                    playlistName = it.title,
                    playlistIcon = it.imagePath ?: R.drawable.ic_launcher_foreground,
                    onClick = {
                        navController.navigate("playlist_detail_view/${it.id}")
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PlaylistViewPreview() {
    PlaylistView(navController = TestNavHostController(androidx.compose.ui.platform.LocalContext.current))
}