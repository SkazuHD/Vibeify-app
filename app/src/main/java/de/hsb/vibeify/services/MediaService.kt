package de.hsb.vibeify.services

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture


class MediaService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null

    // Implementiere vollständige Callback-Funktionalität
    private val callback = object : MediaLibrarySession.Callback {

        // Wird aufgerufen, wenn ein Client eine Verbindung zur Session anfordert
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()

            // Füge verfügbare Session-Commands hinzu
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands
            )
        }

        // Wird aufgerufen, um die Root-Bibliothek zu erhalten
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
                    // Erstelle Kategorien für die Musikbibliothek
                    val categories = ImmutableList.of(
                        createCategoryItem("songs", "All Songs"),
                        createCategoryItem("albums", "Albums"),
                        createCategoryItem("artists", "Artists"),
                        createCategoryItem("playlists", "Playlists")
                    )
                    Futures.immediateFuture(LibraryResult.ofItemList(categories, params))
                }
                "songs" -> {
                    // Hier würdest du normalerweise deine Songs aus der Datenbank laden
                    val songs = loadSongs()
                    Futures.immediateFuture(LibraryResult.ofItemList(songs, params))
                }
                "albums" -> {
                    // Hier würdest du Alben laden
                    val albums = loadAlbums()
                    Futures.immediateFuture(LibraryResult.ofItemList(albums, params))
                }
                "artists" -> {
                    // Hier würdest du Künstler laden
                    val artists = loadArtists()
                    Futures.immediateFuture(LibraryResult.ofItemList(artists, params))
                }
                "playlists" -> {
                    // Hier würdest du Playlists laden
                    val playlists = loadPlaylists()
                    Futures.immediateFuture(LibraryResult.ofItemList(playlists, params))
                }
                else -> {
                    // Unbekannte Parent-ID
                    Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
                }
            }
        }

        // Wird aufgerufen, um ein bestimmtes Item zu erhalten
        @OptIn(UnstableApi::class)
        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            // Hier würdest du das spezifische Item aus deiner Datenbank laden
            val item = findItemById(mediaId)
            return if (item != null) {
                Futures.immediateFuture(LibraryResult.ofItem(item, null))
            } else {
                Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE))
            }
        }

        // Wird aufgerufen, wenn ein Item zum Abspielen hinzugefügt werden soll
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map { mediaItem ->
                if (mediaItem.localConfiguration == null) {
                    // Lade die vollständigen Metadaten und URI für das Item
                    loadFullMediaItem(mediaItem.mediaId)
                } else {
                    mediaItem
                }
            }.toMutableList()

            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_NONE)
            .setSpatializationBehavior(C.SPATIALIZATION_BEHAVIOR_AUTO)
            .build()

        val trackSelector = DefaultTrackSelector(this)

        val player = ExoPlayer.Builder(this)
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

    // Helper-Methoden für die Callback-Implementierung
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

    private fun loadSongs(): ImmutableList<MediaItem> {
        // TODO: Hier würdest du deine Songs aus der Datenbank/Repository laden
        // Für jetzt gebe ich eine leere Liste zurück
        return ImmutableList.of()
    }

    private fun loadAlbums(): ImmutableList<MediaItem> {
        // TODO: Hier würdest du deine Alben aus der Datenbank/Repository laden
        return ImmutableList.of()
    }

    private fun loadArtists(): ImmutableList<MediaItem> {
        // TODO: Hier würdest du deine Künstler aus der Datenbank/Repository laden
        return ImmutableList.of()
    }

    private fun loadPlaylists(): ImmutableList<MediaItem> {
        // TODO: Hier würdest du deine Playlists aus der Datenbank/Repository laden
        return ImmutableList.of()
    }

    private fun findItemById(mediaId: String): MediaItem? {
        // TODO: Hier würdest du ein spezifisches Item anhand der mediaId aus der Datenbank laden
        return null
    }

    private fun loadFullMediaItem(mediaId: String): MediaItem {
        // TODO: Hier würdest du das vollständige MediaItem mit URI und Metadaten laden
        // Für jetzt gebe ich ein Basis-Item zurück
        return MediaItem.Builder()
            .setMediaId(mediaId)
            .build()
    }
}