package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

// Modelo de asignatura.
@Serializable
data class Asignatura(
    var id: String? = "",
    val nombre: String? = "",
    val creditos: Int? = 0,
    val descripcion: String? = "",
    val media: Double? = 0.0,
    val numero_notas: Int? = 0,
    val tipo_periodo: String? = "Trimestre",
    val numero_periodos: Int? = 3,
    val fechaExamen: String? = null,
    val horaExamen: String? = null
)
