package com.example.edutrack.repository

import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.domain.PremiumCache
import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null
)

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signOut(): Result<Unit>
}

interface AcademicRepository {
    fun observeYears(userId: String): Flow<List<Anio>>
    fun observeSubjects(userId: String, anioId: String): Flow<List<Asignatura>>
    fun observeGrades(userId: String, anioId: String, asignaturaId: String): Flow<List<Notas>>
    suspend fun saveYear(userId: String, anio: Anio): Result<String>
    suspend fun saveSubject(userId: String, anioId: String, asignatura: Asignatura): Result<String>
    suspend fun saveGrade(userId: String, anioId: String, asignaturaId: String, nota: Notas): Result<String>
}

interface GroupsRepository {
    fun observeGroups(userId: String): Flow<List<Group>>
    suspend fun createGroup(ownerUid: String, group: Group): Result<String>
    suspend fun joinGroup(userId: String, inviteCode: String): Result<String>
}

interface PremiumRepository {
    fun observePremium(userId: String): Flow<PremiumCache>
}

interface UserPreferencesRepository {
    val selectedYearId: Flow<String?>
    val darkModeEnabled: Flow<Boolean>
    suspend fun setSelectedYear(yearId: String): Result<Unit>
    suspend fun setDarkMode(enabled: Boolean): Result<Unit>
}
