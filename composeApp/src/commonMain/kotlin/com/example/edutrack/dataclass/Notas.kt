package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

// Modelo de nota.
@Serializable
data class Notas(
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
    // Timestamp de creación para ordenar el historial de evolución.
    val creadoEn: Long = 0L
)
