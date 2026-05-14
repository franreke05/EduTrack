package com.example.edutrack.dataclass

enum class GroupFeedEventType { JOIN, LEAVE, SHARE, UNSHARE, IMPORT }

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
