package com.example.edutrack.dataclass

// Modelo de anio escolar.
class Anio(
    // Id del anio (se usa en relaciones).
    var id: String? = "",
    // Nombre del anio (lo define el usuario).
    var nombre: String? = "",
    // Descripcion del anio (lo define el usuario).
    val descripcion: String? = "",
    // Fecha de inicio del anio.
    val fechaInicio: String? = "",
    // Fecha de fin del anio.
    val fechaFin: String? = "",
    // Numero de asignaturas del anio.
    var numero_asignaturas: Int? = 0,
    // Mapa de asignaturas indexadas por id.
    var lista_asignaturas: Map<String, Asignatura>? = emptyMap(),
    // Id del usuario que creo el anio.
    val id_user: String? = ""
)
