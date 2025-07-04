package com.example.edutrack.dataclass

class Asignatura(
    var id: String?="",
    val nombre: String?="",
    val creditos: Int?=0,
    val descripcion: String?="",
) : java.io.Serializable
