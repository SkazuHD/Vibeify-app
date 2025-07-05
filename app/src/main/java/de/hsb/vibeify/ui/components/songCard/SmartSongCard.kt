package de.hsb.vibeify.ui.components.songCard

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.R
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.ui.components.MenuOption
import de.hsb.vibeify.ui.player.PlaybackViewModel
import de.hsb.vibeify.ui.playlist.dialogs.AddSongToPlaylistDialog

@Composable
fun SmartSongCard(
    song: Song,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    songIcon: Int = R.drawable.ic_launcher_foreground,
    showMenu: Boolean = true,
    additionalMenuOptions: List<MenuOption> = emptyList(),
    smartSongCardViewModel: SmartSongCardViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel = hiltViewModel(),
    onClick: () -> Unit = {
        playbackViewModel.play(song)
    },
) {
    val isSongFavoriteInitial = remember(song.id) {
        smartSongCardViewModel.isSongFavorite(song)
    }
    var isSongFavorite by remember(song.id) {
        mutableStateOf(isSongFavoriteInitial)
    }
    var openAddToPlaylistDialog by remember(song.id) {
        mutableStateOf(false)
    }

    val baseMenuOptions = remember(isSongFavorite, song.id) {
        listOf(
            MenuOption(text = "Abspielen", icon = Icons.Default.PlayArrow, onClick = {
                playbackViewModel.play(song)
            }),
            if (isSongFavorite) {
                MenuOption(
                    text = "Aus Favoriten entfernen",
                    icon = Icons.Default.Favorite,
                    onClick = {
                        smartSongCardViewModel.removeSongFromFavorites(song)
                        isSongFavorite = false
                    },
                )

            } else {
                MenuOption(
                    text = "Zu Favoriten hinzufügen",
                    icon = Icons.Default.FavoriteBorder,
                    onClick = {
                        smartSongCardViewModel.addSongToFavorites(song)
                        isSongFavorite = true
                    })
            },
            MenuOption(
                text = "Zu playlist hinzufügen",
                icon = Icons.Default.LibraryAdd,
                onClick = {
                    openAddToPlaylistDialog = true
                }
            )
        )
    }

    when {
        openAddToPlaylistDialog -> {
            AddSongToPlaylistDialog(onDismissRequest = {
                openAddToPlaylistDialog = false
            }, song = song)
        }
    }

    val allMenuOptions = remember(baseMenuOptions, additionalMenuOptions) {
        baseMenuOptions + additionalMenuOptions
    }

    SongCard(
        title = song.name,
        artist = song.artist ?: "Unknown Artist",
        modifier = modifier,
        shape = shape,
        onClick = {
            onClick()
        },
        isSongFavorite = isSongFavorite,
        songIcon = songIcon,
        songImageUrl = song.imageUrl,
        showMenu = showMenu,
        menuOptions = allMenuOptions
    )
}
