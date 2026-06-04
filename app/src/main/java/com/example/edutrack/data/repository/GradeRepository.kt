package com.example.edutrack.data.repository

import android.util.Log
import com.example.edutrack.dataclass.Notas
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repository para gestionar calificaciones y notas en Firebase RTDB.
 * Proporciona operaciones CRUD para notas dentro de una asignatura.
 */
class GradeRepository(private val database: FirebaseDatabase) {

    companion object {
        private const val TAG = "GradeRepository"
        private const val ROOT_NODE = "Edutrack"
    }

    private fun asignaturaRef(userId: String, anioId: String, asignaturaId: String) =
        database.reference.child(ROOT_NODE).child("users").child(userId)
            .child("anios").child(anioId).child("asignaturas").child(asignaturaId)

    private fun notasRef(userId: String, anioId: String, asignaturaId: String) =
        asignaturaRef(userId, anioId, asignaturaId).child("notas")

    private fun notaRef(userId: String, anioId: String, asignaturaId: String, notaId: String) =
        notasRef(userId, anioId, asignaturaId).child(notaId)

    /**
     * Agrega una nueva nota a una asignatura.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignaturaId ID de la asignatura
     * @param nota Objeto Notas a agregar
     * @return Result<String> con el ID de la nota creada
     */
    fun addGrade(
        userId: String,
        anioId: String,
        asignaturaId: String,
        nota: Notas
    ): Result<String> {
        return try {
            if (userId.isEmpty() || anioId.isEmpty() || asignaturaId.isEmpty()) {
                return Result.failure(IllegalArgumentException("IDs vacíos"))
            }

            val notaId = nota.id?.takeIf { it.isNotBlank() }
                ?: return Result.failure(IllegalArgumentException("Nota sin ID"))

            val notaCompleta = nota.copy(
                id = notaId,
                id_asignatura = asignaturaId,
                creadoEn = System.currentTimeMillis()
            )

            Log.d(TAG, "Agregando nota $notaId a asignatura $asignaturaId")
            notaRef(userId, anioId, asignaturaId, notaId).setValue(notaCompleta)
                .addOnSuccessListener {
                    Log.d(TAG, "Nota agregada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error agregando nota", e)
                }

            Result.success(notaId)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al agregar nota", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza una nota existente.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignaturaId ID de la asignatura
     * @param noteId ID de la nota a actualizar
     * @param nota Objeto Notas con los datos actualizados
     * @return Result<Unit>
     */
    fun updateGrade(
        userId: String,
        anioId: String,
        asignaturaId: String,
        noteId: String,
        nota: Notas
    ): Result<Unit> {
        return try {
            if (userId.isEmpty() || anioId.isEmpty() || asignaturaId.isEmpty() || noteId.isEmpty()) {
                return Result.failure(IllegalArgumentException("IDs vacíos"))
            }

            val updates = mapOf(
                "nombre" to nota.nombre,
                "nota" to nota.nota,
                "porcentaje" to nota.porcentaje,
                "fecha" to nota.fecha,
                "periodo" to nota.periodo
            )

            Log.d(TAG, "Actualizando nota $noteId")
            notaRef(userId, anioId, asignaturaId, noteId).updateChildren(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Nota actualizada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error actualizando nota", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar nota", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina una nota de la base de datos.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignaturaId ID de la asignatura
     * @param noteId ID de la nota a eliminar
     * @return Result<Unit>
     */
    fun deleteGrade(
        userId: String,
        anioId: String,
        asignaturaId: String,
        noteId: String
    ): Result<Unit> {
        return try {
            if (userId.isEmpty() || anioId.isEmpty() || asignaturaId.isEmpty() || noteId.isEmpty()) {
                return Result.failure(IllegalArgumentException("IDs vacíos"))
            }

            Log.d(TAG, "Eliminando nota $noteId")
            notaRef(userId, anioId, asignaturaId, noteId).removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "Nota eliminada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error eliminando nota", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al eliminar nota", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las notas de una asignatura como un Flow reactivo.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignaturaId ID de la asignatura
     * @return Flow<List<Notas>> que emite la lista de notas
     */
    fun getGradesForSubject(
        userId: String,
        anioId: String,
        asignaturaId: String
    ): Flow<List<Notas>> = callbackFlow {
        val ref = notasRef(userId, anioId, asignaturaId)
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val notas = mutableListOf<Notas>()
                        for (child in snapshot.children) {
                            val nota = child.getValue(Notas::class.java)
                            if (nota != null) {
                                notas.add(nota)
                            }
                        }
                        // Ordena por fecha de creación (descendente)
                        notas.sortByDescending { it.creadoEn }
                        trySend(notas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando notas", e)
                        trySend(emptyList())
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo notas: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene una nota específica como un Flow reactivo.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignaturaId ID de la asignatura
     * @param noteId ID de la nota
     * @return Flow<Notas?> que emite los datos de la nota
     */
    fun getGrade(
        userId: String,
        anioId: String,
        asignaturaId: String,
        noteId: String
    ): Flow<Notas?> = callbackFlow {
        val ref = notaRef(userId, anioId, asignaturaId, noteId)
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val nota = snapshot.getValue(Notas::class.java)
                        trySend(nota)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando nota", e)
                        trySend(null)
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo nota: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene las notas de un período específico.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignaturaId ID de la asignatura
     * @param periodo Número del período
     * @return Flow<List<Notas>> con las notas del período
     */
    fun getGradesByPeriod(
        userId: String,
        anioId: String,
        asignaturaId: String,
        periodo: Int
    ): Flow<List<Notas>> = callbackFlow {
        val ref = notasRef(userId, anioId, asignaturaId)
            .orderByChild("periodo")
            .equalTo(periodo.toDouble())

        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val notas = mutableListOf<Notas>()
                        for (child in snapshot.children) {
                            val nota = child.getValue(Notas::class.java)
                            if (nota != null) {
                                notas.add(nota)
                            }
                        }
                        notas.sortByDescending { it.creadoEn }
                        trySend(notas)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando notas del período", e)
                        trySend(emptyList())
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo notas del período: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene el promedio de notas de una asignatura.
     *
     * @param userId ID del usuario propietario
     * @param anioId ID del año académico
     * @param asignaturaId ID de la asignatura
     * @return Flow<Double> con el promedio ponderado
     */
    fun getAverageGrade(
        userId: String,
        anioId: String,
        asignaturaId: String
    ): Flow<Double> = callbackFlow {
        getGradesForSubject(userId, anioId, asignaturaId).collect { notas ->
            val totalPorcentaje = notas.sumOf { it.porcentaje?.toDouble() ?: 0.0 }
            val promedio = if (totalPorcentaje > 0) {
                notas.sumOf { (it.nota?.toDouble() ?: 0.0) * (it.porcentaje?.toDouble() ?: 0.0) / 100 }
            } else {
                notas.map { it.nota?.toDouble() ?: 0.0 }.average()
            }
            trySend(promedio)
        }

        awaitClose()
    }
}
