package com.example.edutrack.data.auth

import android.util.Log
import com.example.edutrack.BuildConfig
import com.example.edutrack.data.firebase.FirebasePaths
import com.example.edutrack.dataclass.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

data class AuthenticatedUser(val uid: String)

sealed interface AuthRepositoryResult {
    data class Success(val user: AuthenticatedUser) : AuthRepositoryResult
    data class Error(val userMessage: String, val debugCause: Throwable? = null) : AuthRepositoryResult
}

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
    private val database: DatabaseReference = Firebase.database.reference
) {
    suspend fun signInWithEmailOrUsername(input: String, password: String): AuthRepositoryResult {
        val loginInput = input.trim()
        if (loginInput.isBlank() || password.isBlank()) {
            return AuthRepositoryResult.Error("Introduce tu correo o usuario y la contraseña.")
        }

        return try {
            if (!loginInput.contains("@")) {
                return AuthRepositoryResult.Error("Por seguridad, inicia sesión con tu correo electrónico.")
            }
            val email = loginInput

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthRepositoryResult.Error("No se pudo abrir la sesión.")
            persistUser(user)
            AuthRepositoryResult.Success(AuthenticatedUser(user.uid))
        } catch (e: FirebaseAuthInvalidUserException) {
            logDebug("Email sign-in failed: user not found", e)
            AuthRepositoryResult.Error("Esta cuenta no existe o fue desactivada.", e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            logDebug("Email sign-in failed: invalid credentials", e)
            AuthRepositoryResult.Error("Correo o contraseña incorrectos.", e)
        } catch (e: Exception) {
            logDebug("Email sign-in failed", e)
            AuthRepositoryResult.Error("No se pudo iniciar sesión. Revisa tu conexión e inténtalo de nuevo.", e)
        }
    }

    suspend fun registerWithEmail(email: String, displayName: String, password: String, confirmPassword: String): AuthRepositoryResult {
        val cleanEmail = email.trim()
        val cleanName = displayName.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return AuthRepositoryResult.Error("Introduce un correo y una contraseña.")
        }
        if (!cleanEmail.contains("@")) {
            return AuthRepositoryResult.Error("Introduce un correo electrónico válido.")
        }
        if (password.length < 6) {
            return AuthRepositoryResult.Error("La contraseña debe tener al menos 6 caracteres.")
        }
        if (password != confirmPassword) {
            return AuthRepositoryResult.Error("Las contraseñas no coinciden.")
        }

        return try {
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: return AuthRepositoryResult.Error("La cuenta se creó, pero no se pudo abrir sesión.")
            persistUser(user, cleanName.ifBlank { null })
            sendVerificationEmailIfNeeded(user)
            AuthRepositoryResult.Success(AuthenticatedUser(user.uid))
        } catch (e: FirebaseAuthUserCollisionException) {
            logDebug("Register failed: user exists", e)
            AuthRepositoryResult.Error("Ya existe una cuenta con ese correo.", e)
        } catch (e: Exception) {
            logDebug("Register failed", e)
            AuthRepositoryResult.Error("No se pudo crear la cuenta. Revisa los datos e inténtalo de nuevo.", e)
        }
    }

    suspend fun signInWithGoogleToken(idToken: String?): AuthRepositoryResult {
        if (idToken.isNullOrBlank()) {
            return AuthRepositoryResult.Error("Google no devolvió un token válido. Inténtalo de nuevo.")
        }

        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthRepositoryResult.Error("Google inició sesión, pero Firebase no devolvió usuario.")
            persistUser(user)
            AuthRepositoryResult.Success(AuthenticatedUser(user.uid))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            logDebug("Google sign-in failed: invalid credential", e)
            AuthRepositoryResult.Error(
                "La credencial de Google no es válida. Revisa el SHA-1/SHA-256 y el cliente web de Firebase.",
                e
            )
        } catch (e: Exception) {
            logDebug("Google sign-in failed", e)
            AuthRepositoryResult.Error("Firebase rechazó el inicio con Google. Revisa la configuración de Firebase.", e)
        }
    }

    private suspend fun persistUser(user: FirebaseUser, displayName: String? = null) {
        val userRef = database.child("users").child(user.uid)
        val profileRef = userRef.child("profile")

        val snapshot = profileRef.get().await()
        val now = System.currentTimeMillis()
        val resolvedName = displayName?.takeIf { it.isNotBlank() }
            ?: user.displayName
            ?: user.email?.substringBefore("@")
            ?: "Usuario"

        if (snapshot.exists()) {
            profileRef.updateChildren(
                mapOf(
                    "id" to user.uid,
                    "nombre" to resolvedName,
                    "email" to (user.email ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "lastLoginAt" to now,
                    "updatedAt" to now
                )
            ).await()
        } else {
            FirebasePaths.root.updateChildren(
                mapOf(
                    "users/${user.uid}/profile/id" to user.uid,
                    "users/${user.uid}/profile/nombre" to resolvedName,
                    "users/${user.uid}/profile/email" to (user.email ?: ""),
                    "users/${user.uid}/profile/photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "users/${user.uid}/profile/createdAt" to now,
                    "users/${user.uid}/profile/updatedAt" to now,
                    "users/${user.uid}/profile/lastLoginAt" to now,
                    "users/${user.uid}/settings/preferences" to UserPreferences()
                )
            ).await()
        }
    }

    private suspend fun sendVerificationEmailIfNeeded(user: FirebaseUser) {
        if (!user.isEmailVerified && user.providerData.any { it.providerId == "password" }) {
            runCatching { user.sendEmailVerification().await() }
                .onFailure { logDebug("Verification email failed", it) }
        }
    }

    private fun logDebug(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d("AuthRepository", message, throwable)
        }
    }
}
