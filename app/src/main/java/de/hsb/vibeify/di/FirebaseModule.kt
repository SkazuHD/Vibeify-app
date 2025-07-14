package de.hsb.vibeify.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


// FirebaseModule provides Firebase-related dependencies such as FirebaseDatabase, FirebaseFirestore, and FirebaseAuth.
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val database =
            FirebaseDatabase.getInstance("https://vibeify-16f99-default-rtdb.europe-west1.firebasedatabase.app")
        database.setPersistenceEnabled(true)
        return database
    }


    @Provides
    @Singleton
    fun provideFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
