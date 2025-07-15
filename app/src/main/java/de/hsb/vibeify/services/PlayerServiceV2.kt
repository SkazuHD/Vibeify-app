package de.hsb.vibeify.services

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// PlayerServiceV2 is a singleton service that manages media playback using MediaController.
@Singleton
class PlayerServiceV2 {

    // values for context, media controller, and various states related to playback exposed as StateFlow.
    // It allows for playing songs, managing playlists, and tracking playback state.
    private var context: Context
    private val controllerReadyActions = mutableListOf<(MediaController) -> Unit>()
    private var _mediaController: MutableStateFlow<Player?> = MutableStateFlow(null)
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

    private val _playbackMode = MutableStateFlow(PlaybackMode.NONE)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode

    // upcomingSongs provides a list of songs that are queued to play after the current song.
    // It combines the current song list, current song, and playback mode to determine the next songs.
    // In SHUFFLE mode, it fetches the next song randomly; otherwise, it fetches the next songs in order.
    val upcomingSongs: StateFlow<List<Song>> = combine(
        currentSongList,
        currentSong,
        playbackMode
    ) { songList, _, mode ->
        _mediaController.value?.let { controller ->
            val currentIndex = controller.currentMediaItemIndex

            if (mode == PlaybackMode.SHUFFLE) {
                Log.d("PlayerServiceV2", "Fetching next song in SHUFFLE mode")
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

    // values for position and duration are updated based on the current playback state.
    // position tracks the current playback position, while duration tracks the total duration of the current media item.
    private val _duration = MutableStateFlow(1L)
    val duration: StateFlow<Long> = _duration

    private val _playerState = MutableStateFlow(Player.STATE_IDLE)
    val playerState: StateFlow<Int> = _playerState

    val player: StateFlow<Player?> = _mediaController

    private var positionTrackingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    // Constructor initializes the MediaController with a SessionToken and sets up listeners for playback state changes.
    // It also handles media item transitions and updates the current song based on the media item.
    @Inject
    constructor(context: Context, userRepository: UserRepository) {
        this.context = context
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            _mediaController.value = controller
            controllerReadyActions.forEach { it(controller) }
            controllerReadyActions.clear()


            controller.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isPlaying.value = controller.isPlaying
                    _playerState.value = playbackState
                }

                // This method is called when the playback state changes, such as when the player starts or stops playing.
                override fun onIsPlayingChanged(isPlayingState: Boolean) {
                    _isPlaying.value = isPlayingState
                    if (isPlayingState) {
                        startPositionTracking()
                    } else {
                        stopPositionTracking()
                    }
                }

                // This method is called when the current media item changes, allowing us to update the current song.
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentSongFromMediaItem(controller)
                }

                // This method is called when the media metadata changes, allowing us to handle metadata updates if needed.
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                }

                // This method is called when the player events occur, such as seeking or changing the playback position.
                override fun onEvents(player: Player, events: Player.Events) {
                    updatePositionAndDuration()

                }

                // This method is called when the shuffle mode is enabled or disabled, allowing us to update the playback mode.
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                    _playbackMode.value = if (shuffleModeEnabled) {
                        PlaybackMode.SHUFFLE
                    } else {
                        PlaybackMode.NONE
                    }
                }
            })
            updatePositionAndDuration()

        }, ContextCompat.getMainExecutor(context))

        serviceScope.launch {
            userRepository.state.map { it.currentUser }.distinctUntilChanged().collect { user ->
                if (user == null) {
                    Log.d(
                        "PlayerServiceV2",
                        "User is null, stopping position tracking and releasing controller"
                    )
                    stopPositionTracking()
                    withController {
                        it.stop()
                    }
                }
            }
        }
    }

    // withController is a utility function that executes an action with the MediaController if it is available.
    fun withController(action: (Player) -> Unit) {
        val controller = _mediaController.value
        if (controller != null) {
            action(controller)
        } else {
            controllerReadyActions.add(action)
        }
    }

    // play functions allow playing a single song or a list of songs, setting the current song and playlist ID.
    fun play(song: Song) {
        _currentSong.value = song
        _currentSongList.value = listOf(song)
        val mediaItem = Song.toMediaItem(song)
        withController { controller ->
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
        startPositionTracking()
    }

    // play function with a list of songs allows playing multiple songs starting from a specified index.
    fun play(songs: List<Song>, startIndex: Int = 0, playlistId: String? = null) {
        if (songs.isEmpty()) return
        _currentSong.value = songs[startIndex]
        _currentSongList.value = songs
        _currentPlaylistId.value = playlistId
        val mediaItems = songs.map { Song.toMediaItem(it) }
        withController { controller ->
            controller.setMediaItems(mediaItems)
            controller.seekTo(startIndex, 0L)
            controller.prepare()
            controller.play()
        }
        startPositionTracking()
    }

    // pause function allows pausing the playback of the current song.
    fun insertIntoQueue(song: Song) {
        _currentSongList.value = _currentSongList.value.toMutableList().apply {
            add(1, song)
        }
        val mediaItem = Song.toMediaItem(song)
        withController { controller ->
            controller.addMediaItem(1, mediaItem)
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
        }
    }

    fun resume() {
        withController { controller ->
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        withController { controller ->
            controller.seekTo(positionMs)
            _position.value = positionMs
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
        _mediaController.value?.release()
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
                delay(intervalMs)
            }
        }
    }

    fun stopPositionTracking() {
        positionTrackingJob?.cancel()
        positionTrackingJob = null
    }


    fun getPlaybackMode(): PlaybackMode {
        return _playbackMode.value
    }


}