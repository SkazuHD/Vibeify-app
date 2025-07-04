package de.hsb.vibeify.services

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerServiceV2 {

    private var context: Context
    private var wasPlayingBeforeViewChange = false

    private val controllerReadyActions = mutableListOf<(MediaController) -> Unit>()
    private var mediaController: MediaController? = null

    private val _currentSongList = MutableStateFlow(emptyList<Song>())
    val currentSongList: StateFlow<List<Song>> = _currentSongList

    private val _currentPlaylistId = MutableStateFlow<String?>(null)
    val currentPlaylistId: StateFlow<String?> = _currentPlaylistId

    private val _currentSong: MutableStateFlow<Song?> = MutableStateFlow(null)
    var currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    enum class PlaybackMode {
        SHUFFLE, NONE
    }

    enum class RepeatMode {
        ALL, LOOP, NONE
    }

    private val _playbackMode = MutableStateFlow(PlaybackMode.NONE)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode


    val upcomingSongs: StateFlow<List<Song>> = combine(
        currentSongList,
        currentSong,
        playbackMode
    ) { songList, _, mode ->
        mediaController?.let { controller ->
            val currentIndex = controller.currentMediaItemIndex

            if (mode == PlaybackMode.SHUFFLE) {
                val nextIndex = controller.getNextMediaItemIndex().coerceIn(0, songList.size)


                if (nextIndex in songList.indices) {
                    listOf(songList[nextIndex])
                } else {
                    emptyList()
                }

            } else {
                if (currentIndex >= 0 && currentIndex < songList.size - 1) {
                    songList.subList(currentIndex + 1, songList.size)
                } else {
                    emptyList()
                }
            }
        } ?: emptyList()
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )


    private val _duration = MutableStateFlow(1L)
    val duration: StateFlow<Long> = _duration

    private val _playerState = MutableStateFlow(Player.STATE_IDLE)
    val playerState: StateFlow<Int> = _playerState

    private var positionTrackingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    fun buildMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.id)
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

            controller.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isPlaying.value = controller.isPlaying && playbackState == Player.STATE_READY
                    _playerState.value = playbackState
                }

                override fun onIsPlayingChanged(isPlayingState: Boolean) {
                    _isPlaying.value = isPlayingState
                    if (isPlayingState) {
                        startPositionTracking()
                    } else {
                        stopPositionTracking()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentSongFromMediaItem(controller)
                }

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    updatePositionAndDuration()
                }
            })
            updatePositionAndDuration()

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

    fun play(song: Song) {
        _currentSong.value = song
        _currentSongList.value = listOf(song)
        val mediaItem = buildMediaItem(song)
        withController { controller ->
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
        startPositionTracking()
    }

    fun play(songs: List<Song>, startIndex: Int = 0, playlistId: String? = null) {
        if (songs.isEmpty()) return
        _currentSong.value = songs[startIndex]
        _currentSongList.value = songs
        _currentPlaylistId.value = playlistId
        val mediaItems = songs.map { buildMediaItem(it) }
        withController { controller ->
            controller.setMediaItems(mediaItems)
            controller.seekTo(startIndex, 0L)
            controller.prepare()
            controller.play()
        }
        startPositionTracking()
    }

    fun changeRepeatMode(mode: Int) {
        withController { controller ->
            if (mode !in Player.REPEAT_MODE_OFF..Player.REPEAT_MODE_ALL) {
                throw IllegalArgumentException("Invalid repeat mode: $mode")
            }
            controller.repeatMode = mode
            // _repeatMode.value = mode
        }
    }

    fun pause() {
        withController { controller ->
            controller.pause()
        }
        stopPositionTracking()
    }

    fun stop() {
        withController { controller ->
            controller.stop()
        }
        stopPositionTracking()
        _currentSong.value = null
        _currentSongList.value = emptyList()
        _currentPlaylistId.value = null
        _position.value = 0L
        _duration.value = 1L
        _isPlaying.value = false
    }


    fun resume() {
        withController { controller ->
            controller.play()
        }
    }

    fun clearMediaItems() {
        withController { controller ->
            controller.clearMediaItems()
        }
    }

    fun seekTo(positionMs: Long) {
        withController { controller ->
            controller.seekTo(positionMs)
            _position.value = positionMs
        }
    }

    fun skipToNext() {
        withController { controller ->
            controller.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        withController { controller ->
            controller.seekToPreviousMediaItem()
        }
    }

    private fun updateCurrentSongFromMediaItem(controller: MediaController) {
        val currentIndex = controller.currentMediaItemIndex
        if (currentIndex >= 0 && currentIndex < currentSongList.value.size) {
            _currentSong.value = currentSongList.value[currentIndex]
        }
    }

    fun release() {
        stopPositionTracking()
        mediaController?.release()
    }

    private fun updatePositionAndDuration() {
        withController { controller ->
            _position.value = controller.currentPosition
            _duration.value = controller.duration.coerceAtLeast(1L)
        }
    }

    fun startPositionTracking(intervalMs: Long = 200) {
        stopPositionTracking()
        positionTrackingJob = serviceScope.launch {
            while (isActive) {
                updatePositionAndDuration()
                kotlinx.coroutines.delay(intervalMs)
            }
        }
    }

    fun stopPositionTracking() {
        positionTrackingJob?.cancel()
        positionTrackingJob = null
    }


    fun togglePlaybackMode() {
        withController { controller ->
            _playbackMode.value = when (_playbackMode.value) {
                PlaybackMode.NONE -> {
                    controller.shuffleModeEnabled = true
                    PlaybackMode.SHUFFLE
                }

                PlaybackMode.SHUFFLE -> {

                    controller.shuffleModeEnabled = false

                    PlaybackMode.NONE
                }

            }
        }
    }

    fun toggleRepeatMode() {
        withController { controller ->
            _repeatMode.value = when (_repeatMode.value) {
                RepeatMode.NONE -> {
                    controller.repeatMode = Player.REPEAT_MODE_ALL
                    println()
                    println("1")
                    RepeatMode.ALL
                }

                RepeatMode.ALL -> {
                    controller.repeatMode = Player.REPEAT_MODE_ONE
                    println("2")

                    RepeatMode.LOOP
                }

                RepeatMode.LOOP -> {
                    controller.repeatMode = Player.REPEAT_MODE_OFF
                    println("3")

                    RepeatMode.NONE
                }


            }
        }
    }


    fun getPlaybackMode(): PlaybackMode {
        return _playbackMode.value
    }

    fun getRepeatMode(): RepeatMode {
        return _repeatMode.value
    }


}