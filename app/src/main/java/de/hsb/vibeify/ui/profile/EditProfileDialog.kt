package de.hsb.vibeify.ui.profile

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import de.hsb.vibeify.ui.components.photoPicker.PickPhoto

@OptIn(UnstableApi::class)
@Composable
fun EditProfileDialog(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user = viewModel.uiState.collectAsState().value.user

    val name = remember { mutableStateOf(user?.name ?: "") }
    val url = remember { mutableStateOf(user?.imageUrl ?: "") }
    val pickedImageUri = remember { mutableStateOf(user?.imageUrl) }


    // Update the ViewModel with the current user data
    Dialog(onDismissRequest = { onDismiss() }) {
        Card {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Edit Profile",
                fontSize = 20.sp
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                PickPhoto(
                    initialImageUri = pickedImageUri.value?.let { Uri.parse(it) },
                    onImagePicked = { uri ->
                        pickedImageUri.value = uri.toString()
                        url.value = uri.toString()
                    }
                )
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Name") },
                    modifier = Modifier.padding(vertical = 16.dp),
                    isError = name.value.length > 32,
                    supportingText = {
                        if (name.value.length > 32) {
                            Text(
                                text = "Limit: ${name.value.length}/32",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                Button(
                    onClick = {
                        if (name.value.length > 32) {
                            return@Button
                        }
                        viewModel.onSave(name.value, pickedImageUri.value ?: "")
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)

                ) {
                    Text("Save")
                }
            }
        }
    }
}

// Preview function to visualize the dialog in the IDE
@Preview
@Composable
fun EditProfileDialogPreview() {
    EditProfileDialog(onDismiss = {})
}