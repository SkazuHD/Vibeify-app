package de.hsb.vibeify.services

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.scopes.ServiceScoped

@ServiceScoped
class MediaService :  MediaSessionService() {
    private var mediaSession: MediaSession? = null

    private fun demoPlayBack(){

        val mediaController = mediaSession?.player
        ?: return
        val mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        val artworkUri = "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png".toUri()


        val mediaItem =
            MediaItem.Builder()
                .setMediaId("media-1")
                .setUri(mediaUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist("David Bowie")
                        .setTitle("Heroes")
                        .setArtworkUri(artworkUri)
                        .build()
                )
                .build()

        mediaController.setMediaItem(mediaItem)
        mediaController.prepare()
        mediaController.play()
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
        demoPlayBack()
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}