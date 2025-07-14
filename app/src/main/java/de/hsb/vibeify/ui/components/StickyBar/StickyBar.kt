package de.hsb.vibeify.ui.components.StickyBar

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.demo.compose.buttons.PlayPauseButton
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import de.hsb.vibeify.R
import de.hsb.vibeify.core.navigation.navigateToPlayback
import de.hsb.vibeify.ui.player.controls.StopButton


//StickyBar is a composable that displays a sticky bar at the bottom of the screen containing information about the currently playing song.
@Composable
fun StickyBar(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: StickyBarViewModel = hiltViewModel()
    val position by viewModel.position.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val song by viewModel.currentSong.collectAsState()
    val playbackState by viewModel.playerState.collectAsState()
    val player = viewModel.mediaController.collectAsState().value

    val isFavorite by viewModel.isCurrentSongFavorite.collectAsState(
        initial = false
    )

    // State variables to manage the visibility of the sticky bar and the slider position
    var showBar by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }


    // Update the slider position based on the current position and duration
    LaunchedEffect(position, duration, isUserSeeking) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = position / duration.toFloat()
        }
    }

    // Show the sticky bar when the playback state is not idle or ended
    LaunchedEffect(playbackState) {
        showBar = playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED
    }

    if (showBar && song != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { navController.navigateToPlayback() }

        ) {
            Column() {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 2.dp)
                ) {

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song!!.imageUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(56.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = song!!.name,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                repeatDelayMillis = 3500,
                                                initialDelayMillis = 3000,
                                                velocity = 28.dp
                                            )
                                    )
                                    if (isFavorite) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Favorite Icon",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                song!!.artist?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            repeatDelayMillis = 3500,
                                            initialDelayMillis = 3000,
                                            velocity = 28.dp
                                        )
                                    )
                                }
                            }
                        }


                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (player != null) {
                        PlayPauseButton(player)
                        StopButton(player)
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }
                // Slider to control the playback position
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        isUserSeeking = true
                    },
                    onValueChangeFinished = {
                        val seekPos = (sliderPosition * duration).toLong()
                        viewModel.seekTo(seekPos)
                        isUserSeeking = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
        }
    }
}