package de.hsb.vibeify.ui.profile

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.ui.components.Avatar

@Composable
fun FollowDialog(
    followType: FollowType,
    user: User?,
    modifier: Modifier = Modifier,
    onProfileClick: (User) -> Unit = {},
    onDismissRequest: () -> Unit,
    followDialogViewModel: FollowDialogViewModel = hiltViewModel()
) {

    val followList = followDialogViewModel.followList.collectAsState().value
    val title = when (followType) {
        FollowType.FOLLOWERS -> "Followers"
        FollowType.FOLLOWING -> "Following"
    }
    val subtitle = when (followType) {
        FollowType.FOLLOWERS -> "People who follow ${user?.name}"
        FollowType.FOLLOWING -> "People ${user?.name} follows"
    }

    LaunchedEffect(
        followType, user,
    ) {
        if (user == null) return@LaunchedEffect
        followDialogViewModel.loadFollowList(user.id, followType)
    }


    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (followList.isEmpty()) {
                    Text(
                        text = "No ${followType.name.lowercase()} found.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(followList) { followUser ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = {
                                            Log.d("FollowDialog", "Clicked on ${followUser.name}")
                                            onProfileClick(followUser)
                                        }
                                    ),
                                headlineContent = {
                                    Text(
                                        text = followUser.name.ifBlank { followUser.email },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },

                                leadingContent = {
                                    Avatar(
                                        imageUrl = followUser.imageUrl,
                                    )
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}