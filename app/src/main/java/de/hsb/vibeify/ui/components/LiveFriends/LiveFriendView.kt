package de.hsb.vibeify.ui.components.LiveFriends

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun LiveFriendView(
    modifier: Modifier = Modifier,
    viewModel: LiveFriendsViewModel = hiltViewModel(),
    onClick : (String) -> Unit = { /* Default no-op */ },
) {
    val uiState by viewModel.uiState.collectAsState()
    val friends = uiState.liveFriends

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        friends.forEach { friend ->
            LiveFriendCard(
                name = friend.name,
                status = friend.isOnline,
                currentSong = friend.currentSong ?: "N/A",
                imageUrl = friend.imageUrl,
                email = friend.email,
                onClick = {
                    onClick(friend.id)
                },
            )
        }
    }
}
