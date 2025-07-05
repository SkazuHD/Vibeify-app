package de.hsb.vibeify.ui.components.photoPicker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.hsb.vibeify.ui.components.Avatar


@Composable
fun PickPhoto(
    onImagePicked: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    initialImageUri: Uri? = null,
    size: Dp = 200.dp,
    placeholderInitials: String = "JL",
    isCircle: Boolean = true
) {
    val selectedImageUri = remember { mutableStateOf(initialImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri.value = uri
            onImagePicked(uri)
        }
    }

    Box(
        modifier = modifier.size(size),
    ) {
        if (selectedImageUri.value != null) {

            Avatar(
                imageUrl = selectedImageUri.value.toString(),
                modifier = Modifier
                    .fillMaxSize(),
                isCircle = isCircle,

                )
        } else {

            Avatar(
                initials = placeholderInitials,
                modifier = Modifier
                    .fillMaxSize(),
                isCircle = isCircle,
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