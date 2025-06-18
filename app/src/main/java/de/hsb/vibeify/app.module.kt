package de.hsb.vibeify

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hsb.vibeify.data.repository.AuthRepository
import de.hsb.vibeify.data.repository.FirebaseAuthRepo
import de.hsb.vibeify.data.repository.FirebaseRepository
import de.hsb.vibeify.data.repository.FirestoreRepo
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.PlaylistRepositoryImpl

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
}
