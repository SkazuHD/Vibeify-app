package de.hsb.vibeify.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import de.hsb.vibeify.api.generated.apis.DefaultApi
import de.hsb.vibeify.api.generated.infrastructure.ApiClient
import de.hsb.vibeify.data.repository.ArtistRepository
import de.hsb.vibeify.data.repository.ArtistRepositoryImpl
import de.hsb.vibeify.data.repository.AuthRepository
import de.hsb.vibeify.data.repository.FirebaseAuthRepo
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.PlaylistRepositoryImpl
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.SongRepositoryImpl
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.data.repository.UserRepositoryImpl
import de.hsb.vibeify.services.AnalyticsService
import de.hsb.vibeify.services.DiscoveryService
import de.hsb.vibeify.services.MediaService
import de.hsb.vibeify.services.PlayerServiceV2
import de.hsb.vibeify.services.PlaylistService
import de.hsb.vibeify.services.PlaylistServiceImpl
import de.hsb.vibeify.services.SearchService
import de.hsb.vibeify.services.SearchServiceImpl
import javax.inject.Singleton


/** * Hilt entry point for the PlayerServiceV2.
 * This allows us to access the PlayerServiceV2 from other parts of the app.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface PlayerServiceEntryPoint {
    fun getPlayerService(): PlayerServiceV2

    companion object {
        fun get(context: Context): PlayerServiceV2 {
            return EntryPointAccessors.fromApplication(
                context.applicationContext,
                PlayerServiceEntryPoint::class.java
            ).getPlayerService()
        }
    }
}


/** Hilt module for providing dependencies in the application.
 * This module provides various repositories and services used throughout the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return FirebaseAuthRepo(firebaseAuth)
    }

    @Singleton
    @Provides
    fun providePlaylistRepository(
        firebaseFirestore: FirebaseFirestore
    ): PlaylistRepository {
        return PlaylistRepositoryImpl(firebaseFirestore)
    }

    @Singleton
    @Provides
    fun provideSongRepository(
        firebaseFirestore: FirebaseFirestore,
    ): SongRepository {
        return SongRepositoryImpl(firebaseFirestore)
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        authRepository: AuthRepository,
        @ApplicationContext context: Context,
        firebaseFirestore: FirebaseFirestore,
        defaultApi: DefaultApi
    ): UserRepository {
        return UserRepositoryImpl(authRepository, context, firebaseFirestore, defaultApi)
    }

    @Singleton
    @Provides
    fun provideArtistRepository(
        songRepository: SongRepository,
    ): ArtistRepository {
        return ArtistRepositoryImpl(songRepository)
    }

    @Singleton
    @Provides
    fun providePlayerService(
        @ApplicationContext context: Context,
        userRepository: UserRepository
    ): PlayerServiceV2 {
        return PlayerServiceV2(context, userRepository)
    }

    @Singleton
    @Provides
    fun provideSearchService(
        songRepository: SongRepository,
        playlistRepository: PlaylistRepository,
        artistRepository: ArtistRepository,
        userRepository: UserRepository,
        discoveryService: DiscoveryService
    ): SearchService {
        return SearchServiceImpl(
            songRepository,
            playlistRepository,
            artistRepository,
            userRepository,
            discoveryService
        )
    }

    @Singleton
    @Provides
    fun providePlaylistService(
        @ApplicationContext context: Context,
        playlistRepository: PlaylistRepository,
        userRepository: UserRepository,
        songRepository: SongRepository,
        discoveryService: DiscoveryService,
        defaultApi: DefaultApi,
    ): PlaylistService {
        return PlaylistServiceImpl(
            playlistRepository,
            songRepository,
            userRepository,
            discoveryService,
            defaultApi,
            context
        )
    }

    @Singleton
    @Provides
    fun provideAnalyticsService(
        userRepository: UserRepository,
        playerServiceV2: PlayerServiceV2
    ): AnalyticsService {
        return AnalyticsService(userRepository, playerServiceV2)
    }

    @Singleton
    @Provides
    fun provideDefaultApi(): DefaultApi {
        val apiClient = ApiClient(baseUrl = "https://vibeify-app.skazu.net/")
        val webService = apiClient.createService(DefaultApi::class.java)
        return webService
    }
}

@Module
@InstallIn(ServiceComponent::class)
object MediaServiceModule {
    @Provides
    @ServiceScoped
    fun provideMediaService(): MediaService {
        return MediaService()
    }
}