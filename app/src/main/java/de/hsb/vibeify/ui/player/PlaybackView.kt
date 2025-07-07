package de.hsb.vibeify.ui.player

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.demo.compose.buttons.NextButton
import androidx.media3.demo.compose.buttons.PlayPauseButton
import androidx.media3.demo.compose.buttons.PreviousButton
import androidx.media3.demo.compose.buttons.RepeatButton
import androidx.media3.demo.compose.buttons.ShuffleButton
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.ui.components.songCard.SongCard
import kotlinx.coroutines.delay

@Composable
fun MinimalMusicPlayer(
    nextSong: String = "Next Songs",
    playbackViewModel: PlaybackViewModel = hiltViewModel()
) {
    val currentSong by playbackViewModel.currentSong.collectAsState()
    val nextSongs = playbackViewModel.upcomingSongs.collectAsState()
    val currentSongList by playbackViewModel.currentSongList.collectAsState()

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        PlayerHero(currentSong, playbackViewModel)

        Spacer(modifier = Modifier.height(24.dp))

        PlayerControls(playbackViewModel)

        Spacer(modifier = Modifier.height(24.dp))


        Text(
            text = nextSong,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(nextSongs.value) { song ->
                SongCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        playbackViewModel.play(
                            currentSongList,
                            currentSongList.indexOf(song),
                            playbackViewModel.currentPlaylistId.value
                        )
                    },
                    title = song.name,
                    artist = song.artist ?: "Unknown Artist",
                    showMenu = false,
                    isSongFavorite = playbackViewModel.isSongFavorite(song),
                    songImageUrl = song.imageUrl,
                )
            }
        }
    }
}


@Composable
fun PlayerHero(currentSong: Song?, playbackViewModel: PlaybackViewModel = hiltViewModel()) {
    val isFavorite by playbackViewModel.isCurrentSongFavorite.collectAsState(
        initial = false
    )

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(currentSong?.imageUrl)
            .crossfade(true)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = "Song Cover",
        modifier = Modifier.fillMaxWidth(
            0.65f
        )
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(0.65f)) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            currentSong?.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 3500,
                        initialDelayMillis = 3000,
                        velocity = 28.dp
                    )
                )
            }

            currentSong?.artist?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 3500,
                        initialDelayMillis = 3000,
                        velocity = 28.dp
                    )
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            IconButton(
                onClick = {
                    playbackViewModel.toggleFavorite()
                },
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerControls(playbackViewModel: PlaybackViewModel = hiltViewModel()) {
    PlayerSlider(playbackViewModel)
    PlayerButtons(playbackViewModel)
}

@Composable
fun PlayerButtons(
    playbackViewModel: PlaybackViewModel = hiltViewModel()
) {

    val player = playbackViewModel.mediaController.collectAsState().value

    Row(
        horizontalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (player != null) {
            ShuffleButton(player)
            PreviousButton(player)
            PlayPauseButton(player)
            NextButton(player)
            RepeatButton(player)
        }
    }
}

@Composable
fun PlayerSlider(playbackViewModel: PlaybackViewModel = hiltViewModel()) {
    val player = playbackViewModel.mediaController.collectAsState().value

    var sliderPosition by remember { mutableStateOf(0f) }
    val position by playbackViewModel.position.collectAsState()
    val duration by playbackViewModel.duration.collectAsState()
    var isUserSeeking by remember { mutableStateOf(false) }
    val isPlaying by playbackViewModel.isPlaying.collectAsState()

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!isUserSeeking) {
                sliderPosition = position / duration.toFloat()
            }
            delay(300)
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                isUserSeeking = true
            },
            onValueChangeFinished = {
                val seekPos = (sliderPosition * duration).toLong()
                playbackViewModel.seekTo(seekPos)
                isUserSeeking = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(position), color = MaterialTheme.colorScheme.onBackground)
            Text(formatTime(duration), color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0)
        "%d:%02d:%02d".format(hours, minutes, seconds)
    else
        "%d:%02d".format(minutes, seconds)
}
