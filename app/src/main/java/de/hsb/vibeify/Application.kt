package de.hsb.vibeify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VibeifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}