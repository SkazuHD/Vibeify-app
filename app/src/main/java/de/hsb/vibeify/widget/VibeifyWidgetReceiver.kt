package de.hsb.vibeify.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class VibeifyWidgetReceiver :
    GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VibeifyWidget()
}