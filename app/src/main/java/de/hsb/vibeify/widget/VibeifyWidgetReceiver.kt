package de.hsb.vibeify.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// This connects the app widget to the Glance framework.
class VibeifyWidgetReceiver :
    GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VibeifyWidget()
}