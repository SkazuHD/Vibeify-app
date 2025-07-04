package de.hsb.vibeify.ui.profile

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.hsb.vibeify.R
import de.hsb.vibeify.core.AuthViewModel
import de.hsb.vibeify.ui.components.Avatar
import de.hsb.vibeify.ui.components.playlistCard.PlaylistCard

@Composable
fun ProfileView(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlists = viewModel.playlists
    val followers = viewModel.followersFlow.collectAsState().value
    val following = viewModel.followingFlow.collectAsState().value

    val openDialog = remember { mutableStateOf(false) }
    val openFollowDialog = remember { mutableStateOf(false) }
    val openFollowersDialog = remember { mutableStateOf(false) }

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
                        navController.navigate("public_profile/${user.id}")
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
                        navController.navigate("public_profile/${user.id}")

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
                            Row {
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

                Text(
                    text = "Your Playlists",
                    modifier = Modifier.padding(top = 30.dp, bottom = 8.dp),
                    fontSize = 20.sp
                )

                if (playlists.isEmpty()) {
                    Text(
                        text = "You have no playlists yet. Create one to get started!",
                        modifier = Modifier.padding(bottom = 16.dp, top = 10.dp),
                        fontSize = 15.sp
                    )
                } else {
                    Box(modifier = modifier) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(playlists) {
                                PlaylistCard(
                                    playlistDescription = it.description ?: "",
                                    playlistName = it.title,
                                    playlistIcon = R.drawable.ic_launcher_foreground,
                                    playlistImage = it.imageUrl,
                                    playlistId = it.id,
                                    onClick = {
                                        navController.navigate("playlist_detail_view/${it.id}")
                                    }
                                )
                            }
                        }
                    }
                }


                Button(
                    onClick = {
                        authViewModel.signOut()
                    },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Sign Out")
                }


            }
        }
    }

}