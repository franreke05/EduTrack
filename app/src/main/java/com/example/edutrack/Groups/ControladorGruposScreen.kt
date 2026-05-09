package com.example.edutrack.Groups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.dataclass.UserGroup
import com.example.edutrack.groupMemberRef
import com.example.edutrack.groupMembersRef
import com.example.edutrack.groupRef
import com.example.edutrack.groupSharedSubjectsRef
import com.example.edutrack.profileRef
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
