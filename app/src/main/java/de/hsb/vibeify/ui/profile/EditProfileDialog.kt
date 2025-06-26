package de.hsb.vibeify.ui.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.ui.components.Avatar

@Composable
fun EditProfileDialog(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()) {
    val user = viewModel.uiState.collectAsState().value.user

    val name = remember{ mutableStateOf(user?.name ?: "") }
    val url = remember{ mutableStateOf(user?.imageUrl ?: "") }


    Dialog(onDismissRequest = {onDismiss()}){
        Card {
            Text(
                text = "Edit Profile",
                modifier = Modifier.padding(16.dp)
            )
            Avatar(
                modifier = Modifier.padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(200.dp)
                    .padding(top = 16.dp),
                initials = "JL"
            )
            TextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Name") },
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {
                    //username und Bild-URL update
                    viewModel.onSave(name.value, url.value)
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Text("Save")
            }

        }
    }
}

@Preview
@Composable
fun EditProfileDialogPreview() {
    EditProfileDialog(onDismiss = {})
}