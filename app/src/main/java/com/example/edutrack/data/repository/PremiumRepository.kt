package com.example.edutrack.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repository para gestionar el sistema Premium en Firebase RTDB.
 * Proporciona operaciones para verificar estado premium y fechas de expiración.
 */
class PremiumRepository(private val database: FirebaseDatabase) {

    companion object {
        private const val TAG = "PremiumRepository"
        private const val ROOT_NODE = "Edutrack"
    }

    private fun premiumCacheRef(userId: String) =
        database.reference.child(ROOT_NODE).child("users").child(userId).child("premiumCache")

    private fun profileRef(userId: String) =
        database.reference.child(ROOT_NODE).child("users").child(userId).child("profile")

    /**
     * Actualiza el estado premium del usuario con fecha de expiración.
     *
     * @param userId ID del usuario
     * @param expiresAt Timestamp de expiración (en milisegundos)
     * @return Result<Unit> con el resultado de la operación
     */
    fun updatePremiumStatus(userId: String, expiresAt: Long): Result<Unit> {
        return try {
            if (userId.isEmpty()) {
                return Result.failure(IllegalArgumentException("ID de usuario vacío"))
            }

            if (expiresAt <= 0) {
                return Result.failure(IllegalArgumentException("Fecha de expiración inválida"))
            }

            val now = System.currentTimeMillis()
            val isPremium = expiresAt > now

            val cacheData = mapOf(
                "expiresAt" to expiresAt,
                "isPremium" to isPremium,
                "lastUpdated" to now
            )

            Log.d(TAG, "Actualizando estado premium para usuario $userId, expira: $expiresAt")
            premiumCacheRef(userId).setValue(cacheData)
                .addOnSuccessListener {
                    Log.d(TAG, "Estado premium actualizado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error actualizando estado premium", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar estado premium", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica si el usuario tiene suscripción premium activa.
     *
     * @param userId ID del usuario
     * @return Flow<Boolean> que indica si el usuario es premium
     */
    fun isPremiumActive(userId: String): Flow<Boolean> = callbackFlow {
        val ref = premiumCacheRef(userId).child("isPremium")
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val isPremium = snapshot.getValue(Boolean::class.java) ?: false
                        trySend(isPremium)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando estado premium", e)
                        trySend(false)
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo estado premium: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene la fecha de expiración de la suscripción premium.
     *
     * @param userId ID del usuario
     * @return Flow<Long?> con el timestamp de expiración o null si no es premium
     */
    fun getPremiumExpiry(userId: String): Flow<Long?> = callbackFlow {
        val ref = premiumCacheRef(userId).child("expiresAt")
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val expiresAt = snapshot.getValue(Long::class.java)
                        trySend(expiresAt)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando fecha de expiración", e)
                        trySend(null)
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo fecha de expiración: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene los días restantes de suscripción premium.
     *
     * @param userId ID del usuario
     * @return Flow<Int> con los días restantes (0 si no es premium)
     */
    fun getDaysRemaining(userId: String): Flow<Int> = callbackFlow {
        val ref = premiumCacheRef(userId)
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java) ?: 0L
                        val now = System.currentTimeMillis()

                        val daysRemaining = if (expiresAt > now) {
                            ((expiresAt - now) / (1000 * 60 * 60 * 24)).toInt()
                        } else {
                            0
                        }

                        trySend(daysRemaining)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error calculando días restantes", e)
                        trySend(0)
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo datos premium: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Revoca la suscripción premium del usuario.
     *
     * @param userId ID del usuario
     * @return Result<Unit>
     */
    fun revokePremium(userId: String): Result<Unit> {
        return try {
            if (userId.isEmpty()) {
                return Result.failure(IllegalArgumentException("ID de usuario vacío"))
            }

            val now = System.currentTimeMillis()
            val cacheData = mapOf(
                "expiresAt" to now,
                "isPremium" to false,
                "lastUpdated" to now
            )

            Log.d(TAG, "Revocando suscripción premium para usuario $userId")
            premiumCacheRef(userId).setValue(cacheData)
                .addOnSuccessListener {
                    Log.d(TAG, "Suscripción premium revocada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error revocando suscripción premium", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al revocar suscripción premium", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene información completa del estado premium.
     *
     * @param userId ID del usuario
     * @return Flow<PremiumStatus> con los datos de suscripción
     */
    fun getPremiumStatus(userId: String): Flow<PremiumStatus> = callbackFlow {
        val ref = premiumCacheRef(userId)
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java) ?: 0L
                        val isPremium = snapshot.child("isPremium").getValue(Boolean::class.java) ?: false
                        val lastUpdated = snapshot.child("lastUpdated").getValue(Long::class.java) ?: 0L

                        val now = System.currentTimeMillis()
                        val daysRemaining = if (expiresAt > now) {
                            ((expiresAt - now) / (1000 * 60 * 60 * 24)).toInt()
                        } else {
                            0
                        }

                        val status = PremiumStatus(
                            isPremium = isPremium,
                            expiresAt = expiresAt,
                            daysRemaining = daysRemaining,
                            lastUpdated = lastUpdated
                        )

                        trySend(status)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando estado premium completo", e)
                        trySend(PremiumStatus())
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo estado premium completo: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Data class para representar el estado premium completo.
     */
    data class PremiumStatus(
        val isPremium: Boolean = false,
        val expiresAt: Long = 0L,
        val daysRemaining: Int = 0,
        val lastUpdated: Long = 0L
    )
}
