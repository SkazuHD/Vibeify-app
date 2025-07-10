package de.hsb.vibeify.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.R
import de.hsb.vibeify.core.navigation.navigateToPlaylistDetail
import de.hsb.vibeify.core.navigation.navigateToPublicProfile
import de.hsb.vibeify.ui.components.LiveFriends.LiveFriendView
import de.hsb.vibeify.ui.components.LoadingIndicator
import de.hsb.vibeify.ui.components.NoContentCard
import de.hsb.vibeify.ui.components.SurpriseCard
import de.hsb.vibeify.ui.components.playlistCard.PlaylistCardVM
import de.hsb.vibeify.ui.components.songCard.SmartSongCard
import de.hsb.vibeify.ui.components.songCard.TrendingSongCard
import de.hsb.vibeify.ui.player.PlaybackViewModel


@Composable
fun MainView(
    navController: NavController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel = hiltViewModel()
) {
    val uiState = mainViewModel.uiState.collectAsState()
    val isLoadingActivities = uiState.value.isLoadingActivities
    val isLoadingRecommendations = uiState.value.isLoadingRecommendations
    val recentActivityItems = uiState.value.recentActivityItems
    val recommendations = uiState.value.recommendations


    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = { Text("Friend Activities") },
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .size(160.dp)
            ) {
                LiveFriendView(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { friendId ->
                        navController.navigateToPublicProfile(friendId)
                    }
                )

            }
        }
        item {
            Column {
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineContent = { Text("Discover Something new") },
                )
                SurpriseCard(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                headlineContent = { Text("Recent Activities") },
            )
        }
        item {
            if (isLoadingActivities) {
                LoadingIndicator(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                )
            } else {
                RecentActivityGrid(
                    recentActivityItems = recentActivityItems,
                    navController = navController,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (isLoadingRecommendations) {
            item {
                LoadingIndicator(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                )
            }
        } else if (recommendations.isEmpty()) {
            item {
                NoContentCard(
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SentimentNeutral,
                            contentDescription = " No recommendations",
                            modifier = Modifier.size(64.dp),
                        )
                    },
                    title = "No Recommendations",
                )
            }
        } else {
            item {
                ListItem(
                    modifier = Modifier.fillMaxWidth(),
                    headlineContent = { Text("Recommendations") },
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendations) { song ->
                        TrendingSongCard(
                            song = song,
                            onClick = {
                                playbackViewModel.play(song)
                            },
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun RecentActivityGrid(
    recentActivityItems: List<RecentActivityItem>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (recentActivityItems.isEmpty()) {
        NoContentCard(
            modifier = modifier.fillMaxWidth(),
            icon = {
                Icon(
                    imageVector = Icons.Default.SentimentNeutral,
                    contentDescription = " No recent activities",
                    modifier = Modifier.size(64.dp),
                )
            },
            title = "No Recent Activities",
        )


    } else {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 2
        ) {
            recentActivityItems.forEach { activityItem ->
                when (activityItem) {
                    is RecentActivityItem.SongActivity -> {
                        SmartSongCard(
                            song = activityItem.song,
                            showMenu = false,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    is RecentActivityItem.PlaylistActivity -> {
                        PlaylistCardVM(
                            playlistName = activityItem.playlist.title,
                            playlistDescription = activityItem.playlist.description ?: "",
                            playlistIcon = R.drawable.ic_launcher_foreground,
                            playlistImage = activityItem.playlist.imageUrl,
                            modifier = Modifier.weight(1f),
                            playlistId = activityItem.playlist.id,
                            showArrow = false,
                            onClick = {
                                navController.navigateToPlaylistDetail(activityItem.playlist.id)
                            }
                        )
                    }
                }
            }
        }
    }
}