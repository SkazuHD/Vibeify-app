package de.hsb.vibeify.services

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerServiceV2 {

    private var context: Context

    private val controllerReadyActions = mutableListOf<(MediaController) -> Unit>()
    private var mediaController: MediaController? = null

    fun demoPlayBack() {
        // This is a demo playback function that sets up a media item and starts playback.
        val mediaUri = "asset:///Bread.mp3"
        val artworkUri =
            "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png".toUri()


        val mediaItem =
            MediaItem.Builder()
                .setMediaId("outkast_hey_ya")
                .setUri(mediaUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Hey Ya!")
                        .setArtist("Outkast")
                        .setAlbumTitle("Speakerboxxx/The Love Below")
                        .setAlbumArtist("Outkast")
                        .setArtworkUri(artworkUri)
                        .setGenre(
                            "Hip Hop"
                        )
                        .setDescription(
                            "A classic hip hop track from Outkast's double album, featuring a catchy hook and infectious beat."
                        )
                        .setTrackNumber(1)
                        .setDiscNumber(
                            1
                        ).setReleaseYear(
                            2003
                        ).setReleaseMonth(
                            9
                        ).setReleaseDay(
                            9
                        ).setIsPlayable(
                            true
                        )
                        .build()
                )
                .build()

        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
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
            controllerReadyActions.forEach { it(controller) }
            controllerReadyActions.clear()

        }, ContextCompat.getMainExecutor(context))
    }

    fun withController(action: (MediaController) -> Unit) {
        val controller = mediaController
        if (controller != null) {
            action(controller)
        } else {
            controllerReadyActions.add(action)
        }
    }

    suspend fun awaitController(): MediaController {
        return mediaController
            ?: suspendCancellableCoroutine { continuation ->
                controllerReadyActions.add { controller ->
                    continuation.resume(controller) { cause, _, _ -> }
                }
            }
    }

    fun release() {
        mediaController?.release()
    }

}