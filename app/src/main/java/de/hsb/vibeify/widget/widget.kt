package de.hsb.vibeify.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
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
import androidx.glance.layout.width
import androidx.glance.text.Text
import de.hsb.vibeify.widget.myActivities.MyActivity
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import de.hsb.vibeify.widget.myActivities.NavigationActivity
import androidx.datastore.preferences.core.Preferences
import androidx.glance.currentState


class MyAppWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val songTitle = prefs[WidgetPreferencesKeys.SONG_TITLE] ?: "No song"
            val artist = prefs[WidgetPreferencesKeys.ARTIST] ?: "Unknown Artist"
            val isPlaying = prefs[WidgetPreferencesKeys.IS_PLAYING] ?: false

            MusicWidgetLayout(
                songTitle = songTitle,
                artist = artist,
                isPlaying = isPlaying
            )
        }
    }
}



@SuppressLint("RestrictedApi")
@Composable
fun MusicWidgetLayout(
    songTitle: String,
    artist: String,
    isPlaying: Boolean
) {
    val whiteText = TextStyle(
        color = ColorProvider(Color.White),
        fontWeight = FontWeight.Medium
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(
                actionStartActivity<NavigationActivity>(
                    actionParametersOf(
                        ActionParameters.Key<String>("destination") to "playback_view"
                    )
                )
            )
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier
                .width(100.dp)
                .height(100.dp)
                .cornerRadius(12.dp)
                .background(Color(0xFF333333)),
            contentAlignment = Alignment.Center
        ) {
            Text("Image", style = whiteText)
        }

        Spacer(modifier = GlanceModifier.height(16.dp))

        Text(text = songTitle, style = whiteText, maxLines = 1)
        Text(text = artist, style = whiteText, maxLines = 1)

        Spacer(modifier = GlanceModifier.height(16.dp))
        ProgressBar(progress = 0.5f, modifier = GlanceModifier.padding(vertical = 8.dp))
        Spacer(modifier = GlanceModifier.height(16.dp))

        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            Button(
                text = "⏮",
                onClick = actionStartActivity<MyActivity>(),
                modifier = GlanceModifier.defaultWeight()
            )
            Button(
                text = if (isPlaying) "⏸" else "▶️",
                onClick = actionStartActivity<MyActivity>(),
                modifier = GlanceModifier.defaultWeight()
            )
            Button(
                text = "⏭",
                onClick = actionStartActivity<MyActivity>(),
                modifier = GlanceModifier.defaultWeight()
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        Text(text = "Next: Roses", style = whiteText, maxLines = 1)
    }
}


@Composable
fun ProgressBar(
    progress: Float,
    modifier: GlanceModifier = GlanceModifier
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    val progressWidth: Dp = when {
        clampedProgress <= 0.0f -> 0.dp
        clampedProgress <= 0.1f -> 24.dp
        clampedProgress <= 0.2f -> 48.dp
        clampedProgress <= 0.3f -> 72.dp
        clampedProgress <= 0.4f -> 96.dp
        clampedProgress <= 0.5f -> 120.dp
        clampedProgress <= 0.6f -> 144.dp
        clampedProgress <= 0.7f -> 168.dp
        clampedProgress <= 0.8f -> 192.dp
        clampedProgress <= 0.9f -> 216.dp
        else -> 240.dp
    }

    Box(
        modifier = modifier
            .width(240.dp)
            .height(8.dp)
            .background(Color(0xFF444444))
    ) {
        Box(
            modifier = GlanceModifier
                .width(progressWidth)
                .height(8.dp)
                .background(Color(0xFF00FFAA))
        ) {
            Spacer(modifier = GlanceModifier.width(1.dp))
        }
    }
}


