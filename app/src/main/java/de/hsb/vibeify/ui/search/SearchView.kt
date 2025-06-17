package de.hsb.vibeify.ui.search

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SearchView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Button(onClick = { navController.navigate("playback_view") }, modifier = modifier) {
        Text("Outkast - Hey Ya! 340p")
    }
}