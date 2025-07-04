package com.example.edutrack.dataclass

class Curso (
    var id: String?="",
    val nombre: String?="",
    val asignaturas: MutableList<Asignatura> = mutableListOf()
)
