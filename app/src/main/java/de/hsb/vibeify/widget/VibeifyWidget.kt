package de.hsb.vibeify.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import de.hsb.vibeify.MainActivity
import de.hsb.vibeify.data.model.Song
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
                WidgetContent(playerServiceV2)
            }
        }
    }

    @Composable
    private fun WidgetContent(playerServiceV2: PlayerServiceV2) {
        val currentSong = playerServiceV2.currentSong.collectAsState(initial = Song()).value
        val isPlaying = playerServiceV2.isPlaying.collectAsState(initial = false).value
        val player = playerServiceV2.player.collectAsState(initial = null).value

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
                        maxLines = 1
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
                    Button(
                        text = "⏮",
                        onClick = {
                            player?.seekToPrevious()
                        },
                        modifier = GlanceModifier
                            .size(40.dp)
                            .background(GlanceTheme.colors.secondaryContainer)
                            .cornerRadius(20.dp)
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Button(
                        text = if (isPlaying) "⏸" else "▶",
                        onClick = {
                            if (player?.isPlaying == true) {
                                player.pause()
                            } else {
                                player?.play()
                            }
                        },
                        modifier = GlanceModifier
                            .size(48.dp)
                            .background(GlanceTheme.colors.primary)
                            .cornerRadius(24.dp)
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Button(
                        text = "⏭",
                        onClick = {
                            player?.seekToNext()
                        },
                        modifier = GlanceModifier
                            .size(40.dp)
                            .background(GlanceTheme.colors.secondaryContainer)
                            .cornerRadius(20.dp)
                    )
                }
            }
        }
    }


}