package de.hsb.vibeify.ui.components.playlistCard

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.R


@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* Default no-op */ },
    playlistId: String,
    playlistDescription: String = "Playlist from Vibeify",
    playlistName: String = "Absolute banger",
    playlistIcon: Int = R.drawable.ic_launcher_foreground,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    playlistCardViewModel: PlaylistCardViewModel = hiltViewModel()

) {

    val isPlaylistFavorite = rememberSaveable(playlistId) {
        playlistCardViewModel.isPlaylistFavorite(playlistId)
    }

    Card(
        onClick = { onClick() },
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(playlistIcon),
                contentDescription = "Playlist Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = playlistName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                repeatDelayMillis = 3500,
                                initialDelayMillis = 3000,
                                velocity = 28.dp
                            ),
                        maxLines = 1,
                    )
                    if (isPlaylistFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = playlistDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            repeatDelayMillis = 3500,
                            initialDelayMillis = 3000,
                            velocity = 28.dp
                        ),
                    maxLines = 1,
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Icon",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
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