package de.hsb.vibeify.ui.components

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(color))
        }
        Text(text = initials, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}