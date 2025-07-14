package de.hsb.vibeify.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * A composable function that displays an avatar image or initials.
 *
 * @param modifier Modifier to be applied to the avatar.
 * @param size The size of the avatar.
 * @param imageUrl The URL of the image to be displayed. If null, initials will be shown.
 * @param initials The initials to be displayed if imageUrl is null.
 * @param isCircle Whether the avatar should be circular or rounded rectangle.
 */
@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    imageUrl: String? = null,
    initials: String? = null,
    isCircle: Boolean = true
) {
    Box(
        modifier
            .size(size),
        contentAlignment = Alignment.Center
    )
    {
        val color = MaterialTheme.colorScheme.primary
        val initials = initials?.uppercase() ?: "AB"
        val uri = try {
            imageUrl?.toUri()
        } catch (e: Exception) {
            null
        }
        if (uri != null && uri.toString().isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(if (isCircle) CircleShape else RoundedCornerShape(size / 4)),
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (isCircle) {
                    drawCircle(SolidColor(color))
                } else {
                    drawRoundRect(
                        SolidColor(color),
                        cornerRadius = CornerRadius(
                            size.toPx() / 4,
                            size.toPx() / 4
                        )
                    )

                }
            }
            Text(
                text = initials,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

    }
}