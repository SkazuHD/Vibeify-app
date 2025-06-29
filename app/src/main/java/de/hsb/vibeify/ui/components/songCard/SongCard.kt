package de.hsb.vibeify.ui.components.songCard

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import de.hsb.vibeify.R
import de.hsb.vibeify.ui.components.MenuOption
import de.hsb.vibeify.ui.components.OptionsMenu

@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { /* Default no-op */ },
    title: String = "Song Title",
    artist: String = "Artist Name",
    songIcon: Int = R.drawable.ic_launcher_foreground,
    isSongFavorite: Boolean = false,
    isSongPlaying: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    showMenu: Boolean = true,
    menuOptions: List<MenuOption> = emptyList(),
    onMenuIconClick: (() -> Unit)? = null,
    songImageUrl: String? = null
) {
    val cardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = shape,
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(songImageUrl ?: "")
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Song Image",
                placeholder = painterResource(id = songIcon),
                error = painterResource(id = songIcon),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = shape
                    )
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
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(
                            iterations = Int.MAX_VALUE,
                            repeatDelayMillis = 3500,
                            initialDelayMillis = 3000,
                            velocity = 28.dp
                        )
                    )
                    if (isSongFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            repeatDelayMillis = 3500,
                            initialDelayMillis = 3000,
                            velocity = 28.dp
                        )
                )
            }

            if (showMenu) {
                OptionsMenu(
                    menuOptions = menuOptions,
                    onMenuIconClick = onMenuIconClick
                )
            }
        }
    }
}

@Preview
@Composable
fun SongCardPreview() {
    SongCard(
        title = "Sample Song",
        artist = "Sample Artist",
        isSongFavorite = true,
        isSongPlaying = false,
        menuOptions = listOf(
            MenuOption("Zur Playlist hinzufügen", { /* No-op für Preview */ }),
            MenuOption("Song teilen", { /* No-op für Preview */ })
        ),
        songImageUrl = "https://example.com/sample-song-image.jpg"
    )
}