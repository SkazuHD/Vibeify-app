package de.hsb.vibeify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hsb.vibeify.R

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
    onMenuIconClick: (() -> Unit)? = null
) {
    return Card(
        onClick = { onClick() },
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxWidth(),
        ) {
            Row {
                Image(
                    painter = painterResource(id = songIcon),
                    contentDescription = "Song Icon",
                    modifier = Modifier
                        .padding(8.dp)
                        .width(48.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = shape
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                    ){
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
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
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (showMenu) {
                    Spacer(
                        modifier = Modifier.weight(1f) // Spacer to push the menu to the right
                    )
                    SongCardMenu(
                        modifier = Modifier.padding(end = 8.dp),
                        menuOptions = menuOptions,
                        onMenuIconClick = onMenuIconClick
                    )
                }
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
        songIcon = R.drawable.ic_launcher_foreground,
        isSongFavorite = true,
        menuOptions = listOf(
            MenuOption("Zur Playlist hinzufügen", { /* No-op für Preview */ }),
            MenuOption("Song teilen", { /* No-op für Preview */ })
        )
    )
}