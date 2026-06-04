package com.example.edutrack.data.repository

import android.util.Log
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.dataclass.GroupSharedSubject
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * Repository para gestionar grupos colaborativos en Firebase RTDB.
 * Proporciona operaciones para crear, unirse y compartir recursos en grupos.
 */
class GroupRepository(private val database: FirebaseDatabase) {

    companion object {
        private const val TAG = "GroupRepository"
        private const val ROOT_NODE = "Edutrack"
        private const val INVITE_CODE_LENGTH = 6
        private const val INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }

    private fun groupsRef() = database.reference.child(ROOT_NODE).child("groups")
    private fun groupRef(groupId: String) = groupsRef().child(groupId)
    private fun groupMembersRef(groupId: String) =
        database.reference.child(ROOT_NODE).child("groupMembers").child(groupId)

    private fun groupMemberRef(groupId: String, uid: String) =
        groupMembersRef(groupId).child(uid)

    private fun userGroupsRef(uid: String) =
        database.reference.child(ROOT_NODE).child("userGroups").child(uid)

    private fun groupSharedSubjectsRef(groupId: String) =
        database.reference.child(ROOT_NODE).child("groupSharedSubjects").child(groupId)

    private fun groupFeedRef(groupId: String) =
        database.reference.child(ROOT_NODE).child("groupFeed").child(groupId)

    /**
     * Genera un código de invitación alfanumérico único.
     *
     * @return String con el código de 6 caracteres
     */
    private fun generateInviteCode(): String {
        return (1..INVITE_CODE_LENGTH).map { INVITE_CODE_CHARS.random() }.joinToString("")
    }

