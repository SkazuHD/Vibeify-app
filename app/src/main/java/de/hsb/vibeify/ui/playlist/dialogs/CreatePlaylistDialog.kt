package de.hsb.vibeify.ui.playlist.dialogs

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.ui.components.photoPicker.PickPhoto
import de.hsb.vibeify.ui.playlist.PlaylistViewModel

@Composable
fun CreatePlaylistDialog(
    onDismissRequest: () -> Unit,
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {

    val playlistName = TextFieldState()
    val playListDescription = TextFieldState()
    val pickedImageUri = remember { mutableStateOf<Uri?>(null) }

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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Playlist",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                PickPhoto(
                    onImagePicked = { uri ->
                        pickedImageUri.value = uri
                    },
                    size = 200.dp,
                    placeholderInitials = "",
                    isCircle = false
                )
                OutlinedTextField(
                    state = playlistName,
                    label = { Text("Playlist Name") },
                )
                OutlinedTextField(
                    state = playListDescription,
                    label = { Text("Playlist Description") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                )
                {

                    Button(
                        modifier = Modifier,
                        onClick = {
                            onDismissRequest()
                        }
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        modifier = Modifier,
                        onClick = {
                            playlistViewModel.createPlaylist(
                                playlistName = playlistName.text.toString(),
                                description = playListDescription.text.toString(),
                                imageUrl = pickedImageUri.value?.toString()
                            )
                            onDismissRequest()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}