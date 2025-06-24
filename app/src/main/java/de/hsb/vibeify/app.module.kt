package de.hsb.vibeify

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import de.hsb.vibeify.data.repository.AuthRepository
import de.hsb.vibeify.data.repository.FirebaseAuthRepo
import de.hsb.vibeify.data.repository.FirebaseRepository
import de.hsb.vibeify.data.repository.FirestoreRepo
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.PlaylistRepositoryImpl
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.SongRepositoryImpl
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.data.repository.UserRepositoryImpl
import de.hsb.vibeify.services.MediaService
import de.hsb.vibeify.services.PlayerServiceV2
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
        songRepository: SongRepository
    ): PlaylistRepository {
        return PlaylistRepositoryImpl(songRepository)
    }
    @Singleton
    @Provides
    fun provideSongRepository(): SongRepository {
        return SongRepositoryImpl()
    }
    @Singleton
    @Provides
    fun provideFirebaseRepo(): FirestoreRepo {
        return FirebaseRepository()
    }
    @Singleton
    @Provides
    fun provideUserRepository(
        authRepository: AuthRepository,
        firestoreRepo: FirestoreRepo
    ): UserRepository {
        return UserRepositoryImpl(authRepository, firestoreRepo)
    }
    @Singleton
    @Provides
    fun providePlayerService(@ApplicationContext context: android.content.Context): PlayerServiceV2 {
        return PlayerServiceV2(context)
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