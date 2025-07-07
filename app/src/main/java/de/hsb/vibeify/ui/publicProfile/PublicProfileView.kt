package de.hsb.vibeify.ui.publicProfile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.core.navigation.navigateToPlaylistDetail
import de.hsb.vibeify.ui.components.Avatar
import de.hsb.vibeify.ui.components.NoContentCard
import de.hsb.vibeify.ui.components.playlistCard.PlaylistCardVM
import de.hsb.vibeify.ui.components.songCard.SmartSongCard
import de.hsb.vibeify.ui.profile.CollapsableList

@Composable
fun PublicProfileView(
    modifier: Modifier = Modifier,
    userId: String,
    viewModel: PublicProfileViewModel = hiltViewModel(),
    navController: NavController
) {

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
        viewModel.loadPlaylists(userId)
    }

    val uiState = viewModel.uiState.collectAsState().value
    val playlists = uiState.playlists
    val isFollowing = viewModel.isFollowing.collectAsState(initial = false).value
    val followers = viewModel.followersFlow.collectAsState().value
    val following = viewModel.followingFlow.collectAsState().value
    val liveFriend = viewModel.liveFriendFlow.collectAsState().value
    val status = liveFriend?.isOnline ?: false
    val currentSong = liveFriend?.currentSong

    when {
        uiState.isLoading -> {
            CircularProgressIndicator(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }

        uiState.error != null -> {
            Text(
                text = "Error: ${uiState.error}",
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                // Profile Header Section
                item {
                    Row {

                        Box(
                            modifier = Modifier.padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Avatar(
                                modifier = Modifier
                                    .padding(bottom = 16.dp, top = 16.dp)
                                    .size(170.dp),
                                initials = "JL",
                                imageUrl = uiState.user?.imageUrl,
                            )

                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = "Online Status",
                                tint = if (status) Color(0xFF5afc03) else Color.Gray,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-24).dp, y = (-12).dp)
                            )

                        }

                        uiState.user?.let { user ->
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp, end = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = user.name.ifBlank { user.email },
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontSize = 24.sp,
                                )
                                FlowRow {
                                    Text(
                                        "Followers: ${followers.size}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                    Text(
                                        "Following: ${following.size}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row {
                        Button(
                            onClick = {
                                if (isFollowing) {
                                    viewModel.unfollowUser(userId)
                                } else {
                                    viewModel.followUser(userId)
                                }
                            },
                            modifier = Modifier
                                .padding(end = 8.dp, start = 26.dp)
                                .height(35.dp)
                                .width(120.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Unfollow" else "Follow",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "Currently Listening to",
                        modifier = Modifier.padding(top = 30.dp, bottom = 8.dp),
                        fontSize = 20.sp
                    )
                    if (currentSong != null)
                        SmartSongCard(
                            song = currentSong
                        )
                }

                item {
                    Text(
                        text = "Their Playlists",
                        modifier = Modifier.padding(top = 30.dp, bottom = 8.dp),
                        fontSize = 20.sp
                    )
                }

                // Playlists Content
                if (playlists.isEmpty()) {
                    item {
                        NoContentCard(
                            modifier = Modifier.padding(8.dp),
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.SentimentNeutral,
                                    contentDescription = "No Playlists",
                                    modifier = Modifier.size(48.dp)
                                )
                            },
                            title = "This user has no playlists yet.",
                        )
                    }
                } else {
                    val resultLimit = 3
                    item {
                        CollapsableList(
                            resultLimit = resultLimit,
                            items = playlists,
                            content = { playlist ->
                                PlaylistCardVM(
                                    onClick = {
                                        navController.navigateToPlaylistDetail(playlist.id)
                                    },
                                    playlistDescription = playlist.description ?: "No description",
                                    playlistName = playlist.title,
                                    playlistId = playlist.id,
                                    playlistImage = playlist.imageUrl,
                                    shape = RoundedCornerShape(8.dp),
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
