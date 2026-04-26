package com.example.edutrack.feature.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.BuildConfig
import com.example.edutrack.R
import com.example.edutrack.data.auth.AuthRepository
import com.example.edutrack.data.auth.AuthRepositoryResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { Login, Register }

data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val emailOrUsername: String = "",
    val displayName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successUserId: String? = null
)

class AuthViewModel(
    application: Application,
    private val repository: AuthRepository = AuthRepository()
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onEmailOrUsernameChange(value: String) = _uiState.update { it.copy(emailOrUsername = value, errorMessage = null) }
    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun clearNavigationEvent() = _uiState.update { it.copy(successUserId = null) }
    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                mode = if (it.mode == AuthMode.Login) AuthMode.Register else AuthMode.Login,
                confirmPassword = "",
                displayName = "",
                errorMessage = null
            )
        }
    }

    fun submit() {
        val state = uiState.value
        if (state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (state.mode == AuthMode.Login) {
                repository.signInWithEmailOrUsername(state.emailOrUsername, state.password)
            } else {
                repository.registerWithEmail(
                    email = state.emailOrUsername,
                    displayName = state.displayName,
                    password = state.password,
                    confirmPassword = state.confirmPassword
                )
            }
            handleAuthResult(result)
        }
    }

    fun signInWithGoogle(context: Context) {
        if (uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val serverClientId = runCatching { context.getString(R.string.default_web_client_id) }.getOrNull()
            if (serverClientId.isNullOrBlank() || serverClientId == "default_web_client_id") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Falta configurar default_web_client_id en Firebase."
                    )
                }
                return@launch
            }

            val tokenResult = runCatching {
                requestGoogleIdToken(context, serverClientId, filterAuthorizedAccounts = true)
            }.recoverCatching { firstError ->
                if (firstError is NoCredentialException) {
                    requestGoogleIdToken(context, serverClientId, filterAuthorizedAccounts = false)
                } else {
                    throw firstError
                }
            }

            tokenResult
                .onSuccess { token -> handleAuthResult(repository.signInWithGoogleToken(token)) }
                .onFailure { error -> handleGoogleError(error) }
        }
    }

    private suspend fun requestGoogleIdToken(
        context: Context,
        serverClientId: String,
        filterAuthorizedAccounts: Boolean
    ): String {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterAuthorizedAccounts)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credentialManager = CredentialManager.create(context)
        val credential = credentialManager.getCredential(context, request).credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw IllegalStateException("Google returned an unsupported credential type.")
    }

    private fun handleAuthResult(result: AuthRepositoryResult) {
        when (result) {
            is AuthRepositoryResult.Success -> {
                _uiState.update { it.copy(isLoading = false, successUserId = result.user.uid) }
            }
            is AuthRepositoryResult.Error -> {
                if (BuildConfig.DEBUG) {
                    Log.d("AuthViewModel", result.userMessage, result.debugCause)
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = result.userMessage) }
            }
        }
    }

    private fun handleGoogleError(error: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.d("AuthViewModel", "Google credential flow failed", error)
        }
        val message = when (error) {
            is GetCredentialCancellationException -> "Inicio con Google cancelado."
            is NoCredentialException -> "No hay cuentas de Google disponibles en este dispositivo."
            is GoogleIdTokenParsingException -> "Google devolvió una credencial inválida."
            is GetCredentialException -> "No se pudo abrir Google Sign-In. Revisa Google Play Services y Firebase."
            is IllegalArgumentException -> "El cliente web de Google no está configurado correctamente."
            else -> "No se pudo iniciar sesión con Google. Inténtalo de nuevo."
        }
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}
