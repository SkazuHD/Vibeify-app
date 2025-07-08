package de.hsb.vibeify.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import de.hsb.vibeify.services.PlayerServiceV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

class MyAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: MyAppWidget = MyAppWidget()

    companion object {
        fun update(context: Context, playerServiceV2: PlayerServiceV2) {
            CoroutineScope(Dispatchers.Main).launch {
                val glanceIds =
                    GlanceAppWidgetManager(context).getGlanceIds(MyAppWidget::class.java)
                val currentSong = playerServiceV2.currentSong.value
                val isPlaying = playerServiceV2.isPlaying.value

                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(
                        context,
                        PreferencesGlanceStateDefinition,
                        glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[WidgetPreferencesKeys.SONG_TITLE] = currentSong?.name ?: "No song"
                            this[WidgetPreferencesKeys.ARTIST] =
                                currentSong?.artist ?: "Unknown Artist"
                            this[WidgetPreferencesKeys.IS_PLAYING] = isPlaying
                        }
                    }

                    MyAppWidget().update(context, glanceId)
                }
            }
        }
    }
}