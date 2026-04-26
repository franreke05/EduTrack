package com.example.edutrack

import android.content.Context
import android.util.Log
import com.example.edutrack.core.FreemiumLimits
import com.example.edutrack.data.firebase.FirebasePaths
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.UserPreferences
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import java.util.UUID

lateinit var db_ref: DatabaseReference

fun CrearUsuario(usuario: Usuario) {
    val uid = Firebase.auth.currentUser?.uid ?: usuario.id.orEmpty()
    if (uid.isBlank()) return
    val now = System.currentTimeMillis()
    usuario.id = uid
    FirebasePaths.root.updateChildren(
        mapOf(
            "users/$uid/profile/id" to uid,
            "users/$uid/profile/nombre" to usuario.nombre.orEmpty(),
            "users/$uid/profile/email" to usuario.email.orEmpty(),
            "users/$uid/profile/photoUrl" to usuario.photoUrl.orEmpty(),
            "users/$uid/profile/updatedAt" to now,
            "users/$uid/settings/preferences" to (usuario.preferences ?: UserPreferences())
        )
    )
}

fun AgregarNota_Asignatura(asignatura: Asignatura, nota: Notas) {
    val uid = asignatura.ownerId ?: asignatura.id_usuario ?: Firebase.auth.currentUser?.uid ?: return
    val yearId = asignatura.yearId ?: asignatura.id_anio ?: return
    val subjectId = asignatura.id ?: asignatura.subjectId ?: return
    guardarNotaSegura(uid, yearId, subjectId, nota) {}
}

fun CrearAnio(anio: Anio) {
    val uid = anio.ownerId ?: anio.id_user ?: Firebase.auth.currentUser?.uid ?: return
    val yearId = anio.id?.takeIf { it.isNotBlank() }
        ?: FirebasePaths.years(uid).push().key
        ?: UUID.randomUUID().toString()
    val now = System.currentTimeMillis()
    anio.id = yearId
    val year = Anio(
        id = yearId,
        nombre = anio.nombre,
        descripcion = anio.descripcion,
        fechaInicio = anio.fechaInicio,
        fechaFin = anio.fechaFin,
        numero_asignaturas = anio.numero_asignaturas,
        lista_asignaturas = emptyMap(),
        id_user = uid,
        ownerId = uid,
        createdAt = anio.createdAt?.takeIf { it > 0L } ?: now,
        updatedAt = now
    )
    FirebasePaths.root.updateChildren(mapOf("users/$uid/years/$yearId" to year))
}

fun CrearAsignatura(asignatura: Asignatura) {
    val uid = asignatura.ownerId ?: asignatura.id_usuario ?: Firebase.auth.currentUser?.uid ?: return
    val yearId = asignatura.yearId ?: asignatura.id_anio ?: return
    val subjectId = asignatura.id?.takeIf { it.isNotBlank() }
        ?: FirebasePaths.subjects(uid, yearId).push().key
        ?: UUID.randomUUID().toString()
    val now = System.currentTimeMillis()
    val periodType = asignatura.tipo_periodo ?: asignatura.periodType ?: "Trimestre"
    val totalPeriods = asignatura.numero_periodos ?: asignatura.totalPeriods ?: 3
    asignatura.id = subjectId
    val subject = Asignatura(
        id = subjectId,
        nombre = asignatura.nombre?.trim(),
        creditos = asignatura.creditos ?: 0,
        descripcion = asignatura.descripcion?.trim(),
        media = 0.0,
        id_usuario = uid,
        id_anio = yearId,
        tipo_periodo = periodType,
        numero_periodos = totalPeriods,
        ownerId = uid,
        yearId = yearId,
        subjectId = subjectId,
        name = asignatura.nombre?.trim(),
        description = asignatura.descripcion?.trim(),
        credits = asignatura.creditos ?: 0,
        periodType = periodType,
        totalPeriods = totalPeriods,
        average = 0.0,
        noteCount = 0,
        usedPercentage = 0.0,
        createdAt = now,
        updatedAt = now
    )
    FirebasePaths.root.updateChildren(
        mapOf(
            "users/$uid/years/$yearId/subjects/$subjectId" to subject,
            "users/$uid/years/$yearId/updatedAt" to now
        )
    )
}

fun borrarAsignaturaCompleta(
    asignaturaId: String?,
    anioId: String?,
    onResult: (Boolean) -> Unit = {}
) {
    val uid = Firebase.auth.currentUser?.uid
    if (uid.isNullOrBlank() || asignaturaId.isNullOrBlank() || anioId.isNullOrBlank()) {
        onResult(false)
        return
    }
    val now = System.currentTimeMillis()
    FirebasePaths.root.updateChildren(
        mapOf<String, Any?>(
            "users/$uid/years/$anioId/subjects/$asignaturaId" to null,
            "users/$uid/years/$anioId/updatedAt" to now
        )
    ).addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.e("FirebaseCleanup", "Error al borrar asignatura $asignaturaId", task.exception)
        }
        onResult(task.isSuccessful)
    }
}

fun EditarUsuario(userId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
    if (userId.isEmpty()) {
        onResult(false)
        return
    }
    val allowedUpdates = updates
        .filterKeys { it in setOf("nombre", "photoUrl") }
        .toMutableMap()
    allowedUpdates["updatedAt"] = System.currentTimeMillis()
    FirebasePaths.profile(userId)
        .updateChildren(allowedUpdates)
        .addOnSuccessListener {
            Log.d("FirebaseEdit", "Usuario $userId actualizado.")
            onResult(true)
        }
        .addOnFailureListener {
            Log.e("FirebaseEdit", "Error al actualizar usuario $userId.", it)
            onResult(false)
        }
}

