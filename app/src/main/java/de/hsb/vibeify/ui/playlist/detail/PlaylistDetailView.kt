package de.hsb.vibeify.ui.playlist.detail

import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import de.hsb.vibeify.R
import de.hsb.vibeify.data.repository.LIKED_SONGS_PLAYLIST_ID
import de.hsb.vibeify.ui.components.MenuOption
import de.hsb.vibeify.ui.components.OptionsMenu
import de.hsb.vibeify.ui.components.songCard.SmartSongCard
import de.hsb.vibeify.ui.player.PlaybackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailView(
    modifier: Modifier = Modifier,
    playlistId: String,
    navController: NavController,
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
    val isPlaylistOwner = playlistDetailViewModel.isPlaylistOwner
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

                    AsyncImage(
                        model = ImageRequest.Builder(context = LocalContext.current)
                            .data(playlistImage)
                            .crossfade(true)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .networkCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Song Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .clip(RoundedCornerShape(8.dp))
                    )
                    IconButton(
                        onClick = {
                            playbackViewModel.play(songs, 0, playlistId)
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
                if (isFavoriteAble) {
                    Column {
                        Box {
                            IconButton(
                                onClick = {
                                    playlistDetailViewModel.togglePlaylistFavorite(playlistId)
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
                } else if (isPlaylistOwner && playlistId != LIKED_SONGS_PLAYLIST_ID) {
                    OptionsMenu(
                        menuOptions = listOf(
                            MenuOption(
                                text = "Delete Playlist",
                                icon = Icons.Default.Remove,
                                onClick = {
                                    val res = playlistDetailViewModel.removePlaylist(playlistId)
                                    Log.d("PlaylistDetailView", "Playlist removed: $res")
                                    if (
                                        res
                                    ) {
                                        navController.popBackStack()
                                    }

                                }
                            )
                        )
                    )
                }

            }
        }
        if (isLoadingSongs) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
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
                ) { song ->
                    val songCardAdditionalMenuOptions = if (isPlaylistOwner) {
                        listOf(
                            MenuOption(
                                text = "Remove from this playlist",
                                icon = Icons.Default.Remove,
                                onClick = {
                                    playlistDetailViewModel.removeSongFromPlaylist(
                                        playlistId,
                                        song = song
                                    )
                                }
                            )
                        )
                    } else {
                        emptyList()
                    }
                    SmartSongCard(
                        song = song,
                        playbackViewModel = playbackViewModel,
                        onClick = {
                            playbackViewModel.play(songs, songs.indexOf(song), playlistId)
                        },
                        additionalMenuOptions = songCardAdditionalMenuOptions
                    )
                }

            }
        }
    }
}
