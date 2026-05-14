package com.example.edutrack

import android.content.Context
import android.util.Log
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Examen
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.GroupFeedEventType
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.SubjectImport
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
fun examenesRef(userId: String, anioId: String, asignaturaId: String) = asignaturaRef(userId, anioId, asignaturaId).child("examenes")

// Rutas de grupos (nivel raíz bajo ROOT_NODE, no bajo users/).
fun groupsRef() = db().child(ROOT_NODE).child("groups")
fun groupRef(groupId: String) = groupsRef().child(groupId)
fun groupMembersRef(groupId: String) = db().child(ROOT_NODE).child("groupMembers").child(groupId)
fun groupMemberRef(groupId: String, uid: String) = groupMembersRef(groupId).child(uid)
fun userGroupsRef(uid: String) = db().child(ROOT_NODE).child("userGroups").child(uid)
fun groupSharedSubjectsRef(groupId: String) = db().child(ROOT_NODE).child("groupSharedSubjects").child(groupId)
fun groupFeedRef(groupId: String) = db().child(ROOT_NODE).child("groupFeed").child(groupId)
fun groupImportsRef(uid: String) = userRef(uid).child("groupImports")
fun groupImportsMetaRef(uid: String, monthKey: String) = groupImportsRef(uid).child("_meta").child(monthKey)

// Clave de mes en UTC local (YYYY-MM) para agrupar el contador mensual de imports.
fun currentMonthKey(): String {
    val cal = java.util.Calendar.getInstance()
    return "%04d-%02d".format(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
}

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
    val updates = mapOf<String, Any>(
        "nombre" to nombre,
        "descripcion" to descripcion,
        "numero_asignaturas" to maxAsignaturas,
        "tipo_periodo" to tipoPeriodo
    )
    anioRef(userId, anioId).updateChildren(updates)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener {
            Log.e("DB", "Error editando año $anioId", it)
            onResult(false)
        }
}

fun borrarAnioCompleto(
    userId: String,
    anioId: String,
    onResult: (Boolean) -> Unit = {}
) {
    if (anioId.isBlank()) { onResult(false); return }
    anioRef(userId, anioId).removeValue()
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener {
            Log.e("DB", "Error borrando año $anioId", it)
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
                    .addOnSuccessListener {
                        logFeedEvent(
                            groupId = groupId,
                            type = GroupFeedEventType.JOIN,
                            actorUid = uid,
                            actorName = displayName,
                            actorPhotoUrl = photoUrl
                        )
                        onResult(true, groupId)
                    }
                    .addOnFailureListener { onResult(false, "Error al unirse al grupo") }
            }.addOnFailureListener { e -> onResult(false, "Error verificando miembro: ${e.message?.take(60)}") }
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
    // El evento LEAVE se emite ANTES del removeValue porque las reglas exigen que el
    // autor sea miembro activo para escribir en groupFeed.
    logFeedEvent(
        groupId = groupId,
        type = GroupFeedEventType.LEAVE,
        actorUid = uid,
        actorName = actorName,
        actorPhotoUrl = actorPhotoUrl
    )
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
    val toWrite = subject.copy(id = subjectId)
    groupSharedSubjectsRef(groupId).child(subjectId).setValue(toWrite)
        .addOnSuccessListener {
            val actorUid = toWrite.sharedBy
            if (!actorUid.isNullOrBlank()) {
                logFeedEvent(
                    groupId = groupId,
                    type = GroupFeedEventType.SHARE,
                    actorUid = actorUid,
                    actorName = toWrite.sharedByName,
                    actorPhotoUrl = toWrite.sharedByPhotoUrl,
                    targetId = subjectId,
                    targetLabel = toWrite.name
                )
            }
            onResult(true)
        }
        .addOnFailureListener { Log.e("DB", "Error compartiendo asignatura", it); onResult(false) }
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
                logFeedEvent(
                    groupId = groupId,
                    type = GroupFeedEventType.UNSHARE,
                    actorUid = actorUid,
                    actorName = actorName,
                    actorPhotoUrl = actorPhotoUrl,
                    targetId = subjectId,
                    targetLabel = subjectName
                )
            }
            onResult(true)
        }
        .addOnFailureListener { Log.e("DB", "Error eliminando asignatura compartida", it); onResult(false) }
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
    val event = mapOf(
        "id" to eventId,
        "type" to type.name,
        "actorUid" to actorUid,
        "actorName" to actorName,
        "actorPhotoUrl" to actorPhotoUrl,
        "targetId" to targetId,
        "targetLabel" to targetLabel,
        "createdAt" to ServerValue.TIMESTAMP
    )
    ref.setValue(event)
        .addOnFailureListener { Log.e("DB", "Error logeando feed event ${type.name}", it) }
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
    if (sourceSubjectId.isNullOrBlank()) {
        onResult(false, "Asignatura origen sin identificador")
        return
    }
    if (creditos < 1) {
        onResult(false, "Créditos inválidos")
        return
    }

    // Deduplicación: el contador mensual no se incrementa si ya existía un import previo.
    groupImportsRef(uid).orderByChild("sourceSubjectId").equalTo(sourceSubjectId).limitToFirst(1)
        .get()
        .addOnSuccessListener { dupSnap ->
            if (dupSnap.exists()) {
                onResult(false, "Ya importaste esta asignatura")
                return@addOnSuccessListener
            }
            val asignaturaId = UUID.randomUUID().toString()
            val asignatura = Asignatura(
                id = asignaturaId,
                nombre = shared.name,
                creditos = creditos,
                descripcion = "",
                media = 0.0,
                numero_notas = 0,
                tipo_periodo = shared.tipoPeriodo ?: "Trimestre",
                numero_periodos = shared.numeroPeriodos ?: 3,
                fechaExamen = null,
                horaExamen = null
            )
            val importId = groupImportsRef(uid).push().key ?: run {
                onResult(false, "Error generando identificador")
                return@addOnSuccessListener
            }
            val importEntry = SubjectImport(
                id = importId,
                sourceGroupId = sourceGroupId,
                sourceSubjectId = sourceSubjectId,
                subjectName = shared.name,
                importedAt = System.currentTimeMillis(),
                targetAnioId = targetAnioId,
                targetAsignaturaId = asignaturaId
            )

            asignaturaRef(uid, targetAnioId, asignaturaId).setValue(asignatura)
                .addOnSuccessListener {
                    groupImportsRef(uid).child(importId).setValue(importEntry)
                    groupImportsMetaRef(uid, currentMonthKey()).child("count")
                        .setValue(ServerValue.increment(1))
                    logFeedEvent(
                        groupId = sourceGroupId,
                        type = GroupFeedEventType.IMPORT,
                        actorUid = uid,
                        actorName = actorName,
                        actorPhotoUrl = actorPhotoUrl,
                        targetId = sourceSubjectId,
                        targetLabel = shared.name
                    )
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e("DB", "Error importando asignatura", e)
                    onResult(false, "Error al crear la asignatura")
                }
        }
        .addOnFailureListener { e ->
            onResult(false, "Error comprobando duplicados: ${e.message?.take(60)}")
        }
}
