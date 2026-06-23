package com.example.edutrack.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import com.example.edutrack.data.SessionPrefs
import com.example.edutrack.data.sessionDataStore
import com.example.edutrack.premiumCacheRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun rememberPremiumCache(userId: String?): State<PremiumCache> {
    val state = remember { mutableStateOf(PremiumCache()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Carga la caché local inmediatamente — evita el flash de anuncios al arrancar
    LaunchedEffect(userId) {
        if (userId.isNullOrBlank()) return@LaunchedEffect
        val prefs = context.sessionDataStore.data.first()
        val cachedIsPremium = prefs[SessionPrefs.PREMIUM_IS_PREMIUM] ?: false
        val cachedExpiresAt = prefs[SessionPrefs.PREMIUM_EXPIRES_AT] ?: 0L
        // Solo aplica si no ha expirado — misma lógica que isPremiumValid()
        if (cachedIsPremium && cachedExpiresAt > System.currentTimeMillis() && state.value == PremiumCache()) {
            state.value = PremiumCache(isPremium = true, expiresAt = cachedExpiresAt)
        }
    }

    DisposableEffect(userId) {
        if (userId.isNullOrBlank()) return@DisposableEffect onDispose {}
        val ref = premiumCacheRef(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cache = PremiumCache(
                    isPremium = snapshot.child("isPremium").getValue(Boolean::class.java) ?: false,
                    expiresAt = snapshot.child("expiresAt").getValue(Long::class.java),
                    productId = snapshot.child("productId").getValue(String::class.java)
                )
                state.value = cache
                scope.launch {
                    context.sessionDataStore.edit {
                        it[SessionPrefs.PREMIUM_IS_PREMIUM] = cache.isPremium
                        it[SessionPrefs.PREMIUM_EXPIRES_AT] = cache.expiresAt ?: 0L
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) = Unit
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

@Composable
fun rememberUserPlan(userId: String?): State<UserPlan> {
    val state = remember { mutableStateOf(UserPlan.FREE) }
    DisposableEffect(userId) {
        if (userId.isNullOrBlank()) {
            state.value = UserPlan.FREE
            return@DisposableEffect onDispose {}
        }
        val ref = premiumCacheRef(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isPremium = snapshot.child("isPremium").getValue(Boolean::class.java) ?: false
                state.value = if (isPremium) UserPlan.PREMIUM else UserPlan.FREE
            }

            override fun onCancelled(error: DatabaseError) {
                state.value = UserPlan.FREE
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}
