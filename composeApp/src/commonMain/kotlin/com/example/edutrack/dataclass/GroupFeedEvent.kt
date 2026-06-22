package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

@Serializable
enum class GroupFeedEventType { JOIN, LEAVE, SHARE, UNSHARE, IMPORT, RESOURCE, EXAM }

@Serializable
data class GroupFeedEvent(
    var id: String? = null,
    var type: String? = null,
    var actorUid: String? = null,
    var actorName: String? = null,
    var actorPhotoUrl: String? = null,
    var targetId: String? = null,
    var targetLabel: String? = null,
    var createdAt: Long? = null
)
