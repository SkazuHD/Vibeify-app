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
import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.data.repository.UserRepositoryImpl
import de.hsb.vibeify.services.MediaService
import de.hsb.vibeify.services.PlayerServiceV2

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    @Provides
    fun provideAuthRepository(): AuthRepository {
        return FirebaseAuthRepo()
    }

    @Provides
    fun providePlaylistRepository(): PlaylistRepository {
        return PlaylistRepositoryImpl()
    }

    @Provides
    fun provideFirebaseRepo(): FirestoreRepo {
        return FirebaseRepository()
    }

    @Provides
    fun provideUserRepository(
        authRepository: AuthRepository,
        firestoreRepo: FirestoreRepo
    ): UserRepository {
        return UserRepositoryImpl(authRepository, firestoreRepo)
    }

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