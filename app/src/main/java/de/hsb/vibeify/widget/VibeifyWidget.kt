package de.hsb.vibeify.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import de.hsb.vibeify.MainActivity
import de.hsb.vibeify.R
import de.hsb.vibeify.di.PlayerServiceEntryPoint
import de.hsb.vibeify.services.PlayerServiceV2
import javax.inject.Inject


class VibeifyWidget @Inject constructor() :
    GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val playerServiceV2 = PlayerServiceEntryPoint.get(context)

        provideContent {
            GlanceTheme {
                WidgetContent(playerServiceV2, initialAlbumArt = null)
            }
        }
    }

    private suspend fun getBitMapfromUrl(url: String, context: Context): ImageProvider {
        Log.d("VibeifyWidget", "Fetching album art from URL: $url")
        var bitmap: Bitmap? = null
        val result = ImageLoader(context).execute(
            ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .size(100, 100)
                .target { res: Drawable ->
                    bitmap = (res as BitmapDrawable).bitmap
                }.build()
        )
        if (bitmap == null) {
            bitmap = createBitmap(100, 100)
        }
        Log.d("VibeifyWidget", "Album art fetched successfully: ${bitmap?.width}x${bitmap?.height}")
        return ImageProvider(bitmap)
    }

    @Composable
    private fun WidgetContent(
        playerServiceV2: PlayerServiceV2,
        initialAlbumArt: ImageProvider? = null
    ) {
        val currentSong = playerServiceV2.currentSong.collectAsState(initial = null).value
        val isPlaying = playerServiceV2.isPlaying.collectAsState(initial = false).value
        val player = playerServiceV2.player.collectAsState(initial = null).value
        var albumArt by remember { mutableStateOf(initialAlbumArt) }
        val context = LocalContext.current

        LaunchedEffect(currentSong?.id) {
            Log.d("VibeifyWidget", "Current song updated: ${currentSong?.id}")
            if (currentSong?.imageUrl != null && currentSong.imageUrl.isNotEmpty()) {
                Log.d("VibeifyWidget", "Fetching album art from URL: ${currentSong.imageUrl}")
                albumArt = getBitMapfromUrl(currentSong.imageUrl!!, context)
            } else {
                Log.d("VibeifyWidget", "No album art URL found, using default")
                albumArt = ImageProvider(R.drawable.ic_launcher_background)
            }
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art Placeholder
                if (currentSong?.imageUrl.isNullOrEmpty())
                    Box(
                        modifier = GlanceModifier
                            .size(64.dp)
                            .background(GlanceTheme.colors.primaryContainer)
                            .cornerRadius(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♪",
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer
                            )
                        )
                    } else
                    albumArt?.let { art ->
                        Image(
                            provider = art,
                            contentDescription = "Album Art",
                            modifier = GlanceModifier
                                .size(64.dp)
                                .cornerRadius(8.dp)
                                .background(GlanceTheme.colors.primaryContainer),
                            contentScale = androidx.glance.layout.ContentScale.Crop
                        )
                    }


                Spacer(modifier = GlanceModifier.width(16.dp))

                // Song Info
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentSong?.name ?: "No song playing",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1,
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = currentSong?.artist ?: "Unknown Artist",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SquareIconButton(
                        imageProvider = ImageProvider(R.drawable.ic_skip_previous),
                        contentDescription = "Previous",
                        onClick = {
                            player?.seekToPrevious()
                        },
                        modifier = GlanceModifier.size(40.dp),
                        backgroundColor = GlanceTheme.colors.secondaryContainer,
                        contentColor = GlanceTheme.colors.onSecondaryContainer
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    SquareIconButton(
                        imageProvider = ImageProvider(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                        ),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        onClick = {
                            if (isPlaying) {
                                player?.pause()
                            } else {
                                player?.play()
                            }
                        },
                        modifier = GlanceModifier.size(48.dp),
                        backgroundColor = GlanceTheme.colors.primary,
                        contentColor = GlanceTheme.colors.onPrimary
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    SquareIconButton(
                        imageProvider = ImageProvider(R.drawable.ic_skip_next),
                        contentDescription = "Next",
                        onClick = {
                            player?.seekToNext()
                        },
                        modifier = GlanceModifier.size(40.dp),
                        backgroundColor = GlanceTheme.colors.secondaryContainer,
                        contentColor = GlanceTheme.colors.onSecondaryContainer
                    )
                }
            }
        }
    }


}