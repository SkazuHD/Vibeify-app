package de.hsb.vibeify.ui.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import de.hsb.vibeify.core.Destinations

@Composable
fun PlaylistView(navController: NavController) {
    Column {
        Text(
            text = "Playlist View",
            modifier = androidx.compose.ui.Modifier,
        )

        val examplePlaylistId = "123"
        Button(onClick = { navController.navigate("playlist_detail_view/$examplePlaylistId") }) {
            Text("Zur Playlist Detailansicht")
        }
    }
}

@Preview
@Composable
fun PlaylistViewPreview() {
    PlaylistView(navController = TestNavHostController(androidx.compose.ui.platform.LocalContext.current))
}