package de.hsb.vibeify.ui.components.photoPicker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import de.hsb.vibeify.R
import de.hsb.vibeify.ui.components.Avatar


@OptIn(UnstableApi::class)
@Composable
fun PickPhoto(
    onImagePicked: (Uri) -> Unit,
    viewModel: PhotoPickerViewModel = hiltViewModel()
) {
    val selectedImageUri by viewModel.state.collectAsState(initial = Uri.EMPTY)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        Log.d("PickPhoto", "Picked image URI: $uri")
        if (uri != null) {
            viewModel.state.value = uri.toString()
            onImagePicked(uri)
        }
    }

    Box(
        modifier = Modifier.size(200.dp),
    ) {
        if (selectedImageUri.toString().isNotEmpty() && selectedImageUri != Uri.EMPTY) {

            Avatar(
                imageUrl = selectedImageUri.toString(),
                modifier = Modifier
                    .fillMaxSize()

            )
        }
        else {

            Avatar(
                initials = "JL",
                modifier = Modifier
                    .fillMaxSize(),
            )
        }
        IconButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)

        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Pick Photo",
                tint = Color.Black
            )
        }

    }

}