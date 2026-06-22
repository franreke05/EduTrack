package com.example.edutrack

import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.GroupFeedEventType
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.dataclass.SubjectImport
import com.google.firebase.database.ServerValue
import java.util.UUID

// ponytail: 6-char code with unambiguous charset — collision probability negligible at current scale
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

    val updates = mapOf(
        "/${ROOT_NODE}/groups/$groupId" to mapOf(
            "id" to groupId, "name" to name, "description" to description,
            "ownerUid" to ownerUid, "createdAt" to now, "inviteCode" to inviteCode,
            "isPrivate" to isPrivate, "memberCount" to 1
        ),
        "/${ROOT_NODE}/groupMembers/$groupId/$ownerUid" to mapOf(
            "uid" to ownerUid, "role" to GroupRole.OWNER.name, "joinedAt" to now,
            "displayName" to ownerDisplayName, "photoUrl" to ownerPhotoUrl
        ),
        "/${ROOT_NODE}/userGroups/$ownerUid/$groupId" to mapOf(
            "groupId" to groupId, "name" to name,
            "role" to GroupRole.OWNER.name, "joinedAt" to now
        )
    )

    db().updateChildren(updates)
        .addOnSuccessListener { onResult(groupId) }
        .addOnFailureListener { onResult(null) }
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
            if (!snapshot.exists()) { onResult(false, "Código de invitación no válido"); return@addOnSuccessListener }
            val groupSnap = snapshot.children.first()
            val groupId = groupSnap.key ?: run { onResult(false, "Error interno"); return@addOnSuccessListener }
            val groupName = groupSnap.child("name").getValue(String::class.java) ?: ""

            groupMemberRef(groupId, uid).get()
                .addOnSuccessListener { memberSnap ->
                    if (memberSnap.exists()) { onResult(false, "Ya eres miembro de este grupo"); return@addOnSuccessListener }
                    val now = System.currentTimeMillis()
                    val updates = mapOf(
                        "/${ROOT_NODE}/groupMembers/$groupId/$uid" to mapOf(
                            "uid" to uid, "role" to GroupRole.MEMBER.name, "joinedAt" to now,
                            "displayName" to displayName, "photoUrl" to photoUrl
                        ),
                        "/${ROOT_NODE}/userGroups/$uid/$groupId" to mapOf(
                            "groupId" to groupId, "name" to groupName,
                            "role" to GroupRole.MEMBER.name, "joinedAt" to now
                        ),
                        "/${ROOT_NODE}/groups/$groupId/memberCount" to ServerValue.increment(1)
                    )
                    db().updateChildren(updates)
                        .addOnSuccessListener {
                            logFeedEvent(groupId, GroupFeedEventType.JOIN, uid, displayName, photoUrl)
                            onResult(true, groupId)
                        }
                        .addOnFailureListener { onResult(false, "Error al unirse al grupo") }
                }
                .addOnFailureListener { e -> onResult(false, "Error verificando miembro: ${e.message?.take(60)}") }
        }
        .addOnFailureListener { e -> onResult(false, "Error buscando grupo: ${e.message?.take(60)}") }
}

fun salirDeGrupo(
    uid: String,
    groupId: String,
    actorName: String? = null,
    actorPhotoUrl: String? = null,
    onResult: (Boolean) -> Unit
) {
    // Feed event ANTES del remove — reglas exigen que el actor sea miembro activo
    logFeedEvent(groupId, GroupFeedEventType.LEAVE, uid, actorName, actorPhotoUrl)
    val updates = mapOf(
        "/${ROOT_NODE}/groupMembers/$groupId/$uid" to null,
        "/${ROOT_NODE}/userGroups/$uid/$groupId" to null,
        "/${ROOT_NODE}/groups/$groupId/memberCount" to ServerValue.increment(-1)
    )
    db().updateChildren(updates)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}

fun salirDeGrupoConCleanup(
    uid: String,
    groupId: String,
    actorName: String? = null,
    actorPhotoUrl: String? = null,
    onResult: (Boolean) -> Unit
) {
    if (uid.isBlank() || groupId.isBlank()) { onResult(false); return }
    logFeedEvent(groupId, GroupFeedEventType.LEAVE, uid, actorName, actorPhotoUrl)
    val updates = mapOf(
        "/${ROOT_NODE}/groupMembers/$groupId/$uid" to null,
        "/${ROOT_NODE}/userGroups/$uid/$groupId" to null,
        "/${ROOT_NODE}/groups/$groupId/memberCount" to ServerValue.increment(-1)
    )
    db().updateChildren(updates)
        .addOnSuccessListener {
            deleteCascadeGroupImports(uid, groupId) { onResult(true) }
        }
        .addOnFailureListener { onResult(false) }
}

