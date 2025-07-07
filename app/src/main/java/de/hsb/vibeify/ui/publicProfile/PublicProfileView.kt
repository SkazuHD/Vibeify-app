package de.hsb.vibeify.ui.publicProfile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.core.navigation.navigateToPlaylistDetail
import de.hsb.vibeify.ui.components.Avatar
import de.hsb.vibeify.ui.components.NoContentCard
import de.hsb.vibeify.ui.components.playlistCard.PlaylistCardVM
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.error != null -> {
                Text(text = "Error: ${uiState.error}")
            }

            else -> {
                Row() {
                    if (uiState.user?.imageUrl?.isNotBlank() == true) {
                        Avatar(
                            modifier = Modifier
                                .padding(bottom = 16.dp, top = 16.dp)
                                .size(170.dp),
                            initials = "JL",
                            imageUrl = uiState.user.imageUrl,
                        )
                    } else {
                        Avatar(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .size(170.dp)
                                .padding(top = 16.dp),
                            initials = uiState.user?.name?.take(2) ?: "AB",
                        )
                    }

                    uiState.user?.let { user ->
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp, end = 16.dp)
                        ) {
                            Text(
                                text = user.name.ifBlank { user.email },
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 24.sp,
                            )
                            Row {
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

                Text(
                    text = "Their Playlists",
                    modifier = Modifier.padding(top = 30.dp, bottom = 8.dp),
                    fontSize = 20.sp
                )

                if (playlists.isEmpty()) {
                    NoContentCard(
                        modifier = Modifier
                            .padding(8.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = "No Playlists",
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = "This user has no playlists yet.",
                    )

                } else {
                    val resultLimit = 3
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