fun borrarUsuarioCompleto(context: Context, userId: String, onFinish: () -> Unit) {
    if (userId.isEmpty()) {
        onFinish()
        return
    }
    val authUser = Firebase.auth.currentUser
    val updates = mutableMapOf<String, Any?>(
        "users/$userId" to null,
        "userGroups/$userId" to null
    )
    FirebasePaths.userGroups(userId).get().addOnSuccessListener { snapshot ->
        snapshot.children.mapNotNull { it.key }.forEach { groupId ->
            updates["groupMembers/$groupId/$userId"] = null
        }
        FirebasePaths.root.updateChildren(updates).addOnCompleteListener { userDbTask ->
            if (userDbTask.isSuccessful) Log.d("FirebaseCleanup", "Nodo de usuario $userId eliminado de la DB.")
            else Log.e("FirebaseCleanup", "Error al eliminar datos de la DB.", userDbTask.exception)
            authUser?.delete()?.addOnCompleteListener {
                onFinish()
            } ?: onFinish()
        }
    }.addOnFailureListener {
        Log.e("FirebaseCleanup", "Error leyendo grupos de usuario.", it)
        onFinish()
    }
}

fun guardarNotaSegura(
    uid: String,
    yearId: String,
    subjectId: String,
    nota: Notas,
    onFinish: (Boolean) -> Unit
) {
    val noteId = nota.id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
    val grade = nota.grade ?: nota.nota ?: -1.0
    val percentage = nota.percentage ?: nota.porcentaje ?: -1.0
    if (grade < 0.0 || grade > 10.0 || percentage <= 0.0 || percentage > 100.0) {
        onFinish(false)
        return
    }
    FirebasePaths.notes(uid, yearId, subjectId).get().addOnSuccessListener { snapshot ->
        val existing = snapshot.children.mapNotNull { it.getValue(Notas::class.java) }
        val usedWithoutCurrent = existing
            .filter { it.id != noteId }
            .sumOf { it.percentage ?: it.porcentaje ?: 0.0 }
        if (usedWithoutCurrent + percentage > FreemiumLimits.MAX_TOTAL_GRADE_PERCENT + 1e-6) {
            onFinish(false)
            return@addOnSuccessListener
        }
        val now = System.currentTimeMillis()
        val period = nota.period ?: nota.periodo ?: 1
        val finalNote = Notas(
            id = noteId,
            id_asignatura = subjectId,
            nombre = nota.nombre?.trim(),
            nota = grade,
            porcentaje = percentage,
            fecha = nota.fecha,
            periodo = period,
            ownerId = uid,
            yearId = yearId,
            subjectId = subjectId,
            name = nota.nombre?.trim(),
            grade = grade,
            percentage = percentage,
            period = period,
            createdAt = nota.createdAt?.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        val updatedNotes = existing.filter { it.id != noteId } + finalNote
        val average = calcularPromedioNotas(updatedNotes)
        val usedPercentage = updatedNotes.sumOf { it.percentage ?: it.porcentaje ?: 0.0 }
        FirebasePaths.root.updateChildren(
            mapOf(
                "users/$uid/years/$yearId/subjects/$subjectId/notes/$noteId" to finalNote,
                "users/$uid/years/$yearId/subjects/$subjectId/media" to average,
                "users/$uid/years/$yearId/subjects/$subjectId/average" to average,
                "users/$uid/years/$yearId/subjects/$subjectId/noteCount" to updatedNotes.size,
                "users/$uid/years/$yearId/subjects/$subjectId/usedPercentage" to usedPercentage,
                "users/$uid/years/$yearId/subjects/$subjectId/updatedAt" to now,
                "users/$uid/years/$yearId/updatedAt" to now
            )
        ).addOnCompleteListener { onFinish(it.isSuccessful) }
    }.addOnFailureListener {
        onFinish(false)
    }
}

fun eliminarNotaSegura(
    uid: String,
    yearId: String,
    subjectId: String,
    noteId: String,
    onFinish: (Boolean) -> Unit = {}
) {
    FirebasePaths.notes(uid, yearId, subjectId).get().addOnSuccessListener { snapshot ->
        val remaining = snapshot.children.mapNotNull { it.getValue(Notas::class.java) }
            .filter { it.id != noteId }
        val now = System.currentTimeMillis()
        val average = calcularPromedioNotas(remaining)
        val usedPercentage = remaining.sumOf { it.percentage ?: it.porcentaje ?: 0.0 }
        FirebasePaths.root.updateChildren(
            mapOf<String, Any?>(
                "users/$uid/years/$yearId/subjects/$subjectId/notes/$noteId" to null,
                "users/$uid/years/$yearId/subjects/$subjectId/media" to average,
                "users/$uid/years/$yearId/subjects/$subjectId/average" to average,
                "users/$uid/years/$yearId/subjects/$subjectId/noteCount" to remaining.size,
                "users/$uid/years/$yearId/subjects/$subjectId/usedPercentage" to usedPercentage,
                "users/$uid/years/$yearId/subjects/$subjectId/updatedAt" to now,
                "users/$uid/years/$yearId/updatedAt" to now
            )
        ).addOnCompleteListener { onFinish(it.isSuccessful) }
    }.addOnFailureListener {
        onFinish(false)
    }
}

private fun calcularPromedioNotas(notas: List<Notas>): Double {
    val totalPeso = notas.sumOf { it.percentage ?: it.porcentaje ?: 0.0 }
    if (totalPeso <= 0.0) return 0.0
    return notas.sumOf { (it.grade ?: it.nota ?: 0.0) * (it.percentage ?: it.porcentaje ?: 0.0) } / totalPeso
}
