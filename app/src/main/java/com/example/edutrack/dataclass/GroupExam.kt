package com.example.edutrack.dataclass

data class GroupExam(
    var id: String? = null,
    var nombre: String? = null,
    var asignatura: String? = null,
    var fecha: String? = null,
    var hora: String? = null,
    var authorId: String? = null,
    var authorName: String? = null,
    var authorPhotoUrl: String? = null,
    var createdAt: Long? = null
)
