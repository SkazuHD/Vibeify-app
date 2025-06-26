package de.hsb.vibeify.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.ui.player.PlaybackViewModel
import de.hsb.vibeify.ui.playlist.AddSongToPlaylistDialog
import de.hsb.vibeify.ui.playlist.detail.PlaylistDetailViewModel

@Composable
fun SmartSongCard(
    song: Song,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    songIcon: Int = R.drawable.ic_launcher_foreground,
    showMenu: Boolean = true,
    playbackViewModel: PlaybackViewModel = hiltViewModel(),
    playlistDetailViewModel: PlaylistDetailViewModel = hiltViewModel(),
    additionalMenuOptions: List<MenuOption> = emptyList()
) {
    var isSongFavorite by rememberSaveable { mutableStateOf(playlistDetailViewModel.isSongFavorite(song)) }
    var openAddToPlaylistDialog = remember { mutableStateOf(false) }

    val baseMenuOptions = listOf(
        MenuOption(text = "Abspielen", icon = Icons.Default.PlayArrow, onClick = {
            playbackViewModel.play(song)
        }),
        if (isSongFavorite) {
            MenuOption(text = "Aus Favoriten entfernen",
                icon = Icons.Default.Favorite,
                onClick= {
                playlistDetailViewModel.removeSongFromFavorites(song)
                isSongFavorite = false
            },
               )

        } else {
            MenuOption(text = "Zu Favoriten hinzufügen", icon = Icons.Default.FavoriteBorder, onClick = {
                playlistDetailViewModel.addSongToFavorites(song)
                isSongFavorite = true
            })
        },
        MenuOption(
            text = "Zu playlist hinzufügen",
            icon = Icons.Default.LibraryAdd,
            onClick = {
                openAddToPlaylistDialog.value = true
            }
        )
    )
    when{
        openAddToPlaylistDialog.value -> {
            AddSongToPlaylistDialog(onDismissRequest = {
                openAddToPlaylistDialog.value = false
            }, song = song)
        }
    }

    val allMenuOptions = baseMenuOptions + additionalMenuOptions

    SongCard(
        title = song.name,
        artist = song.artist ?: "Unknown Artist",
        modifier = modifier,
        shape = shape,
        onClick = {
            if (onClick != {}) {
                onClick()
            } else {
                playbackViewModel.play(song)
            }
        },
        isSongFavorite = isSongFavorite,
        songIcon = songIcon,
        songImageUrl = song.imageUrl,
        showMenu = showMenu,
        menuOptions = allMenuOptions
    )
}
