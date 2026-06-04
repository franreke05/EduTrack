package com.example.edutrack.data.di

import android.content.Context
import com.example.edutrack.data.repository.GradeRepository
import com.example.edutrack.data.repository.GroupRepository
import com.example.edutrack.data.repository.PremiumRepository
import com.example.edutrack.data.repository.SubjectRepository
import com.example.edutrack.data.repository.UserRepository
import com.google.android.datatransport.runtime.dagger.Module
import com.google.android.datatransport.runtime.dagger.Provides
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para proporcionar instancias de repositorios y dependencias globales.
 * Centraliza la inyección de dependencias para acceso a datos.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    private const val DB_URL = "https://edutrack-5579f-default-rtdb.europe-west1.firebasedatabase.app/"

    /**
     * Proporciona instancia Singleton de FirebaseDatabase.
     * Asegura que se use la misma instancia en toda la aplicación.
     *
     * @return FirebaseDatabase configurada con la URL de Edutrack
     */
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance(DB_URL)
    }

    /**
     * Proporciona instancia Singleton de UserRepository.
     *
     * @param database Instancia de FirebaseDatabase inyectada
     * @return UserRepository para gestionar operaciones de usuario
     */
    @Provides
    @Singleton
    fun provideUserRepository(database: FirebaseDatabase): UserRepository {
        return UserRepository(database)
    }

    /**
     * Proporciona instancia Singleton de SubjectRepository.
     *
     * @param database Instancia de FirebaseDatabase inyectada
     * @return SubjectRepository para gestionar operaciones de asignaturas
     */
    @Provides
    @Singleton
    fun provideSubjectRepository(database: FirebaseDatabase): SubjectRepository {
        return SubjectRepository(database)
    }

    /**
     * Proporciona instancia Singleton de GradeRepository.
     *
     * @param database Instancia de FirebaseDatabase inyectada
     * @return GradeRepository para gestionar operaciones de notas
     */
    @Provides
    @Singleton
    fun provideGradeRepository(database: FirebaseDatabase): GradeRepository {
        return GradeRepository(database)
    }

    /**
     * Proporciona instancia Singleton de GroupRepository.
     *
     * @param database Instancia de FirebaseDatabase inyectada
     * @return GroupRepository para gestionar operaciones de grupos
     */
    @Provides
    @Singleton
    fun provideGroupRepository(database: FirebaseDatabase): GroupRepository {
        return GroupRepository(database)
    }

    /**
     * Proporciona instancia Singleton de PremiumRepository.
     *
     * @param database Instancia de FirebaseDatabase inyectada
     * @return PremiumRepository para gestionar suscripción premium
     */
    @Provides
    @Singleton
    fun providePremiumRepository(database: FirebaseDatabase): PremiumRepository {
        return PremiumRepository(database)
    }
}
