package com.example.edutrack.dataclass

// Modelo de nota.
class Notas(
    // Id para identificar la nota.
    var id: String? = "",
    // Id de la asignatura a la que pertenece.
    var id_asignatura: String? = "",
    // Nombre del examen o trabajo.
    val nombre: String? = "",
    // Calificacion obtenida.
    val nota: Double? = 0.0,
    // Peso de la nota dentro de la asignatura.
    val porcentaje: Double? = 0.0,
    // Fecha del examen.
    val fecha: String? = "",
    // Trimestre o cuatrimestre al que pertenece la nota.
    val periodo: Int? = 1,
    val ownerId: String? = "",
    val yearId: String? = "",
    val subjectId: String? = id_asignatura,
    val name: String? = nombre,
    val grade: Double? = nota,
    val percentage: Double? = porcentaje,
    val period: Int? = periodo,
    val createdAt: Long? = 0L,
    val updatedAt: Long? = 0L
)
