package com.example.edutrack

import android.content.Context
import android.util.Log
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.util.UUID

private const val ROOT_NODE = "Edutrack"
const val DB_URL = "https://edutrack-5579f-default-rtdb.europe-west1.firebasedatabase.app/"

private fun db() = com.google.firebase.database.FirebaseDatabase.getInstance(DB_URL).reference

// Rutas centralizadas para toda la base de datos.
fun userRef(userId: String) = db().child(ROOT_NODE).child("users").child(userId)
fun profileRef(userId: String) = userRef(userId).child("profile")
fun premiumCacheRef(userId: String) = userRef(userId).child("premiumCache")
fun aniosRef(userId: String) = userRef(userId).child("anios")
fun anioRef(userId: String, anioId: String) = aniosRef(userId).child(anioId)
fun asignaturasRef(userId: String, anioId: String) = anioRef(userId, anioId).child("asignaturas")
fun asignaturaRef(userId: String, anioId: String, asignaturaId: String) = asignaturasRef(userId, anioId).child(asignaturaId)
fun notasRef(userId: String, anioId: String, asignaturaId: String) = asignaturaRef(userId, anioId, asignaturaId).child("notas")

// Rutas de grupos (nivel raíz bajo ROOT_NODE, no bajo users/).
fun groupsRef() = db().child(ROOT_NODE).child("groups")
fun groupRef(groupId: String) = groupsRef().child(groupId)
fun groupMembersRef(groupId: String) = db().child(ROOT_NODE).child("groupMembers").child(groupId)
fun groupMemberRef(groupId: String, uid: String) = groupMembersRef(groupId).child(uid)
fun userGroupsRef(uid: String) = db().child(ROOT_NODE).child("userGroups").child(uid)
fun groupSharedSubjectsRef(groupId: String) = db().child(ROOT_NODE).child("groupSharedSubjects").child(groupId)

fun CrearUsuario(usuario: Usuario) {
    val userId = usuario.id?.takeIf { it.isNotBlank() } ?: return
    Log.d("DB", "Creando perfil usuario $userId")
    profileRef(userId).setValue(usuario)
}

fun CrearAnio(userId: String, anio: Anio) {
    val anioId = anio.id?.takeIf { it.isNotBlank() } ?: aniosRef(userId).push().key ?: return
    anio.id = anioId
    anioRef(userId, anioId).setValue(anio)
}

fun CrearAsignatura(userId: String, anioId: String, asignatura: Asignatura) {
    val creditos = asignatura.creditos ?: 0
    if (creditos <= 0) {
        Log.e("DB", "Creditos invalidos para ${asignatura.nombre}")
        return
    }
    val asignaturaId = UUID.randomUUID().toString()
    asignatura.id = asignaturaId
    asignaturaRef(userId, anioId, asignaturaId).setValue(asignatura)
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
        .addOnFailureListener {
            Log.e("DB", "Error borrando asignatura $asignaturaId", it)
            onResult(false)
        }
}

fun EditarUsuario(userId: String, updates: Map<String, Any>, onResult: (Boolean) -> Unit) {
    if (userId.isEmpty()) { onResult(false); return }
    profileRef(userId).updateChildren(updates)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener {
            Log.e("DB", "Error editando usuario $userId", it)
            onResult(false)
        }
}

fun borrarUsuarioCompleto(context: Context, userId: String, onFinish: () -> Unit) {
    if (userId.isEmpty()) { onFinish(); return }
    val authUser = Firebase.auth.currentUser

    // Borra todos los datos del usuario de una sola vez eliminando el nodo raiz.
    userRef(userId).removeValue().addOnCompleteListener { task ->
        if (task.isSuccessful) Log.d("DB", "Datos de usuario $userId eliminados.")
        else Log.e("DB", "Error borrando datos de usuario.", task.exception)

        authUser?.delete()?.addOnCompleteListener { authTask ->
            if (!authTask.isSuccessful) Log.e("DB", "Error borrando Auth.", authTask.exception)
            onFinish()
        } ?: onFinish()
    }
}

// Genera un código de invitación alfanumérico de 6 caracteres.
fun generarCodigoInvitacion(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}

