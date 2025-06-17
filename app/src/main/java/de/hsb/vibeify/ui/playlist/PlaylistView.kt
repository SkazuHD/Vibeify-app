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

        Button(onClick = { navController.navigate(Destinations.PlaylistDetailView.route) }) {
            Text("Zur Playlist Detailansicht")
        }
    }
}

@Preview
@Composable
fun PlaylistViewPreview() {
    // Dummy NavController für Preview
    PlaylistView(navController = TestNavHostController(androidx.compose.ui.platform.LocalContext.current))
}