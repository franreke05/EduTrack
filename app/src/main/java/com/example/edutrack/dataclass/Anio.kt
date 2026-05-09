package com.example.edutrack.dataclass

// Modelo de anio escolar.
data class Anio(
    var id: String? = "",
    var nombre: String? = "",
    val descripcion: String? = "",
    val fechaInicio: String? = "",
    val fechaFin: String? = "",
    var numero_asignaturas: Int? = 0,
    var lista_asignaturas: Map<String, Asignatura>? = emptyMap()
)
