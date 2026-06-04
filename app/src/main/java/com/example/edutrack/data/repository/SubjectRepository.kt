package com.example.edutrack.data.repository

import android.util.Log
import com.example.edutrack.dataclass.Asignatura
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Repository para gestionar asignaturas en Firebase RTDB.
 * Proporciona operaciones CRUD para asignaturas dentro de un año académico.
 */
class SubjectRepository(private val database: FirebaseDatabase) {

    companion object {
        private const val TAG = "SubjectRepository"
        private const val ROOT_NODE = "Edutrack"
    }

    private fun anioRef(userId: String, anioId: String) =
        database.reference.child(ROOT_NODE).child("users").child(userId).child("anios").child(anioId)

    private fun asignaturasRef(userId: String, anioId: String) =
        anioRef(userId, anioId).child("asignaturas")

    private fun asignaturaRef(userId: String, anioId: String, asignaturaId: String) =
        asignaturasRef(userId, anioId).child(asignaturaId)

    /**
     * Crea una nueva asignatura en la base de datos.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignatura Objeto Asignatura a crear
     * @return Result<String> con el ID de la asignatura creada
     */
    fun createSubject(userId: String, anioId: String, asignatura: Asignatura): Result<String> {
        return try {
            if (userId.isEmpty() || anioId.isEmpty()) {
                return Result.failure(IllegalArgumentException("IDs de usuario o año vacíos"))
            }

            val creditos = asignatura.creditos ?: 0
            if (creditos <= 0) {
                Log.e(TAG, "Créditos inválidos para ${asignatura.nombre}")
                return Result.failure(IllegalArgumentException("Créditos inválidos"))
            }

            val asignaturaId = UUID.randomUUID().toString()
            val newAsignatura = asignatura.copy(id = asignaturaId)

            Log.d(TAG, "Creando asignatura $asignaturaId en año $anioId")
            asignaturaRef(userId, anioId, asignaturaId).setValue(newAsignatura)
                .addOnSuccessListener {
                    Log.d(TAG, "Asignatura creada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error creando asignatura", e)
                }

            Result.success(asignaturaId)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al crear asignatura", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza una asignatura existente.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param subjectId ID de la asignatura a actualizar
     * @param asignatura Objeto Asignatura con los datos actualizados
     * @return Result<Unit>
     */
    fun updateSubject(
        userId: String,
        anioId: String,
        subjectId: String,
        asignatura: Asignatura
    ): Result<Unit> {
        return try {
            if (userId.isEmpty() || anioId.isEmpty() || subjectId.isEmpty()) {
                return Result.failure(IllegalArgumentException("IDs vacíos"))
            }

            val updates = mapOf(
                "nombre" to asignatura.nombre,
                "creditos" to asignatura.creditos,
                "descripcion" to asignatura.descripcion,
                "tipo_periodo" to asignatura.tipo_periodo,
                "numero_periodos" to asignatura.numero_periodos,
                "fechaExamen" to asignatura.fechaExamen,
                "horaExamen" to asignatura.horaExamen
            )

            Log.d(TAG, "Actualizando asignatura $subjectId")
            asignaturaRef(userId, anioId, subjectId).updateChildren(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Asignatura actualizada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error actualizando asignatura", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar asignatura", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina una asignatura completamente.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param subjectId ID de la asignatura a eliminar
     * @return Result<Unit>
     */
    fun deleteSubject(userId: String, anioId: String, subjectId: String): Result<Unit> {
        return try {
            if (userId.isEmpty() || anioId.isEmpty() || subjectId.isEmpty()) {
                return Result.failure(IllegalArgumentException("IDs vacíos"))
            }

            Log.d(TAG, "Eliminando asignatura $subjectId")
            asignaturaRef(userId, anioId, subjectId).removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "Asignatura eliminada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error eliminando asignatura", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al eliminar asignatura", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene una asignatura específica como un Flow reactivo.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param subjectId ID de la asignatura
     * @return Flow<Asignatura?> que emite cambios en la asignatura
     */
    fun getSubject(userId: String, anioId: String, subjectId: String): Flow<Asignatura?> =
        callbackFlow {
            val ref = asignaturaRef(userId, anioId, subjectId)
            val listener = ref.addValueEventListener(
                object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        try {
                            val asignatura = snapshot.getValue(Asignatura::class.java)
                            trySend(asignatura)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deserializando asignatura", e)
                            trySend(null)
                        }
                    }

                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        Log.e(TAG, "Error leyendo asignatura: ${error.message}")
                        close(error.toException())
                    }
                }
            )

            awaitClose {
                ref.removeEventListener(listener)
            }
        }

    /**
     * Obtiene todas las asignaturas de un año como un Flow reactivo.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @return Flow<List<Asignatura>> que emite la lista de asignaturas
     */
    fun getSubjectsForYear(userId: String, anioId: String): Flow<List<Asignatura>> =
        callbackFlow {
            val ref = asignaturasRef(userId, anioId)
            val listener = ref.addValueEventListener(
                object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        try {
                            val asignaturas = mutableListOf<Asignatura>()
                            for (child in snapshot.children) {
                                val asignatura = child.getValue(Asignatura::class.java)
                                if (asignatura != null) {
                                    asignaturas.add(asignatura)
                                }
                            }
                            trySend(asignaturas)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deserializando asignaturas", e)
                            trySend(emptyList())
                        }
                    }

                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                        Log.e(TAG, "Error leyendo asignaturas: ${error.message}")
                        close(error.toException())
                    }
                }
            )

            awaitClose {
                ref.removeEventListener(listener)
            }
        }

    /**
     * Obtiene la cantidad de asignaturas en un año.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @return Flow<Int> con el número de asignaturas
     */
    fun getSubjectCount(userId: String, anioId: String): Flow<Int> = callbackFlow {
        val ref = asignaturasRef(userId, anioId)
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(snapshot.childrenCount.toInt())
                close()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e(TAG, "Error contando asignaturas: ${error.message}")
                close(error.toException())
            }
        }
        ref.addListenerForSingleValueEvent(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}
