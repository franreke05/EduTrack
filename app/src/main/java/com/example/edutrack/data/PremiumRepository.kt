package com.example.edutrack.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/**
 * PremiumRepository: Centraliza acceso a datos de Premium.
 *
 * IMPORTANTE:
 * - Solo LECTURA desde el cliente
 * - NUNCA escribir premiumCache desde aquí
 * - El servidor (Cloud Functions) es el único que puede escribir
 * - Si el usuario intenta escribir, Firebase rechaza (reglas)
 *
 * La escritura solo ocurre cuando:
 * 1. Compra en Google Play
 * 2. Cloud Function verifyPremiumPurchase() valida
 * 3. Backend escribe el resultado
 */

data class PremiumCache(
    val isPremium: Boolean = false,
    val productId: String? = null,
    val expiresAt: Long? = null,
    val validatedAt: Long? = null,
    val source: String? = null  // "google_play_server" o null
)

interface PremiumRepositoryListener {
    fun onPremiumCacheUpdated(cache: PremiumCache)
    fun onPremiumCacheError(error: String)
}

class PremiumRepository {
    private val auth = FirebaseAuth.getInstance()

    fun observePremiumCache(listener: PremiumRepositoryListener) {
        val userId = auth.currentUser?.uid ?: return

        val ref = com.example.edutrack.premiumCacheRef(userId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cache = snapshot.getValue(PremiumCache::class.java)
                    ?: PremiumCache(isPremium = false)
                listener.onPremiumCacheUpdated(cache)
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onPremiumCacheError(error.message)
            }
        })
    }

    /**
     * Obtener premiumCache una sola vez
     */
    suspend fun getPremiumCache(): PremiumCache = coroutineScope {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return@coroutineScope PremiumCache(isPremium = false)
        }

        val ref = com.example.edutrack.premiumCacheRef(userId)

        return@coroutineScope try {
            val snapshot = ref.get().await()
            snapshot.getValue(PremiumCache::class.java) ?: PremiumCache(isPremium = false)
        } catch (e: Exception) {
            PremiumCache(isPremium = false)
        }
    }

    /**
     * IMPORTANTE: El cliente NUNCA debe escribir premiumCache.
     * Las reglas de Firebase lo rechazarán:
     *
     *  "premiumCache": {
     *    ".read": "auth != null && auth.uid == $userId",
     *    ".write": false
     *  }
     *
     * Si intentas escribir, recibirás: "Permission denied"
     * Esto es por diseño para evitar fraude.
     */

    companion object {
        @Volatile
        private var instance: PremiumRepository? = null

        fun getInstance(): PremiumRepository =
            instance ?: synchronized(this) {
                instance ?: PremiumRepository().also { instance = it }
            }
    }
}
