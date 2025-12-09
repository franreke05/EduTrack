package com.example.edutrack.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")

object SessionPrefs {
    val USER_ID = stringPreferencesKey("user_id")
    val IS_LOGGED = booleanPreferencesKey("is_logged")
    val SELECTED_ANIO = stringPreferencesKey("selected_anio")
}

fun Context.userIdFlow(): Flow<String?> =
    sessionDataStore.data.map { it[SessionPrefs.USER_ID] }

fun Context.isLoggedFlow(): Flow<Boolean> =
    sessionDataStore.data.map { it[SessionPrefs.IS_LOGGED] ?: false }

fun Context.selectedAnioFlow(): Flow<String?> =
    sessionDataStore.data.map { it[SessionPrefs.SELECTED_ANIO] }

suspend fun Context.setUserSession(userId: String) {
    sessionDataStore.edit {
        it[SessionPrefs.USER_ID] = userId
        it[SessionPrefs.IS_LOGGED] = true
    }
}

suspend fun Context.setSelectedAnio(anioId: String) {
    sessionDataStore.edit {
        it[SessionPrefs.SELECTED_ANIO] = anioId
    }
}

suspend fun Context.clearSession() {
    sessionDataStore.edit {
        it.remove(SessionPrefs.USER_ID)
        it.remove(SessionPrefs.IS_LOGGED)
        it.remove(SessionPrefs.SELECTED_ANIO)
    }
}
