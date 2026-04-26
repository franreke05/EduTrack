package com.example.edutrack.data.groups

import com.example.edutrack.core.FreemiumLimits
import com.example.edutrack.data.firebase.FirebasePaths
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupSettings
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

sealed interface GroupResult {
    data class Success(val groupId: String) : GroupResult
    data class Error(val message: String) : GroupResult
}

class GroupRepository(
    private val database: DatabaseReference = Firebase.database.reference
) {
    private val groupsRef: DatabaseReference = FirebasePaths.groups()
    private val userGroupsRoot: DatabaseReference = database.child("userGroups")

    suspend fun createGroup(
        userId: String,
        displayName: String,
        name: String,
        description: String,
        settings: GroupSettings,
        isPremium: Boolean
    ): GroupResult {
        if (!isPremium) {
            return GroupResult.Error("Crear grupos es una funcion Premium.")
        }
        if (userId.isBlank()) return GroupResult.Error("Sesion no valida.")
        if (name.isBlank()) return GroupResult.Error("El grupo necesita un nombre.")

        val groupId = groupsRef.push().key ?: return GroupResult.Error("No se pudo crear el identificador del grupo.")
        val now = System.currentTimeMillis()
        val group = Group(
            id = groupId,
            name = name.trim(),
            description = description.trim(),
            creatorId = userId,
            ownerId = userId,
            createdAt = now,
            updatedAt = now,
            settings = settings,
            members = emptyMap()
        )
        val member = GroupMember(
            userId = userId,
            displayName = displayName.ifBlank { "Admin" },
            role = "admin",
            joinedAt = now,
            updatedAt = now
        )
        return try {
            database.updateChildren(
                mapOf(
                    "groups/$groupId" to group,
                    "groupMembers/$groupId/$userId" to member,
                    "userGroups/$userId/$groupId" to mapOf(
                        "groupId" to groupId,
                        "role" to "admin",
                        "joinedAt" to now
                    )
                )
            ).await()
            GroupResult.Success(groupId)
        } catch (e: Exception) {
            GroupResult.Error("No se pudo crear el grupo. Revisa tu conexion y permisos.")
        }
    }

    suspend fun joinGroup(
        userId: String,
        displayName: String,
        groupId: String,
        isPremium: Boolean
    ): GroupResult {
        if (userId.isBlank()) return GroupResult.Error("Sesion no valida.")
        if (groupId.isBlank()) return GroupResult.Error("Introduce el codigo del grupo.")
        val myGroupCount = loadMyGroups(userId).size
        if (!isPremium && myGroupCount >= FreemiumLimits.MAX_FREE_GROUPS) {
            return GroupResult.Error("La version gratis permite unirse a 1 grupo.")
        }
        val cleanGroupId = groupId.trim()
        val groupSnapshot = groupsRef.child(cleanGroupId).get().await()
        if (!groupSnapshot.exists()) return GroupResult.Error("No existe ningun grupo con ese codigo.")

        val now = System.currentTimeMillis()
        val member = GroupMember(
            userId = userId,
            displayName = displayName.ifBlank { "Miembro" },
            role = "member",
            joinedAt = now,
            updatedAt = now
        )
        return try {
            database.updateChildren(
                mapOf(
                    "groupMembers/$cleanGroupId/$userId" to member,
                    "userGroups/$userId/$cleanGroupId" to mapOf(
                        "groupId" to cleanGroupId,
                        "role" to "member",
                        "joinedAt" to now
                    )
                )
            ).await()
            GroupResult.Success(cleanGroupId)
        } catch (e: Exception) {
            GroupResult.Error("No se pudo unir al grupo. Revisa el codigo o tus permisos.")
        }
    }

    suspend fun leaveGroup(userId: String, groupId: String) {
        database.updateChildren(
            mapOf<String, Any?>(
                "groupMembers/$groupId/$userId" to null,
                "userGroups/$userId/$groupId" to null
            )
        ).await()
    }

    suspend fun loadMyGroups(userId: String): List<Group> {
        val membershipSnapshot = userGroupsRoot.child(userId).get().await()
        return membershipSnapshot.children.mapNotNull { child ->
            val groupId = child.key ?: return@mapNotNull null
            runCatching { loadGroupWithMembers(groupId, userId) }.getOrNull()
        }
    }

    private suspend fun loadGroupWithMembers(groupId: String, requiredMemberId: String? = null): Group? {
        val memberSnapshot = FirebasePaths.groupMembers(groupId).get().await()
        if (requiredMemberId != null && !memberSnapshot.hasChild(requiredMemberId)) return null
        val members = memberSnapshot.children.mapNotNull { child ->
            child.getValue(GroupMember::class.java)?.let { member ->
                (member.userId ?: child.key)?.let { it to member }
            }
        }.toMap()
        return groupsRef.child(groupId).get().await()
            .getValue(Group::class.java)
            ?.copy(members = members)
            ?.also { it.id = groupId }
    }
}
