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

// Indica si la sesion esta activa; por defecto es false.
fun Context.isLoggedFlow(): Flow<Boolean> =
    sessionDataStore.data.map { it[SessionPrefs.IS_LOGGED] ?: false }

// Expone el anio seleccionado en la sesion.
fun Context.selectedAnioFlow(): Flow<String?> =
    sessionDataStore.data.map { it[SessionPrefs.SELECTED_ANIO] }

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

// Limpia los datos de sesion almacenados.
suspend fun Context.clearSession() {
    sessionDataStore.edit {
        it.remove(SessionPrefs.USER_ID)
        it.remove(SessionPrefs.IS_LOGGED)
        it.remove(SessionPrefs.SELECTED_ANIO)
    }
}
