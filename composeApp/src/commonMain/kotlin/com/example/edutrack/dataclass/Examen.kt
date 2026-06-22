package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class Examen(
    val id: String = "",
    val nombre: String = "",          // "Parcial 1", "Examen Final", etc
    val fecha: String = "",           // "dd/MM/yyyy"
    val hora: String = "",            // "HH:mm", puede ser vacío
    val asignaturaId: String = "",
    val asignaturaNombre: String = "",
    val creadoEn: Long = 0L
)
