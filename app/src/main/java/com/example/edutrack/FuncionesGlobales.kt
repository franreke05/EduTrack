package com.example.edutrack

import android.content.Context
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

fun CrearUsuario(usuario: Usuario) {
    val userId = usuario.id?.takeIf { it.isNotBlank() } ?: return
    profileRef(userId).setValue(usuario)
}

fun CrearAnio(userId: String, anio: Anio) {
    val anioId = anio.id?.takeIf { it.isNotBlank() } ?: aniosRef(userId).push().key ?: return
    anio.id = anioId
    anioRef(userId, anioId).setValue(anio)
}

fun CrearAsignatura(userId: String, anioId: String, asignatura: Asignatura) {
    val creditos = asignatura.creditos ?: 0
    if (creditos <= 0) return
    asignatura.id = UUID.randomUUID().toString()
    asignaturaRef(userId, anioId, asignatura.id!!).setValue(asignatura)
}

fun AgregarNota(userId: String, anioId: String, asignaturaId: String, nota: Notas) {
    val notaId = nota.id?.takeIf { it.isNotBlank() } ?: return
    notasRef(userId, anioId, asignaturaId).child(notaId).setValue(nota)
}

fun borrarAsignaturaCompleta(
    userId: String,
    anioId: String,
    asignaturaId: String?,
    onResult: (Boolean) -> Unit = {}
) {
    if (asignaturaId.isNullOrBlank()) { onResult(false); return }
    asignaturaRef(userId, anioId, asignaturaId).removeValue()
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}

fun editarAnio(
    userId: String,
    anioId: String,
    nombre: String,
    descripcion: String,
    maxAsignaturas: Int,
    tipoPeriodo: String,
    onResult: (Boolean) -> Unit = {}
) {
    if (anioId.isBlank()) { onResult(false); return }
    anioRef(userId, anioId).updateChildren(mapOf(
        "nombre" to nombre,
        "descripcion" to descripcion,
        "numero_asignaturas" to maxAsignaturas,
        "tipo_periodo" to tipoPeriodo
    ))
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}

fun borrarAnioCompleto(userId: String, anioId: String, onResult: (Boolean) -> Unit = {}) {
    if (anioId.isBlank()) { onResult(false); return }
    anioRef(userId, anioId).removeValue()
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}

fun EditarUsuario(userId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
    if (userId.isEmpty()) { onResult(false); return }
    profileRef(userId).updateChildren(updates)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}

fun borrarUsuarioCompleto(context: Context, userId: String, onFinish: () -> Unit) {
    if (userId.isEmpty()) { onFinish(); return }
    val authUser = Firebase.auth.currentUser
    userRef(userId).removeValue().addOnCompleteListener {
        authUser?.delete()?.addOnCompleteListener { onFinish() } ?: onFinish()
    }
}

fun marcarExamenComoNotificado(
    userId: String,
    anioId: String,
    asignaturaId: String,
    examenId: String,
    onComplete: (success: Boolean) -> Unit
) {
    if (userId.isBlank() || anioId.isBlank() || asignaturaId.isBlank() || examenId.isBlank()) {
        onComplete(false); return
    }
    examenesRef(userId, anioId, asignaturaId).child(examenId)
        .updateChildren(mapOf("notificado" to true, "notificadoEn" to System.currentTimeMillis()))
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}

fun borrarExamenConfirmado(
    userId: String,
    anioId: String,
    asignaturaId: String,
    examenId: String,
    onComplete: (success: Boolean) -> Unit
) {
    if (userId.isBlank() || anioId.isBlank() || asignaturaId.isBlank() || examenId.isBlank()) {
        onComplete(false); return
    }
    examenesRef(userId, anioId, asignaturaId).child(examenId).removeValue()
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}

fun parseExamDateWithValidation(dateStr: String?, logTag: String = "DateParser"): Calendar? {
    if (dateStr.isNullOrBlank()) return null
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
    return try {
        val parsed = fmt.parse(dateStr) ?: return null
        Calendar.getInstance().apply { time = parsed }.takeIf {
            it.get(Calendar.YEAR) in 2000..2100
        }
    } catch (_: Exception) { null }
}

fun isPremiumValid(expiresAt: Long?): Boolean {
    if (expiresAt == null || expiresAt <= 0) return false
    return expiresAt > System.currentTimeMillis()
}

