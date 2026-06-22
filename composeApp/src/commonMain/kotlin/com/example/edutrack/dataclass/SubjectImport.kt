package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class SubjectImport(
    var id: String? = null,
    var sourceGroupId: String? = null,
    var sourceSubjectId: String? = null,
    var subjectName: String? = null,
    var importedAt: Long? = null,
    var targetAnioId: String? = null,
    var targetAsignaturaId: String? = null
)