fun crearGrupo(
    ownerUid: String,
    name: String,
    description: String,
    isPrivate: Boolean,
    ownerDisplayName: String,
    ownerPhotoUrl: String? = null,
    onResult: (groupId: String?) -> Unit
) {
    val groupId = groupsRef().push().key ?: run { onResult(null); return }
    val inviteCode = generarCodigoInvitacion()
    val now = System.currentTimeMillis()

    val group = mapOf(
        "id" to groupId,
        "name" to name,
        "description" to description,
        "ownerUid" to ownerUid,
        "createdAt" to now,
        "inviteCode" to inviteCode,
        "isPrivate" to isPrivate,
        "memberCount" to 1
    )
    val member = mapOf(
        "uid" to ownerUid,
        "role" to GroupRole.OWNER.name,
        "joinedAt" to now,
        "displayName" to ownerDisplayName,
        "photoUrl" to ownerPhotoUrl
    )
    val userGroup = mapOf(
        "groupId" to groupId,
        "name" to name,
        "role" to GroupRole.OWNER.name,
        "joinedAt" to now
    )

    val updates = mapOf(
        "/${ROOT_NODE}/groups/$groupId" to group,
        "/${ROOT_NODE}/groupMembers/$groupId/$ownerUid" to member,
        "/${ROOT_NODE}/userGroups/$ownerUid/$groupId" to userGroup
    )

    db().updateChildren(updates)
        .addOnSuccessListener { onResult(groupId) }
        .addOnFailureListener { Log.e("DB", "Error creando grupo", it); onResult(null) }
}

fun unirseAGrupoPorCodigo(
    uid: String,
    inviteCode: String,
    displayName: String,
    photoUrl: String? = null,
    onResult: (success: Boolean, errorMsg: String) -> Unit
) {
    groupsRef().orderByChild("inviteCode").equalTo(inviteCode).limitToFirst(1)
        .get()
        .addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                onResult(false, "Código de invitación no válido")
                return@addOnSuccessListener
            }
            val groupSnap = snapshot.children.first()
            val groupId = groupSnap.key ?: run { onResult(false, "Error interno"); return@addOnSuccessListener }
            val groupName = groupSnap.child("name").getValue(String::class.java) ?: ""

            groupMemberRef(groupId, uid).get().addOnSuccessListener { memberSnap ->
                if (memberSnap.exists()) {
                    onResult(false, "Ya eres miembro de este grupo")
                    return@addOnSuccessListener
                }
                val now = System.currentTimeMillis()
                val member = mapOf(
                    "uid" to uid,
                    "role" to GroupRole.MEMBER.name,
                    "joinedAt" to now,
                    "displayName" to displayName,
                    "photoUrl" to photoUrl
                )
                val userGroup = mapOf(
                    "groupId" to groupId,
                    "name" to groupName,
                    "role" to GroupRole.MEMBER.name,
                    "joinedAt" to now
                )
                val updates = mapOf(
                    "/${ROOT_NODE}/groupMembers/$groupId/$uid" to member,
                    "/${ROOT_NODE}/userGroups/$uid/$groupId" to userGroup,
                    "/${ROOT_NODE}/groups/$groupId/memberCount" to ServerValue.increment(1)
                )
                db().updateChildren(updates)
                    .addOnSuccessListener { onResult(true, groupId) }
                    .addOnFailureListener { onResult(false, "Error al unirse al grupo") }
            }.addOnFailureListener { e -> onResult(false, "Error verificando miembro: ${e.message?.take(60)}") }
        }
        .addOnFailureListener { e -> onResult(false, "Error buscando grupo: ${e.message?.take(60)}") }
}

fun salirDeGrupo(uid: String, groupId: String, onResult: (Boolean) -> Unit) {
    val updates = mapOf(
        "/${ROOT_NODE}/groupMembers/$groupId/$uid" to null,
        "/${ROOT_NODE}/userGroups/$uid/$groupId" to null,
        "/${ROOT_NODE}/groups/$groupId/memberCount" to ServerValue.increment(-1)
    )
    db().updateChildren(updates)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { Log.e("DB", "Error saliendo de grupo", it); onResult(false) }
}

fun compartirAsignaturaConGrupo(
    groupId: String,
    subject: GroupSharedSubject,
    onResult: (Boolean) -> Unit
) {
    val subjectId = subject.id ?: UUID.randomUUID().toString()
    groupSharedSubjectsRef(groupId).child(subjectId).setValue(subject.copy(id = subjectId))
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { Log.e("DB", "Error compartiendo asignatura", it); onResult(false) }
}
