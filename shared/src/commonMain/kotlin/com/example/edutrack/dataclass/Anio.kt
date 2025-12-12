package com.example.edutrack.dataclass

class Anio(
    var id: String? = "", // Id para el año (útil para funciones)
    var nombre: String? = "", // Nombre del año (lo pone el usuario)
    val descripcion: String? = "", // Descripción del año (lo pone el usuario)
    val fechaInicio: String? = "", // Fecha de inicio del año
    val fechaFin: String? = "", // Fecha de fin del año
    var numero_asignaturas: Int? = 0, // Número de asignaturas que tiene el año
    var lista_asignaturas: Map<String, Asignatura>? = emptyMap(), // Asignaturas indexadas por id/clave
    val id_user: String? = "", // Id del usuario que creó el año
)
