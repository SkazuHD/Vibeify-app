package de.hsb.vibeify.services

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerServiceV2 {

    var context: Context
    lateinit var mediaController: MediaController

    private fun demoPlayBack() {
        // This is a demo playback function that sets up a media item and starts playback.
        val mediaUri = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        val artworkUri =
            "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png".toUri()


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

    @Inject
    constructor(context: Context) {
        this.context = context
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            mediaController = controller
            demoPlayBack()
        }, ContextCompat.getMainExecutor(context))
    }

    fun release() {
        mediaController.release()
    }

    fun getController(): MediaController {
        return mediaController
    }

}