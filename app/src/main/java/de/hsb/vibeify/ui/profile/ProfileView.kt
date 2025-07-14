package de.hsb.vibeify.ui.profile

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import de.hsb.vibeify.core.AuthViewModel
import de.hsb.vibeify.core.navigation.navigateToPlaylistDetail
import de.hsb.vibeify.core.navigation.navigateToPublicProfile
import de.hsb.vibeify.ui.components.Avatar
import de.hsb.vibeify.ui.components.LoadingIndicator
import de.hsb.vibeify.ui.components.NoContentCard
import de.hsb.vibeify.ui.components.playlistCard.PlaylistCardVM

@OptIn(UnstableApi::class)
@Composable
fun ProfileView(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlists = uiState.playlists
    val followers = viewModel.followersFlow.collectAsState().value
    val following = viewModel.followingFlow.collectAsState().value

    val openDialog = remember { mutableStateOf(false) }
    val openFollowDialog = remember { mutableStateOf(false) }
    val openFollowersDialog = remember { mutableStateOf(false) }

    val resultLimit = 3

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        item {
            //profile header section: avatar, name, followers/following, edit button
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(text = "Error: ${uiState.error}")
                }

                openDialog.value -> {
                    EditProfileDialog(onDismiss = { openDialog.value = false })
                }

                openFollowDialog.value -> {
                    FollowDialog(
                        onDismissRequest = { openFollowDialog.value = false },
                        user = uiState.user,
                        followType = FollowType.FOLLOWING,
                        onProfileClick = { user ->
                            Log.d("ProfileView", "onProfileClick: $user")
                            openFollowDialog.value = false
                            navController.navigateToPublicProfile(user.id)
                        }
                    )
                }

                openFollowersDialog.value -> {
                    FollowDialog(
                        onDismissRequest = { openFollowersDialog.value = false },
                        user = uiState.user,
                        followType = FollowType.FOLLOWERS,
                        onProfileClick = { user ->

                            Log.d("ProfileView", "onProfileClick: $user")
                            openFollowersDialog.value = false
                            navController.navigateToPublicProfile(user.id)
                        }
                    )
                }

                else -> {
                    Row() {
                        if (uiState.user?.imageUrl?.isNotBlank() == true) {
                            Avatar(
                                modifier = Modifier
                                    .padding(bottom = 16.dp, top = 16.dp)
                                    .size(170.dp),
                                initials = "JL",
                                imageUrl = uiState.user?.imageUrl,
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
                                    style = TextStyle.Default.copy(
                                        lineBreak = LineBreak.Paragraph.copy(
                                            strategy = LineBreak.Strategy.Balanced
                                        )
                                    )
                                )
                                FlowRow {
                                    Text(
                                        "Followers: ${followers.size}",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clickable(
                                                onClick = {
                                                    openFollowersDialog.value = true
                                                }
                                            )
                                    )
                                    Text(
                                        "Following: ${following.size}",
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clickable(
                                                onClick = {
                                                    openFollowDialog.value = true
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                    Row {
                        Button(
                            onClick = {
                                openDialog.value = true
                            },
                            modifier = Modifier
                                .padding(end = 8.dp, start = 26.dp)
                                .height(35.dp)
                                .width(120.dp)
                        ) {
                            Text(
                                text = "Edit Profile",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Playlists section
        item {
            Text(
                text = "Your Playlists",
                modifier = Modifier.padding(top = 30.dp, bottom = 8.dp),
                fontSize = 20.sp
            )
        }
        if (uiState.isLoadingPlaylist) {
            item {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                )
            }
        } else if (playlists.isEmpty()) {
            item {
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
                    title = "You have no playlists yet. Create one to get started!",
                )
            }
        } else {
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
        item {
            Button(
                onClick = {
                    authViewModel.signOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Sign Out")
            }
        }
    }
}

// CollapsableList is a reusable composable that displays a list of items with a "Show All" toggle
@Composable
fun <T> CollapsableList(
    resultLimit: Int = 3,
    items: List<T> = emptyList(),
    content: @Composable (item: T) -> Unit
) {
    var showAll by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.take(if (showAll) Int.MAX_VALUE else resultLimit)
            .forEach { item ->
                content(item)
            }
        if (items.size > resultLimit) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (showAll) "Show Less" else "Show All",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { showAll = !showAll }
                        .padding(8.dp)
                )
            }
        }
    }
}
