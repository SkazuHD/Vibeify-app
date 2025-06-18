package de.hsb.vibeify.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.scopes.ServiceScoped

@ServiceScoped
class MediaService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}