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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import de.hsb.vibeify.R
import de.hsb.vibeify.services.PlayerService
import de.hsb.vibeify.ui.components.MenuOption
import de.hsb.vibeify.ui.components.SongCard
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
    val songs = playlistDetailViewModel.songs
    val playlistDurationText = playlistDetailViewModel.playlistDurationText
    val context = LocalContext.current
    val isFavorite = playlistDetailViewModel.isFavorite
    val isFavoriteAble = playlistDetailViewModel.isFavoriteAble

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
                            playbackViewModel.play(songs[0])
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
            Box() {
                if (playlistDetailViewModel.isLoadingSongs){
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(songs) {
                        var isSongFavorite by rememberSaveable { mutableStateOf(playlistDetailViewModel.isSongFavorite(it)) }
                        SongCard(
                            title = it.name,
                            artist = it.artist ?: "Unknown Artist",
                            songIcon = R.drawable.ic_launcher_foreground,
                            onClick = {
                                playbackViewModel.play(it)
                            },
                            isSongFavorite = isSongFavorite,
                            menuOptions = listOf(
                                MenuOption("Abspielen", {
                                    playbackViewModel.play(it)
                                }),
                                if (isSongFavorite) {
                                    MenuOption("Aus Favoriten entfernen", {
                                        playlistDetailViewModel.removeSongFromFavorites(it)
                                        isSongFavorite = false
                                    })
                                } else {
                                    MenuOption("Zu Favoriten hinzufügen", {
                                        playlistDetailViewModel.addSongToFavorites(it)
                                        isSongFavorite = true
                                    })
                                },
                            )
                        )
                    }

                }
            }


    }

}
