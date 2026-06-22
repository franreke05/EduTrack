package com.example.edutrack.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val SESSION_PREFS = "session_prefs"

val Context.sessionDataStore by preferencesDataStore(name = SESSION_PREFS)

object SessionPrefs {
    val USER_ID = stringPreferencesKey("user_id")
    val IS_LOGGED = booleanPreferencesKey("is_logged")
    val SELECTED_ANIO = stringPreferencesKey("selected_anio")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
    val LANGUAGE = stringPreferencesKey("language")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val ANIOS_JSON = stringPreferencesKey("anios_json")
}

fun Context.userIdFlow(): Flow<String?> =
    sessionDataStore.data.map { it[SessionPrefs.USER_ID] }.distinctUntilChanged()

// Indica si la sesion esta activa; por defecto es false.
fun Context.isLoggedFlow(): Flow<Boolean> =
    sessionDataStore.data.map { it[SessionPrefs.IS_LOGGED] ?: false }.distinctUntilChanged()

// Expone el anio seleccionado en la sesion.
fun Context.selectedAnioFlow(): Flow<String?> =
    sessionDataStore.data.map { it[SessionPrefs.SELECTED_ANIO] }.distinctUntilChanged()

fun Context.darkModeFlow(): Flow<Boolean> =
    sessionDataStore.data.map { it[SessionPrefs.DARK_MODE] ?: false }.distinctUntilChanged()

// Guarda el usuario y marca la sesion como iniciada.
suspend fun Context.setUserSession(userId: String) {
    sessionDataStore.edit {
        it[SessionPrefs.USER_ID] = userId
        it[SessionPrefs.IS_LOGGED] = true
    }
}

// Actualiza el anio seleccionado en la sesion.
suspend fun Context.setSelectedAnio(anioId: String) {
    sessionDataStore.edit {
        it[SessionPrefs.SELECTED_ANIO] = anioId
    }
}

suspend fun Context.setDarkMode(enabled: Boolean) {
    sessionDataStore.edit {
        it[SessionPrefs.DARK_MODE] = enabled
    }
}

fun Context.isOnboardedFlow(): Flow<Boolean> =
    sessionDataStore.data.map { it[SessionPrefs.IS_ONBOARDED] ?: false }.distinctUntilChanged()

suspend fun Context.setOnboarded() {
    sessionDataStore.edit { it[SessionPrefs.IS_ONBOARDED] = true }
}

fun Context.languageFlow(): Flow<String> =
    sessionDataStore.data.map { it[SessionPrefs.LANGUAGE] ?: "es" }.distinctUntilChanged()

suspend fun Context.setLanguage(lang: String) {
    sessionDataStore.edit { it[SessionPrefs.LANGUAGE] = lang }
}

fun Context.notificationsEnabledFlow(): Flow<Boolean> =
    sessionDataStore.data.map { it[SessionPrefs.NOTIFICATIONS_ENABLED] ?: true }.distinctUntilChanged()

suspend fun Context.setNotificationsEnabled(enabled: Boolean) {
    sessionDataStore.edit { it[SessionPrefs.NOTIFICATIONS_ENABLED] = enabled }
}

// Limpia los datos de sesion almacenados.
suspend fun Context.clearSession() {
    sessionDataStore.edit {
        it.remove(SessionPrefs.USER_ID)
        it.remove(SessionPrefs.IS_LOGGED)
        it.remove(SessionPrefs.SELECTED_ANIO)
    }
}
