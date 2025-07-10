package de.hsb.vibeify.ui.components.LiveFriends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.ui.components.LoadingIndicator
import de.hsb.vibeify.ui.components.NoContentCard

@Composable
fun LiveFriendView(
    modifier: Modifier = Modifier,
    viewModel: LiveFriendsViewModel = hiltViewModel(),
    onClick: (String) -> Unit = { /* Default no-op */ },
) {
    val uiState by viewModel.uiState.collectAsState()
    val friends = uiState.liveFriends
    val isLoading = uiState.isLoading
    if (isLoading) {
        LoadingIndicator(
            modifier = modifier.fillMaxSize(),
        )
    } else if (friends.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
        ) {
            friends.forEach { friend ->
                LiveFriendCard(
                    name = friend.name,
                    status = friend.isOnline,
                    currentSong = friend.currentSong?.name ?: "N/A",
                    imageUrl = friend.imageUrl,
                    email = friend.email,
                    modifier = Modifier.clickable {
                        onClick(friend.id)
                    }
                )
            }
        }
    } else {
        NoContentCard(
            modifier = Modifier,
            icon = {
                Icon(
                    imageVector = Icons.Default.SentimentVeryDissatisfied,
                    contentDescription = "No Friends",
                    modifier = Modifier.size(64.dp),
                )
            },
            title = "You have 0 Bitches",
        )

    }
}
