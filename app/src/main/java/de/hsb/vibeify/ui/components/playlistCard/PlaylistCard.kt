package de.hsb.vibeify.ui.components.playlistCard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import de.hsb.vibeify.R

// PlaylistCard is a composable function that displays a card for a playlist.
@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* Default no-op */ },
    playlistId: String,
    playlistDescription: String = "Playlist from Vibeify",
    playlistName: String = "Absolute banger",
    playlistIcon: Int = R.drawable.ic_launcher_foreground,
    playlistImage: String? = null,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    isFavorite: Boolean = false,
    showArrow: Boolean = true,
    enabled: Boolean = true

) {
    PlaylistCardContent(
        modifier = modifier,
        onClick = onClick,
        playlistName = playlistName,
        playlistDescription = playlistDescription,
        playlistIcon = playlistIcon,
        playlistImage = playlistImage,
        shape = shape,
        isFavorite = isFavorite,
        showArrow = showArrow, enabled = enabled
    )
}

// PlaylistCardVM is a composable function that displays a card for a playlist with ViewModel support.
@Composable
fun PlaylistCardVM(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* Default no-op */ },
    playlistId: String,
    playlistDescription: String = "Playlist from Vibeify",
    playlistName: String = "Absolute banger",
    playlistIcon: Int = R.drawable.ic_launcher_foreground,
    playlistImage: String? = null,
    showArrow: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    enabled: Boolean = true,
    playlistCardViewModel: PlaylistCardViewModel = hiltViewModel()
) {
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        isFavorite = playlistCardViewModel.isPlaylistFavorite(playlistId)
    }

    PlaylistCardContent(
        modifier = modifier,
        onClick = onClick,
        playlistName = playlistName,
        playlistDescription = playlistDescription,
        playlistIcon = playlistIcon,
        playlistImage = playlistImage,
        shape = shape,
        isFavorite = isFavorite,
        showArrow = showArrow,
        enabled = enabled
    )
}

// PlaylistCardContent is a private composable function that contains the actual UI for the playlist card.
@Composable
private fun PlaylistCardContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    playlistName: String,
    playlistDescription: String,
    playlistIcon: Int,
    playlistImage: String?,
    shape: RoundedCornerShape,
    isFavorite: Boolean,
    showArrow: Boolean = true,
    enabled: Boolean = true
) {

    val context = LocalContext.current
    val imageRequest = remember(playlistImage) {
        if (!playlistImage.isNullOrEmpty()) {
            ImageRequest.Builder(context)
                .data(playlistImage)
                .crossfade(false) // Disable crossfade for better performance
                .error(R.drawable.ic_launcher_foreground)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .build()
        } else null
    }

    Card(
        onClick = { onClick() },
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Playlist cover",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(shape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(playlistIcon),
                    contentDescription = "Playlist Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = playlistName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .basicMarquee()
                    )
                    if (isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite playlist",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = playlistDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                )
            }


            if (showArrow) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play playlist",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

            }


        }
    }
}


@Preview
@Composable
fun PlaylistCardPreview() {
    PlaylistCard(
        playlistId = "12345",
        playlistName = "Chill Vibes",
        playlistDescription = "A collection of relaxing tunes",
        playlistIcon = R.drawable.ic_launcher_foreground,
        onClick = { /* No-op for preview */ }
    )
}