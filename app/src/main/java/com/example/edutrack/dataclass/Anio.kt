package com.example.edutrack.dataclass


class Anio(
    var id: String? = "",
    var nombre: String? = "",
    val descripcion: String? = "",
    val fechaInicio: String? = "",
    val fechaFin: String? = "",
    var lista_asignaturas: List<Asignatura>? = mutableListOf<Asignatura>(),
    val id_user: String? = "",
)

