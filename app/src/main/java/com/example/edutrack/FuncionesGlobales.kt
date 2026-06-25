package com.example.edutrack

import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
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

// RGPD art. 17: intenta borrado vía Cloud Function (admin SDK, sin requires-recent-login).
// Si la función no está desplegada o falla, cae en borrado local (client-side).
fun borrarUsuarioCompleto(userId: String, onFinish: (success: Boolean) -> Unit) {
    if (userId.isEmpty()) { onFinish(false); return }
    FirebaseFunctions.getInstance()
        .getHttpsCallable("deleteUserData")
        .call()
        .addOnSuccessListener {
            Firebase.auth.signOut()
            onFinish(true)
        }
        .addOnFailureListener { e ->
            android.util.Log.w("BorrarCuenta", "Cloud Function no disponible (${e.message}), usando borrado local")
            borrarCuentaLocal(userId, onFinish)
        }
}

// Fallback client-side: borrado completo idéntico a la Cloud Function.
// Procesa grupos uno a uno (secuencial) para manejar lógica de ownership sin bloquear el hilo.
// Si el usuario es OWNER: borra el grupo entero + quita a otros miembros de sus userGroups.
// Si es MEMBER: borra solo su contenido + escanea feed/resources/exams/sharedSubjects.
// Si Auth.delete() falla (requires-recent-login), los datos ya están borrados → signOut.
private fun borrarCuentaLocal(userId: String, onFinish: (success: Boolean) -> Unit) {
    userGroupsRef(userId).get()
        .addOnSuccessListener { groupsSnap ->
            val updates = HashMap<String, Any?>()
            updates["users/$userId"] = null
            updates["userGroups/$userId"] = null
            val groupIds = groupsSnap.children.mapNotNull { it.key }.toMutableList()
            procesarGrupo(userId, groupIds, updates, onFinish)
        }
        .addOnFailureListener { onFinish(false) }
}

// Procesa grupos en orden; cuando la lista está vacía, hace commit.
private fun procesarGrupo(
    userId: String,
    remaining: MutableList<String>,
    updates: HashMap<String, Any?>,
    onFinish: (Boolean) -> Unit
) {
    if (remaining.isEmpty()) { commitBorradoLocal(updates, userId, onFinish); return }
    val gid = remaining.removeFirst()

    // Leer metadata del grupo y sus miembros en paralelo
    var ownerUid: String? = null
    var membersSnap: com.google.firebase.database.DataSnapshot? = null
    val firstTwo = java.util.concurrent.atomic.AtomicInteger(2)

    fun afterBothReads() {
        if (firstTwo.decrementAndGet() != 0) return
        if (ownerUid == userId) {
            // OWNER: eliminar TODO el grupo y sacarlo del userGroups de cada miembro
            updates["groups/$gid"] = null
            updates["groupMembers/$gid"] = null
            updates["groupGrades/$gid"] = null
            updates["groupFeed/$gid"] = null
            updates["groupResources/$gid"] = null
            updates["groupExams/$gid"] = null
            updates["groupSharedSubjects/$gid"] = null
            membersSnap?.children?.mapNotNull { it.key }?.forEach { memberUid ->
                if (memberUid != userId) updates["userGroups/$memberUid/$gid"] = null
            }
            procesarGrupo(userId, remaining, updates, onFinish)
        } else {
            // MEMBER: eliminar solo su contenido en el grupo
            updates["groupMembers/$gid/$userId"] = null
            updates["groupGrades/$gid/$userId"] = null
            val pending = java.util.concurrent.atomic.AtomicInteger(4)
            fun done() { if (pending.decrementAndGet() == 0) procesarGrupo(userId, remaining, updates, onFinish) }
            groupFeedRef(gid).get().addOnCompleteListener { t ->
                t.result?.children?.forEach { c ->
                    if (c.child("actorUid").value == userId) updates["groupFeed/$gid/${c.key}"] = null
                }; done()
            }
            groupResourcesRef(gid).get().addOnCompleteListener { t ->
                t.result?.children?.forEach { c ->
                    if (c.child("authorId").value == userId) updates["groupResources/$gid/${c.key}"] = null
                }; done()
            }
            groupExamsRef(gid).get().addOnCompleteListener { t ->
                t.result?.children?.forEach { c ->
                    if (c.child("authorId").value == userId) updates["groupExams/$gid/${c.key}"] = null
                }; done()
            }
            groupSharedSubjectsRef(gid).get().addOnCompleteListener { t ->
                t.result?.children?.forEach { c ->
                    if (c.child("sharedBy").value == userId) updates["groupSharedSubjects/$gid/${c.key}"] = null
                }; done()
            }
        }
    }

    groupRef(gid).get().addOnCompleteListener { t ->
        ownerUid = t.result?.child("ownerUid")?.value as? String
        afterBothReads()
    }
    groupMembersRef(gid).get().addOnCompleteListener { t ->
        membersSnap = t.result
        afterBothReads()
    }
}

private fun commitBorradoLocal(updates: HashMap<String, Any?>, userId: String, onFinish: (Boolean) -> Unit) {
    // Avatar (fire-and-forget; puede no existir)
    com.google.firebase.storage.FirebaseStorage.getInstance()
        .reference.child("avatars/$userId.jpg").delete()

    val paths = updates.keys.toList()
    if (paths.isEmpty()) { borrarAuthYSalir(onFinish); return }

    // removeValue() por nodo independiente: si Firebase Rules bloquea algún nodo de grupo,
    // los demás (datos propios del usuario) se borran igualmente.
    val pending = java.util.concurrent.atomic.AtomicInteger(paths.size)
    val root = db().child(ROOT_NODE)
    for (path in paths) {
        root.child(path).removeValue()
            .addOnCompleteListener {
                if (pending.decrementAndGet() == 0) borrarAuthYSalir(onFinish)
            }
    }
}

private fun borrarAuthYSalir(onFinish: (Boolean) -> Unit) {
    Firebase.auth.currentUser?.delete()
        ?.addOnCompleteListener { Firebase.auth.signOut(); onFinish(true) }
        ?: run { Firebase.auth.signOut(); onFinish(true) }
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

