package de.hsb.vibeify.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
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
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.ui.components.songCard.SongCard
import kotlinx.coroutines.delay


val fakeSong = Song(
    name = "Outkast - Hey Ya!",
    artist = "Outkast",
    album = "Speakerboxxx/The Love Below",
    duration = 235,
    filePath = "asset:///Bread.mp3",
    imageUrl = "https://www.uni-due.de/imperia/md/images/pom/allgemeines/fittosize__600_0_8ff8ec8e0fac546f5c41889afe1d97d9_jonas_foto.jpg"
)

@Composable
fun MinimalMusicPlayer(
    nextSong: String = "Next Songs",
    playbackViewModel: PlaybackViewModel = hiltViewModel()
) {
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val position by playbackViewModel.position.collectAsState()
    val duration by playbackViewModel.duration.collectAsState()
    val currentSong by playbackViewModel.currentSong.collectAsState()
    val nextSongs = playbackViewModel.upcomingSongs.collectAsState()
    val currentSongList by playbackViewModel.currentSongList.collectAsState()
    val playbackMode by playbackViewModel.playbackMode.collectAsState()
    val repeatMode by playbackViewModel.repeatMode.collectAsState()
    val isFavorite by playbackViewModel.isCurrentSongFavorite.collectAsState(
        initial = false
    )

    var sliderPosition by remember { mutableStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!isUserSeeking) {
                sliderPosition = position / duration.toFloat()
            }
            delay(300)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                Column {
                    currentSong?.name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    currentSong?.artist?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 16.dp)
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


            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { playbackViewModel.togglePlaybackMode() },
                    ) {
                        when (playbackMode) {
                            PlaybackViewModel.PlaybackMode.SHUFFLE -> Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )

                            PlaybackViewModel.PlaybackMode.NONE -> Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Kein Shuffle",
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        IconButton(onClick = {
                            playbackViewModel.skipToPrevious()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voriger Song",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        IconButton(onClick = {
                            if (isPlaying) {
                                playbackViewModel.pause()
                            } else {
                                playbackViewModel.resume()
                            }
                        }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(onClick = {
                            playbackViewModel.skipToNext()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Nächster Song",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { playbackViewModel.toggleRepeatMode() },

                        ) {

                        when (repeatMode) {
                            PlaybackViewModel.RepeatMode.ALL -> Icon(
                                imageVector = Icons.Filled.Repeat,
                                contentDescription = "loop entire playlist",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )

                            PlaybackViewModel.RepeatMode.LOOP -> Icon(
                                imageVector = Icons.Filled.RepeatOne,
                                contentDescription = "Loop 1 time",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )

                            PlaybackViewModel.RepeatMode.NONE -> Icon(
                                imageVector = Icons.Filled.Repeat,
                                contentDescription = "None",
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )

                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

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
