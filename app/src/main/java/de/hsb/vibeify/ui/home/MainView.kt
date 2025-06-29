package de.hsb.vibeify.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SurpriseCard(
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
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
        }
        item {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = { Text("Recent Activities") },
            )
        }
        item {
            if (isLoading.value) {
                LoadingIndicator(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    recentActivityItems.value.forEach { activityItem ->
                        when (activityItem) {
                            is RecentActivityItem.SongActivity -> {
                                SmartSongCard(
                                    song = activityItem.song,
                                    showMenu = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            is RecentActivityItem.PlaylistActivity -> {
                                PlaylistCard(
                                    playlistName = activityItem.playlist.title,
                                    playlistDescription = activityItem.playlist.description
                                        ?: "Playlist von Vibeify",
                                    playlistIcon = activityItem.playlist.imagePath
                                        ?: R.drawable.ic_launcher_foreground,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate("playlist_detail_view/${activityItem.playlist.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = { Text("Recommendations") },
            )
        }
        item {
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

}