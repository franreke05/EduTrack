package com.example.edutrack.data.repository

import android.content.Context
import android.util.Log
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.Serializable

/**
 * Repository para gestionar operaciones de usuario en Firebase RTDB.
 * Proporciona acceso a perfiles de usuario, actualizaciones y eliminación de cuentas.
 */
class UserRepository(private val database: FirebaseDatabase) {

    companion object {
        private const val TAG = "UserRepository"
        private const val ROOT_NODE = "Edutrack"
    }

    private fun userRef(userId: String) =
        database.reference.child(ROOT_NODE).child("users").child(userId)

    private fun profileRef(userId: String) = userRef(userId).child("profile")

    private fun premiumCacheRef(userId: String) = userRef(userId).child("premiumCache")

    /**
     * Obtiene el perfil del usuario como un Flow reactivo.
     *
     * @param uid ID del usuario en Firebase Auth
     * @return Flow<Usuario> que emite actualizaciones del perfil
     */
    fun getUserProfile(uid: String): Flow<Usuario?> = callbackFlow {
        val ref = profileRef(uid)
        val listener = ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                try {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    trySend(usuario)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deserializando usuario $uid", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e(TAG, "Error leyendo perfil: ${error.message}")
                close(error.toException())
            }
        })

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Crea o actualiza el perfil del usuario.
     *
     * @param usuario Objeto Usuario con los datos a guardar
     * @return Result<Unit> con el resultado de la operación
     */
    fun createUserProfile(usuario: Usuario): Result<Unit> {
        return try {
            val userId = usuario.id?.takeIf { it.isNotBlank() } ?: return Result.failure(
                IllegalArgumentException("Usuario sin ID válido")
            )

            Log.d(TAG, "Creando perfil usuario $userId")
            profileRef(userId).setValue(usuario)
                .addOnSuccessListener {
                    Log.d(TAG, "Perfil creado exitosamente para $userId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error creando perfil usuario $userId", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al crear perfil", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza campos específicos del perfil del usuario.
     *
     * @param userId ID del usuario
     * @param updates Mapa de campos a actualizar
     * @return Result<Unit> con el resultado de la operación
     */
    fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            if (userId.isEmpty()) {
                return Result.failure(IllegalArgumentException("ID de usuario vacío"))
            }

            Log.d(TAG, "Actualizando perfil usuario $userId")
            profileRef(userId).updateChildren(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Perfil actualizado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error actualizando perfil", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar perfil", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el nombre del usuario.
     *
     * @param userId ID del usuario
     * @param nombre Nuevo nombre
     * @return Result<Unit>
     */
    fun updateUserName(userId: String, nombre: String): Result<Unit> {
        return updateUserProfile(userId, mapOf("nombre" to nombre))
    }

    /**
     * Actualiza la foto de perfil del usuario.
     *
     * @param userId ID del usuario
     * @param photoUrl Nueva URL de foto
     * @return Result<Unit>
     */
    fun updateUserPhoto(userId: String, photoUrl: String?): Result<Unit> {
        return if (photoUrl != null) {
            updateUserProfile(userId, mapOf("photoUrl" to photoUrl))
        } else {
            updateUserProfile(userId, mapOf("photoUrl" to ""))
        }
    }

    /**
     * Elimina completamente la cuenta del usuario y sus datos de Firebase RTDB.
     *
     * @param uid ID del usuario
     * @return Result<Unit> con el resultado de la operación
     */
    fun deleteUserAccount(uid: String): Result<Unit> {
        return try {
            if (uid.isEmpty()) {
                return Result.failure(IllegalArgumentException("ID de usuario vacío"))
            }

            Log.d(TAG, "Iniciando eliminación de cuenta para usuario $uid")

            // Primero elimina todos los datos del usuario
            userRef(uid).removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "Datos de usuario $uid eliminados exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error eliminando datos de usuario", e)
                }

            // Luego elimina la cuenta de autenticación
            Firebase.auth.currentUser?.delete()
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "Error eliminando cuenta de Auth", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al eliminar cuenta", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica si el usuario existe en la base de datos.
     *
     * @param userId ID del usuario
     * @return Flow<Boolean> que indica si el usuario existe
     */
    fun userExists(userId: String): Flow<Boolean> = callbackFlow {
        val ref = profileRef(userId)
        val listener = ref.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(snapshot.exists())
                close()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e(TAG, "Error verificando existencia de usuario: ${error.message}")
                close(error.toException())
            }
        })

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene el email del usuario.
     *
     * @param userId ID del usuario
     * @return Flow<String?> con el email del usuario
     */
    fun getUserEmail(userId: String): Flow<String?> = callbackFlow {
        val ref = profileRef(userId).child("email")
        val listener = ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(snapshot.getValue(String::class.java))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e(TAG, "Error leyendo email: ${error.message}")
                close(error.toException())
            }
        })

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}
