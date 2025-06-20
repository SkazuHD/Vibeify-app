package de.hsb.vibeify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    songIcon: Int = R.drawable.ic_launcher_foreground

) {
    return Card(
        onClick = { onClick() },
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .fillMaxWidth(),
        ) {
            Row {
                Image(
                    painter = painterResource(id = songIcon),
                    contentDescription = "Song Icon",
                    modifier = modifier.padding(8.dp).width(48.dp).background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),

                        shape = RoundedCornerShape(4.dp)

                    )
                )
                Column(
                    modifier = modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                ) {
                    Text (
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = modifier.padding(bottom = 2.dp)
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
        songIcon = R.drawable.ic_launcher_foreground
    )
}