    /**
     * Crea un nuevo grupo colaborativo.
     *
     * @param ownerUid ID del usuario propietario
     * @param group Objeto Group con los datos del grupo
     * @return Result<String> con el ID del grupo creado
     */
    fun createGroup(
        ownerUid: String,
        group: Group,
        ownerDisplayName: String,
        ownerPhotoUrl: String? = null
    ): Result<String> {
        return try {
            if (ownerUid.isEmpty() || group.name.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Datos incompletos"))
            }

            val groupId = groupsRef().push().key
                ?: return Result.failure(Exception("Error generando ID de grupo"))

            val inviteCode = generateInviteCode()
            val now = System.currentTimeMillis()

            val groupData = mapOf(
                "id" to groupId,
                "name" to group.name,
                "description" to (group.description ?: ""),
                "ownerUid" to ownerUid,
                "createdAt" to now,
                "inviteCode" to inviteCode,
                "isPrivate" to (group.isPrivate ?: false),
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
                "name" to group.name,
                "role" to GroupRole.OWNER.name,
                "joinedAt" to now
            )

            val updates = mapOf(
                "/${ROOT_NODE}/groups/$groupId" to groupData,
                "/${ROOT_NODE}/groupMembers/$groupId/$ownerUid" to member,
                "/${ROOT_NODE}/userGroups/$ownerUid/$groupId" to userGroup
            )

            Log.d(TAG, "Creando grupo $groupId")
            database.reference.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Grupo creado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error creando grupo", e)
                }

            Result.success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al crear grupo", e)
            Result.failure(e)
        }
    }

    /**
     * Permite que un usuario se una a un grupo usando código de invitación.
     *
     * @param uid ID del usuario
     * @param inviteCode Código de invitación del grupo
     * @param displayName Nombre mostrado del usuario
     * @param photoUrl URL de foto del usuario
     * @return Result<String> con el ID del grupo al que se unió
     */
    fun joinGroupByCode(
        uid: String,
        inviteCode: String,
        displayName: String,
        photoUrl: String? = null
    ): Result<String> {
        return try {
            if (uid.isEmpty() || inviteCode.isEmpty() || displayName.isEmpty()) {
                return Result.failure(IllegalArgumentException("Datos incompletos"))
            }

            // Se busca el grupo con el código
            val query = groupsRef().orderByChild("inviteCode").equalTo(inviteCode).limitToFirst(1)

            query.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Log.e(TAG, "Código de invitación inválido")
                    return@addOnSuccessListener
                }

                val groupSnap = snapshot.children.firstOrNull()
                val groupId = groupSnap?.key ?: return@addOnSuccessListener
                val groupName = groupSnap.child("name").getValue(String::class.java) ?: ""

                // Verifica si ya es miembro
                groupMemberRef(groupId, uid).get().addOnSuccessListener { memberSnap ->
                    if (memberSnap.exists()) {
                        Log.w(TAG, "Usuario ya es miembro del grupo")
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

                    database.reference.updateChildren(updates)
                        .addOnSuccessListener {
                            Log.d(TAG, "Usuario $uid se unió al grupo $groupId")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error al unirse al grupo", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error buscando grupo", e)
            }

            Result.success("")
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al unirse a grupo", e)
            Result.failure(e)
        }
    }

    /**
     * Comparte una asignatura con el grupo.
     *
     * @param groupId ID del grupo
     * @param subject Asignatura a compartir
     * @return Result<String> con el ID del recurso compartido
     */
    fun shareSubjectWithGroup(
        groupId: String,
        subject: GroupSharedSubject
    ): Result<String> {
        return try {
            if (groupId.isEmpty()) {
                return Result.failure(IllegalArgumentException("ID de grupo vacío"))
            }

            val subjectId = subject.id ?: UUID.randomUUID().toString()
            val toWrite = subject.copy(id = subjectId, sharedAt = System.currentTimeMillis())

            Log.d(TAG, "Compartiendo asignatura $subjectId con grupo $groupId")
            groupSharedSubjectsRef(groupId).child(subjectId).setValue(toWrite)
                .addOnSuccessListener {
                    Log.d(TAG, "Asignatura compartida exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error compartiendo asignatura", e)
                }

            Result.success(subjectId)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al compartir asignatura", e)
            Result.failure(e)
        }
    }

    /**
     * Permite que un usuario abandone un grupo.
     *
     * @param uid ID del usuario
     * @param groupId ID del grupo a abandonar
     * @return Result<Unit>
     */
    fun leaveGroup(uid: String, groupId: String): Result<Unit> {
        return try {
            if (uid.isEmpty() || groupId.isEmpty()) {
                return Result.failure(IllegalArgumentException("IDs vacíos"))
            }

            val updates = mapOf(
                "/${ROOT_NODE}/groupMembers/$groupId/$uid" to null,
                "/${ROOT_NODE}/userGroups/$uid/$groupId" to null,
                "/${ROOT_NODE}/groups/$groupId/memberCount" to ServerValue.increment(-1)
            )

            Log.d(TAG, "Usuario $uid abandonando grupo $groupId")
            database.reference.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Usuario abandonó el grupo exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error abandonando grupo", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al abandonar grupo", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene los miembros de un grupo como un Flow reactivo.
     *
     * @param groupId ID del grupo
     * @return Flow<List<GroupMember>> con los miembros del grupo
     */
    fun getGroupMembers(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        val ref = groupMembersRef(groupId)
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val members = mutableListOf<GroupMember>()
                        for (child in snapshot.children) {
                            val member = child.getValue(GroupMember::class.java)
                            if (member != null) {
                                members.add(member)
                            }
                        }
                        trySend(members)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando miembros", e)
                        trySend(emptyList())
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo miembros: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene un grupo específico como un Flow reactivo.
     *
     * @param groupId ID del grupo
     * @return Flow<Group?> con los datos del grupo
     */
    fun getGroup(groupId: String): Flow<Group?> = callbackFlow {
        val ref = groupRef(groupId)
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val group = snapshot.getValue(Group::class.java)
                        trySend(group)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando grupo", e)
                        trySend(null)
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo grupo: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    /**
     * Obtiene los asuntos compartidos de un grupo.
     *
     * @param groupId ID del grupo
     * @return Flow<List<GroupSharedSubject>> con los asuntos compartidos
     */
    fun getGroupSharedSubjects(groupId: String): Flow<List<GroupSharedSubject>> = callbackFlow {
        val ref = groupSharedSubjectsRef(groupId)
        val listener = ref.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    try {
                        val subjects = mutableListOf<GroupSharedSubject>()
                        for (child in snapshot.children) {
                            val subject = child.getValue(GroupSharedSubject::class.java)
                            if (subject != null) {
                                subjects.add(subject)
                            }
                        }
                        trySend(subjects)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializando asuntos compartidos", e)
                        trySend(emptyList())
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Log.e(TAG, "Error leyendo asuntos compartidos: ${error.message}")
                    close(error.toException())
                }
            }
        )

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}
