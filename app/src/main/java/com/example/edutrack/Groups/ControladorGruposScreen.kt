package com.example.edutrack.Groups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.GroupExam
import com.example.edutrack.dataclass.GroupFeedEvent
import com.example.edutrack.dataclass.GroupGrade
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupResource
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.dataclass.UserGroup
import com.example.edutrack.groupExamsRef
import com.example.edutrack.groupFeedRef
import com.example.edutrack.groupGradesRef
import com.example.edutrack.groupMemberRef
import com.example.edutrack.groupMembersRef
import com.example.edutrack.groupRef
import com.example.edutrack.groupResourcesRef
import com.example.edutrack.groupSharedSubjectsRef
import com.example.edutrack.profileRef
import com.example.edutrack.studentGradesRef
import com.example.edutrack.userGroupsRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

// Estado en tiempo real de los grupos a los que pertenece el usuario.
@Composable
fun rememberUserGroupsState(userId: String?): State<List<UserGroup>> {
    val state = remember { mutableStateOf<List<UserGroup>>(emptyList()) }
    DisposableEffect(userId) {
        if (userId.isNullOrBlank()) {
            state.value = emptyList()
            return@DisposableEffect onDispose {}
        }
        val ref = userGroupsRef(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.children.mapNotNull { it.getValue(UserGroup::class.java) }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo grupos del usuario: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

// Estado en tiempo real de los miembros de un grupo, enriquecido con foto de perfil si falta.
@Composable
fun rememberGroupMembersState(groupId: String?): State<List<GroupMember>> {
    val state = remember { mutableStateOf<List<GroupMember>>(emptyList()) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            state.value = emptyList()
            return@DisposableEffect onDispose {}
        }
        val ref = groupMembersRef(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val members = snapshot.children.mapNotNull { it.getValue(GroupMember::class.java) }
                val missing = members.filter { it.photoUrl.isNullOrBlank() && it.uid != null }
                if (missing.isEmpty()) {
                    state.value = members
                    return
                }
                val enriched = members.toMutableList()
                var pending = missing.size
                missing.forEach { member ->
                    profileRef(member.uid!!).child("photoUrl").get()
                        .addOnSuccessListener { snap ->
                            val url = snap.getValue(String::class.java)
                            if (!url.isNullOrBlank()) {
                                val idx = enriched.indexOfFirst { it.uid == member.uid }
                                if (idx >= 0) enriched[idx] = enriched[idx].copy(photoUrl = url)
                                // Backfill photo into group member record so future reads don't need this fetch.
                                groupMemberRef(groupId, member.uid!!).child("photoUrl").setValue(url)
                            }
                            if (--pending == 0) state.value = enriched.toList()
                        }
                        .addOnFailureListener {
                            if (--pending == 0) state.value = enriched.toList()
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo miembros: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

// Estado en tiempo real del detalle de un grupo.
@Composable
fun rememberGroupState(groupId: String?): State<Group?> {
    val state = remember { mutableStateOf<Group?>(null) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            state.value = null
            return@DisposableEffect onDispose {}
        }
        val ref = groupRef(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.getValue(Group::class.java)
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo grupo: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

// Estado en tiempo real del feed de novedades del grupo (últimos 50 eventos, orden descendente).
@Composable
fun rememberGroupFeedState(groupId: String?): State<List<GroupFeedEvent>> {
    val state = remember { mutableStateOf<List<GroupFeedEvent>>(emptyList()) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            state.value = emptyList()
            return@DisposableEffect onDispose {}
        }
        val query = groupFeedRef(groupId).orderByChild("createdAt").limitToLast(50)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = snapshot.children
                    .mapNotNull { it.getValue(GroupFeedEvent::class.java) }
                    .sortedByDescending { it.createdAt ?: 0L }
                state.value = events
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo feed: ${error.message}")
            }
        }
        query.addValueEventListener(listener)
        onDispose { query.removeEventListener(listener) }
    }
    return state
}

// Estado en tiempo real de las asignaturas compartidas en un grupo.
@Composable
fun rememberGroupSharedSubjectsState(groupId: String?): State<List<GroupSharedSubject>> {
    val state = remember { mutableStateOf<List<GroupSharedSubject>>(emptyList()) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            state.value = emptyList()
            return@DisposableEffect onDispose {}
        }
        val ref = groupSharedSubjectsRef(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.children.mapNotNull { it.getValue(GroupSharedSubject::class.java) }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo asignaturas compartidas: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

// Estado en tiempo real de los recursos compartidos en un grupo.
@Composable
fun rememberGroupResourcesState(groupId: String?): State<List<GroupResource>> {
    val state = remember { mutableStateOf<List<GroupResource>>(emptyList()) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            state.value = emptyList()
            return@DisposableEffect onDispose {}
        }
        val ref = groupResourcesRef(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.children
                    .mapNotNull { it.getValue(GroupResource::class.java) }
                    .sortedByDescending { it.createdAt ?: 0L }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo recursos: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

// Estado en tiempo real de los exámenes anunciados en un grupo.
@Composable
fun rememberGroupExamsState(groupId: String?): State<List<GroupExam>> {
    val state = remember { mutableStateOf<List<GroupExam>>(emptyList()) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            state.value = emptyList()
            return@DisposableEffect onDispose {}
        }
        val ref = groupExamsRef(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.children
                    .mapNotNull { it.getValue(GroupExam::class.java) }
                    .sortedBy { it.fecha ?: "" }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo exámenes del grupo: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

// Notas de un estudiante concreto en un grupo aula — para la vista del alumno.
@Composable
fun rememberStudentGradesState(groupId: String?, studentUid: String?): State<List<GroupGrade>> {
    val state = remember { mutableStateOf<List<GroupGrade>>(emptyList()) }
    DisposableEffect(groupId, studentUid) {
        if (groupId.isNullOrBlank() || studentUid.isNullOrBlank()) {
            state.value = emptyList()
            return@DisposableEffect onDispose {}
        }
        val ref = studentGradesRef(groupId, studentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.children
                    .mapNotNull { it.getValue(GroupGrade::class.java) }
                    .sortedByDescending { it.createdAt ?: 0L }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo notas del alumno: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

// Notas de todos los alumnos de un grupo — para la vista del profesor.
// ponytail: Map<studentUid, List<GroupGrade>> — un solo listener, sin stream por alumno
@Composable
fun rememberAllGroupGradesState(groupId: String?): State<Map<String, List<GroupGrade>>> {
    val state = remember { mutableStateOf<Map<String, List<GroupGrade>>>(emptyMap()) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            state.value = emptyMap()
            return@DisposableEffect onDispose {}
        }
        val ref = groupGradesRef(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.children.associate { studentSnap ->
                    val uid = studentSnap.key ?: return@associate "" to emptyList()
                    val grades = studentSnap.children
                        .mapNotNull { it.getValue(GroupGrade::class.java) }
                        .sortedByDescending { it.createdAt ?: 0L }
                    uid to grades
                }.filterKeys { it.isNotBlank() }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("Grupos", "Error leyendo notas del grupo: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}
