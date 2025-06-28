package de.hsb.vibeify.ui.playlist.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import de.hsb.vibeify.R
import de.hsb.vibeify.services.PlayerService
import de.hsb.vibeify.ui.components.SmartSongCard
import de.hsb.vibeify.ui.player.PlaybackViewModel

fun demoPlayerOnClick(context: android.content.Context) {
    println("Player clicked - demo action")
    val player = PlayerService.getInstance(context)
    val mediaitem = MediaItem.fromUri("https://s3.amazonaws.com/scifri-episodes/scifri20181123-episode.mp3")
    player.setMediaItem(mediaitem)
    player.prepare()
    player.playWhenReady = true
    player.play()

}

@Composable
fun PlaylistDetailView(
    modifier: Modifier = Modifier,
    playlistId: String,
    playlistDetailViewModel: PlaylistDetailViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel = hiltViewModel()
) {
    LaunchedEffect(playlistId) {
        playlistDetailViewModel.loadPlaylist(playlistId)
    }

    val playlistTitle = playlistDetailViewModel.playlistTitle
    val playlistDescription = playlistDetailViewModel.playlistDescription
    val playlistImage = playlistDetailViewModel.playlistImage
    val songs by playlistDetailViewModel.songs.collectAsState()
    val playlistDurationText by playlistDetailViewModel.playlistDurationText.collectAsState()
    val isFavorite = playlistDetailViewModel.isFavorite
    val isFavoriteAble = playlistDetailViewModel.isFavoriteAble
    val isLoadingSongs = playlistDetailViewModel.isLoadingSongs

    //Playlist Header
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth())
        {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        painter = painterResource(id = playlistImage),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Playlist Image",
                        modifier = Modifier
                            .size(100.dp)
                    )
                    IconButton(
                        onClick = {
                            playbackViewModel.play(songs)
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .alpha(0.8f)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                }
                Column {
                    Text(
                        text = playlistTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        text = playlistDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                    Row {
                        Text(
                            text = "${songs.size} Songs",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                        )
                        Text(
                            text = playlistDurationText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )

                    }
                }
                if(isFavoriteAble){
                    Column {
                        Box {
                            IconButton(
                                onClick = {
                                    playlistDetailViewModel.toggleFavorite(playlistId)
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp)

                            ) {
                                Icon(
                                    imageVector = isFavorite
                                        .let { if (it) Icons.Default.Favorite else Icons.Default.FavoriteBorder },
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }

            }
        }

        // Content area - optimized for performance
        if (isLoadingSongs) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = songs,
                    key = { song -> song.id } // Important: Add key for performance
                ) { song ->
                    SmartSongCard(
                        song = song,
                        songIcon = R.drawable.ic_launcher_foreground,
                        onClick = {
                            playbackViewModel.play(songs, songs.indexOf(song))
                        },
                        playbackViewModel = playbackViewModel,
                        playlistDetailViewModel = playlistDetailViewModel
                    )
                }
            }
        }
    }
}
