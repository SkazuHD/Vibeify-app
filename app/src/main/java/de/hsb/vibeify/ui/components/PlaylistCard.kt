package de.hsb.vibeify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hsb.vibeify.R


@Composable
fun PlaylistCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* Default no-op */ },
    playlistDescription: String = "Playlist from Vibeify",
    playlistName: String = "Absolute banger",
    playlistIcon: Int = R.drawable.ic_launcher_foreground,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)

) {
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
                Text(
                    text = playlistDescription,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            repeatDelayMillis = 3500,
                            initialDelayMillis = 3000,
                            velocity = 28.dp
                        ),
                    maxLines = 1,
                )
                Text(
                    text = playlistName,
                    style = MaterialTheme.typography.bodyLarge,
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
    PlaylistCard()
}