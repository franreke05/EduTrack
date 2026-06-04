package com.example.edutrack.dataclass

data class GroupResource(
    var id: String? = null,
    var title: String? = null,
    var url: String? = null,
    var description: String? = null,
    var tag: String? = null,
    var authorId: String? = null,
    var authorName: String? = null,
    var authorPhotoUrl: String? = null,
    var createdAt: Long? = null
)