private fun deleteCascadeGroupImports(uid: String, groupId: String, onComplete: (Boolean) -> Unit) {
    groupImportsRef(uid).get()
        .addOnSuccessListener { snapshot ->
            val batchUpdates = mutableMapOf<String, Any?>()
            snapshot.children.forEach { child ->
                if (child.child("sourceGroupId").getValue(String::class.java) == groupId) {
                    batchUpdates["/${ROOT_NODE}/users/$uid/groupImports/${child.key}"] = null
                }
            }
            if (batchUpdates.isEmpty()) { onComplete(true); return@addOnSuccessListener }
            db().updateChildren(batchUpdates)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
        .addOnFailureListener { onComplete(false) }
}

fun compartirAsignaturaConGrupo(
    groupId: String,
    subject: GroupSharedSubject,
    onResult: (Boolean) -> Unit
) {
    val subjectId = subject.id ?: UUID.randomUUID().toString()
    val toWrite = subject.copy(id = subjectId)
    groupSharedSubjectsRef(groupId).child(subjectId).setValue(toWrite)
        .addOnSuccessListener {
            val actorUid = toWrite.sharedBy
            if (!actorUid.isNullOrBlank()) {
                logFeedEvent(groupId, GroupFeedEventType.SHARE, actorUid, toWrite.sharedByName,
                    toWrite.sharedByPhotoUrl, subjectId, toWrite.name)
            }
            onResult(true)
        }
        .addOnFailureListener { onResult(false) }
}

fun eliminarAsignaturaCompartida(
    groupId: String,
    subjectId: String,
    actorUid: String? = null,
    actorName: String? = null,
    actorPhotoUrl: String? = null,
    subjectName: String? = null,
    onResult: (Boolean) -> Unit
) {
    groupSharedSubjectsRef(groupId).child(subjectId).removeValue()
        .addOnSuccessListener {
            if (!actorUid.isNullOrBlank()) {
                logFeedEvent(groupId, GroupFeedEventType.UNSHARE, actorUid, actorName,
                    actorPhotoUrl, subjectId, subjectName)
            }
            onResult(true)
        }
        .addOnFailureListener { onResult(false) }
}

fun logFeedEvent(
    groupId: String,
    type: GroupFeedEventType,
    actorUid: String,
    actorName: String?,
    actorPhotoUrl: String?,
    targetId: String? = null,
    targetLabel: String? = null
) {
    val ref = groupFeedRef(groupId).push()
    val eventId = ref.key ?: return
    ref.setValue(mapOf(
        "id" to eventId, "type" to type.name, "actorUid" to actorUid,
        "actorName" to actorName, "actorPhotoUrl" to actorPhotoUrl,
        "targetId" to targetId, "targetLabel" to targetLabel,
        "createdAt" to ServerValue.TIMESTAMP
    ))
}

fun importarAsignaturaDesdeGrupo(
    uid: String,
    targetAnioId: String,
    sourceGroupId: String,
    shared: GroupSharedSubject,
    creditos: Int,
    actorName: String?,
    actorPhotoUrl: String?,
    onResult: (success: Boolean, errorMsg: String?) -> Unit
) {
    val sourceSubjectId = shared.id
    if (sourceSubjectId.isNullOrBlank()) { onResult(false, "Asignatura origen sin identificador"); return }
    if (creditos < 1) { onResult(false, "Créditos inválidos"); return }

    groupImportsRef(uid).orderByChild("sourceSubjectId").equalTo(sourceSubjectId).limitToFirst(1)
        .get()
        .addOnSuccessListener { dupSnap ->
            if (dupSnap.exists()) { onResult(false, "Ya importaste esta asignatura"); return@addOnSuccessListener }
            val asignaturaId = UUID.randomUUID().toString()
            val asignatura = Asignatura(
                id = asignaturaId, nombre = shared.name, creditos = creditos,
                descripcion = "", media = 0.0, numero_notas = 0,
                tipo_periodo = shared.tipoPeriodo ?: "Trimestre",
                numero_periodos = shared.numeroPeriodos ?: 3,
                fechaExamen = null, horaExamen = null
            )
            val importId = groupImportsRef(uid).push().key ?: run {
                onResult(false, "Error generando identificador"); return@addOnSuccessListener
            }
            val importEntry = SubjectImport(
                id = importId, sourceGroupId = sourceGroupId, sourceSubjectId = sourceSubjectId,
                subjectName = shared.name, importedAt = System.currentTimeMillis(),
                targetAnioId = targetAnioId, targetAsignaturaId = asignaturaId
            )
            asignaturaRef(uid, targetAnioId, asignaturaId).setValue(asignatura)
                .addOnSuccessListener {
                    groupImportsRef(uid).child(importId).setValue(importEntry)
                    groupImportsMetaRef(uid, currentMonthKey()).child("count").setValue(ServerValue.increment(1))
                    logFeedEvent(sourceGroupId, GroupFeedEventType.IMPORT, uid, actorName,
                        actorPhotoUrl, sourceSubjectId, shared.name)
                    onResult(true, null)
                }
                .addOnFailureListener { onResult(false, "Error al crear la asignatura") }
        }
        .addOnFailureListener { e -> onResult(false, "Error comprobando duplicados: ${e.message?.take(60)}") }
}

// ponytail: orderByChild query → was O(n) full scan, now O(log n) index lookup
fun verificarDuplicadoImportSeguro(
    uid: String,
    sourceGroupId: String,
    sourceSubjectId: String,
    onResult: (isDuplicate: Boolean, errorMsg: String?) -> Unit
) {
    if (uid.isBlank() || sourceSubjectId.isBlank()) { onResult(false, "Parámetros incompletos"); return }
    groupImportsRef(uid).orderByChild("sourceSubjectId").equalTo(sourceSubjectId).limitToFirst(1)
        .get()
        .addOnSuccessListener { snapshot ->
            val match = snapshot.children.firstOrNull { child ->
                child.child("sourceGroupId").getValue(String::class.java) == sourceGroupId
            }
            onResult(match != null, null)
        }
        .addOnFailureListener { e -> onResult(false, "Error al verificar duplicados: ${e.message?.take(60)}") }
}
