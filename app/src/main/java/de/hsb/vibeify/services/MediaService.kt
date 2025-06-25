package de.hsb.vibeify.services

import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dagger.hilt.android.scopes.ServiceScoped

@ServiceScoped
class MediaService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null
    var callback: MediaLibrarySession.Callback = object : MediaLibrarySession.Callback {}



    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback).build()
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}