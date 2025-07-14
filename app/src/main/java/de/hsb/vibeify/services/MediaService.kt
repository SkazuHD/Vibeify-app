package de.hsb.vibeify.services

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject


/**
 * MediaService is a service that provides a media library for the Vibeify app.
 * It uses ExoPlayer to handle media playback and MediaLibrarySession to manage the media library.
 */

@AndroidEntryPoint
class MediaService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null

    @Inject
    lateinit var songRepository: SongRepository

    @Inject
    lateinit var playlistRepository: PlaylistRepository

    @Inject
    lateinit var playerServiceV2: PlayerServiceV2

    @Inject
    lateinit var presenceService: PresenceService


    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Callback for handling media library session events
    private val callback = object : MediaLibrarySession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()

            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands
            )
        }
        // session commands are not used in this service, so we return the default implementation

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId("root")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setTitle("Vibeify Music Library")
                        .build()
                )
                .build()

            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        // Handles requests for children of a given media item
        @OptIn(UnstableApi::class)
        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return when (parentId) {
                "root" -> {
                    val categories = ImmutableList.of(
                        createCategoryItem("songs", "All Songs"),
                        createCategoryItem("albums", "Albums"),
                        createCategoryItem("artists", "Artists"),
                        createCategoryItem("playlists", "Playlists")
                    )
                    Futures.immediateFuture(LibraryResult.ofItemList(categories, params))
                }

                "songs" -> {
                    val songs = loadSongs()
                    Futures.immediateFuture(LibraryResult.ofItemList(songs, params))
                }

                "albums" -> {
                    val albums = loadAlbums()
                    Futures.immediateFuture(LibraryResult.ofItemList(albums, params))
                }

                "artists" -> {
                    val artists = loadArtists()
                    Futures.immediateFuture(LibraryResult.ofItemList(artists, params))
                }

                "playlists" -> {
                    val playlists = loadPlaylists()
                    Futures.immediateFuture(LibraryResult.ofItemList(playlists, params))
                }

                else -> {
                    Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
                }
            }
        }

        // Handles requests for a specific media item by its ID
        @OptIn(UnstableApi::class)
        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val item = findItemById(mediaId)
            return if (item != null) {
                Futures.immediateFuture(LibraryResult.ofItem(item, null))
            } else {
                Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
            }
        }

        // Handles requests to add media items to the session
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map { mediaItem ->
                if (mediaItem.localConfiguration == null) {
                    loadFullMediaItem(mediaItem.mediaId)
                } else {
                    mediaItem
                }
            }.toMutableList()

            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    // Initialize the media library session and ExoPlayer
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Does this work?
        val cacheSize = 500 * 1024 * 1024
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize.toLong())
        val databaseProvider = StandaloneDatabaseProvider(applicationContext)
        val simpleCache =
            SimpleCache(File(applicationContext.cacheDir, "media"), cacheEvictor, databaseProvider)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        val cacheDataSourceFactory = CacheDataSource.Factory().setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaSourceFactory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_NONE)
            .setSpatializationBehavior(C.SPATIALIZATION_BEHAVIOR_AUTO)
            .build()

        val trackSelector = DefaultTrackSelector(this)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(30_000)
            .setPauseAtEndOfMediaItems(false)
            .setSkipSilenceEnabled(false)
            .setUsePlatformDiagnostics(false)
            .build()
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback)
            .setId("VibeifyMediaSession")
            .build()
    }

    // Returns the media library session for this service
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

    // Helper function to create a MediaItem for a category
    private fun createCategoryItem(mediaId: String, title: String): MediaItem {
        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setTitle(title)
                    .build()
            )
            .build()
    }
    // Helper functions to load songs, albums, artists, and playlists from the repository
    private fun loadSongs(): ImmutableList<MediaItem> {
        return try {
            val songs = runBlocking {
                songRepository.getAllSongs()
            }
            ImmutableList.copyOf(songs.map { song ->
                Song.toMediaItem(song)
            })
        } catch (e: Exception) {
            ImmutableList.of()
        }
    }

    private fun loadAlbums(): ImmutableList<MediaItem> {
        return try {
            val songs = runBlocking {
                songRepository.getAllSongs()
            }
            val albums = songs.groupBy { it.album }.map { (albumName, albumSongs) ->
                MediaItem.Builder()
                    .setMediaId("album_$albumName")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .setTitle(albumName)
                            .setArtist(albumSongs.firstOrNull()?.artist ?: "Unknown Artist")
                            .setTotalTrackCount(albumSongs.size)
                            .build()
                    )
                    .build()
            }
            ImmutableList.copyOf(albums)
        } catch (e: Exception) {
            ImmutableList.of()
        }
    }

    private fun loadArtists(): ImmutableList<MediaItem> {
        return try {
            val songs = runBlocking {
                songRepository.getAllSongs()
            }
            val artists = songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
                MediaItem.Builder()
                    .setMediaId("artist_$artistName")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .setTitle(artistName)
                            .setTotalTrackCount(artistSongs.size)
                            .build()
                    )
                    .build()
            }
            ImmutableList.copyOf(artists)
        } catch (e: Exception) {
            ImmutableList.of()
        }
    }

    private fun loadPlaylists(): ImmutableList<MediaItem> {
        return try {
            val playlists = runBlocking {
                playlistRepository.getAllPlaylists()
            }
            ImmutableList.copyOf(playlists.map { playlist ->
                MediaItem.Builder()
                    .setMediaId("playlist_${playlist.id}")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .setTitle(playlist.title)
                            .setDescription(playlist.description)
                            .setTotalTrackCount(playlist.songIds.size)
                            .build()
                    )
                    .build()
            })
        } catch (e: Exception) {
            ImmutableList.of()
        }
    }

    private fun findItemById(mediaId: String): MediaItem? {
        return try {
            when {
                mediaId.startsWith("playlist_") -> {
                    val playlistId = mediaId.removePrefix("playlist_")
                    val playlist = runBlocking {
                        playlistRepository.getPlaylistById(playlistId)
                    }
                    playlist?.let {
                        MediaItem.Builder()
                            .setMediaId(mediaId)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setIsBrowsable(true)
                                    .setIsPlayable(false)
                                    .setTitle(it.title)
                                    .setDescription(it.description)
                                    .build()
                            )
                            .build()
                    }
                }

                mediaId.startsWith("album_") || mediaId.startsWith("artist_") -> {
                    null
                }

                else -> {
                    val song = runBlocking {
                        songRepository.getSongById(mediaId)
                    }
                    song?.let { Song.toMediaItem(song) }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun loadFullMediaItem(mediaId: String): MediaItem {
        return try {
            val song = runBlocking {
                songRepository.getSongById(mediaId)
            }
            song?.let {
                Song.toMediaItem(song)
            } ?: MediaItem.Builder().setMediaId(mediaId).build()
        } catch (e: Exception) {
            MediaItem.Builder().setMediaId(mediaId).build()
        }
    }
}