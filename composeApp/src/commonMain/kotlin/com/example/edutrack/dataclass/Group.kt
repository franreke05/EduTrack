package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

@Serializable
enum class GroupRole { OWNER, ADMIN, MEMBER }

@Serializable
data class Group(
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var ownerUid: String? = null,
    var createdAt: Long? = null,
    var inviteCode: String? = null,
    var isPrivate: Boolean = false,
    var memberCount: Int = 0
)

@Serializable
data class GroupMember(
    var uid: String? = null,
    var role: String? = GroupRole.MEMBER.name,
    var joinedAt: Long? = null,
    var displayName: String? = null,
    var photoUrl: String? = null
)

@Serializable
data class UserGroup(
    var groupId: String? = null,
    var name: String? = null,
    var role: String? = GroupRole.MEMBER.name,
    var joinedAt: Long? = null
)

@Serializable
data class GroupSharedSubject(
    var id: String? = null,
    var name: String? = null,
    var tipoPeriodo: String? = null,
    var numeroPeriodos: Int? = null,
    var media: Double? = null,
    var sharedBy: String? = null,
    var sharedByName: String? = null,
    var sharedByPhotoUrl: String? = null,
    var sharedAt: Long? = null
)
