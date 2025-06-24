package de.hsb.vibeify.ui.components.StickyBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.navigation.NavController
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.services.PlayerService
import kotlinx.coroutines.delay





@Composable
fun StickyBar(song: Song, navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: StickyBarViewModel = hiltViewModel()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.position.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()


    var showBar by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(position, duration, isUserSeeking) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = position / duration.toFloat()
        }
    }

    LaunchedEffect(playbackState) {
        showBar = playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED
    }

    if (showBar) {
        Box(

            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { navController.navigate("playback_view")}

        ) {
            Column(){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 2.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    song.artist?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (isPlaying) {
                        viewModel.pause()
                    } else {
                        viewModel.play()
                    }
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.stop()
                        viewModel.clearMediaItems()
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Terminate",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
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