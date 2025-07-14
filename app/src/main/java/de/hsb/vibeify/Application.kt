package de.hsb.vibeify

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import dagger.hilt.android.HiltAndroidApp

// Marks this Application class as a Hilt entry point for dependency injection
@HiltAndroidApp
class VibeifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val settings = firestoreSettings {
            // Use memory cache
            //setLocalCacheSettings(memoryCacheSettings {})
            // Use persistent disk cache (default)
            setLocalCacheSettings(persistentCacheSettings {})
        }
        // Apply the settings to the Firestore instance
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}