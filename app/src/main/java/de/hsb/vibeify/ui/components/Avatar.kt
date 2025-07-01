package de.hsb.vibeify.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.core.net.toUri

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    imageUrl : String? = null,
    initials : String? = null,
    onClick : (() -> Unit)? = null
) {
    Box(modifier.size(size).clickable(
        enabled = onClick != null,
        onClick = { onClick?.invoke()
        }
    ) ,contentAlignment = Alignment.Center)
    {
        val color = MaterialTheme.colorScheme.primary
        val initials = initials?.uppercase() ?: "AB"
        val uri = try { imageUrl?.toUri() } catch (e: Exception) { null }
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
                    .clip(androidx.compose.foundation.shape.CircleShape),
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(SolidColor(color))
            }
            Text(text = initials, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }

    }
}