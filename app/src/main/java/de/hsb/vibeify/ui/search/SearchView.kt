package de.hsb.vibeify.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SearchView(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    Box(modifier = modifier) {
        Button(onClick = { navController.navigate("playback_view") }, modifier = modifier) {
            Text("Outkast - Hey Ya! 340p")
        }
    }
}