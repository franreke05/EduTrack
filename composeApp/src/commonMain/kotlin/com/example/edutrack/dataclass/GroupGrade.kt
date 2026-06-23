package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class GroupGrade(
    var id: String? = null,
    var studentUid: String? = null,
    var subjectId: String? = null,
    var subjectName: String? = null,
    var nombreNota: String? = null,
    var valor: Double? = null,
    var peso: Double? = null,
    var fecha: String? = null,
    var createdBy: String? = null,
    var createdAt: Long? = null
)
