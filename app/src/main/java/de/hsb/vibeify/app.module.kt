package de.hsb.vibeify

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
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
import de.hsb.vibeify.services.SearchService
import de.hsb.vibeify.services.SearchServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Singleton
    @Provides
    fun provideAuthRepository(): AuthRepository {
        return FirebaseAuthRepo()
    }

    @Singleton
    @Provides
    fun providePlaylistRepository(
    ): PlaylistRepository {
        return PlaylistRepositoryImpl()
    }

    @Singleton
    @Provides
    fun provideSongRepository(): SongRepository {
        return SongRepositoryImpl()
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        authRepository: AuthRepository,
        @ApplicationContext context: android.content.Context
    ): UserRepository {
        return UserRepositoryImpl(authRepository, context)
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
    fun providePlayerService(@ApplicationContext context: android.content.Context): PlayerServiceV2 {
        return PlayerServiceV2(context)
    }

    @Singleton
    @Provides
    fun provideSearchService(
        songRepository: SongRepository,
        playlistRepository: PlaylistRepository,
        artistRepository: ArtistRepository,
        userRepository: UserRepository
    ): SearchService {
        return SearchServiceImpl(
            songRepository,
            playlistRepository,
            artistRepository,
            userRepository
        )
    }

    @Singleton
    @Provides
    fun providePlaylistService(
        playlistRepository: PlaylistRepository,
        userRepository: UserRepository,
        songRepository: SongRepository,
        discoveryService: DiscoveryService
    ): PlaylistService {
        return de.hsb.vibeify.services.PlaylistServiceImpl(
            playlistRepository,
            songRepository,
            userRepository,
            discoveryService
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