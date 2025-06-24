package de.hsb.vibeify.services

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerServiceV2 {

    private var context: Context
    private var wasPlayingBeforeViewChange = false

    private val controllerReadyActions = mutableListOf<(MediaController) -> Unit>()
    private var mediaController: MediaController? = null
    var currentSong: Song? = null

    fun buildMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.name)
            .setUri("${song.filePath}")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.name)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.imageUrl?.toUri())
                    .build()
            )
            .build()
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

    fun savePlaybackState() {
        mediaController?.let {
            wasPlayingBeforeViewChange = it.isPlaying
        }
    }

    fun shouldResumePlayback(): Boolean {
        return wasPlayingBeforeViewChange
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