package de.hsb.vibeify.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.R
import de.hsb.vibeify.ui.components.LoadingIndicator
import de.hsb.vibeify.ui.components.PlaylistCard
import de.hsb.vibeify.ui.components.SurpriseCard
import de.hsb.vibeify.ui.components.songCard.SmartSongCard

@Composable
fun MainView(
    navController: NavController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {

    val recentActivityItems = mainViewModel.recentActivityItems.collectAsState()
    val isLoading = mainViewModel.isLoading.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        SurpriseCard(
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                )
        ) {
            Text("Friends will be displayed here", textAlign = TextAlign.Center)
        }

        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = { Text("Recent Activities") },
        )
        if (isLoading.value) {
            LoadingIndicator(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 175.dp)
            ) {
                items(recentActivityItems.value.size) { index ->
                    val activityItem = recentActivityItems.value[index]
                    when (activityItem) {
                        is RecentActivityItem.SongActivity -> {
                            SmartSongCard(
                                song = activityItem.song,
                                showMenu = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        is RecentActivityItem.PlaylistActivity -> {
                            PlaylistCard(
                                playlistName = activityItem.playlist.title,
                                playlistDescription = activityItem.playlist.description
                                    ?: "Playlist von Vibeify",
                                playlistIcon = activityItem.playlist.imagePath
                                    ?: R.drawable.ic_launcher_foreground,
                                modifier = Modifier,
                                onClick = {
                                    navController.navigate("playlist_detail_view/${activityItem.playlist.id}")
                                }
                            )
                        }
                    }
                }
            }
        }

        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = { Text("Recommendations") },
        )
        // Placeholder BOX
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                )
        ) {
            Text("Recommendations will be displayed here", textAlign = TextAlign.Center)
        }

    }


}