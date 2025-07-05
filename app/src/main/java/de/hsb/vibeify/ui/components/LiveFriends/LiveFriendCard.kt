package de.hsb.vibeify.ui.components.LiveFriends

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.ui.components.Avatar

@Composable
fun LiveFriendCard(
    modifier: Modifier = Modifier,
    name : String,
    status : Boolean,
    currentSong : String,
    imageUrl : String?,
    email : String,
    onClick : () -> Unit = { /* Default no-op */ }
){

    val cardColors =
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    Card(
        modifier = modifier
            .clickable(
                onClick = onClick
            )
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.large
            )
            .fillMaxSize()
            .aspectRatio(5f / 6f), // Beispiel: 5:6 Verhältnis
        colors = cardColors
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Row {
                Box(
                    modifier = Modifier.padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Avatar(
                        modifier = Modifier
                            .size(83.dp),
                        imageUrl = imageUrl
                    )
                    if(status) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = "Favorite Icon",
                            tint = Color(0xFF5afc03), // Green color for online status
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-12).dp)
                        )
                    }
                    else {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = "Favorite Icon",
                            tint = Color.Gray, // Red color for offline status
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-12).dp)
                        )
                    }
                }
            }
            val displayName = name.ifBlank { email.split("@").takeIf { it.isNotEmpty() }?.get(0) ?: ""  }
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    repeatDelayMillis = 3500,
                    initialDelayMillis = 3000,
                    velocity = 28.dp
                ).padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
            Text(
                text = currentSong,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 2.dp, start = 4.dp, end = 4.dp)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 3500,
                        initialDelayMillis = 3000,
                        velocity = 28.dp
                    ).padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}


@Preview
@Composable
fun LiveFriendCardPreview() {
    LiveFriendCard(
        name = "John Doe",
        status = true,
        currentSong = "Song Title - Artist",
        imageUrl = "",
        email = "test@test"
    )
}