package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

// Modelo de anio escolar.
@Serializable
data class Anio(
    var id: String? = "",
    var nombre: String? = "",
    val descripcion: String? = "",
    val fechaInicio: String? = "",
    val fechaFin: String? = "",
    var numero_asignaturas: Int? = 0,
    var lista_asignaturas: Map<String, Asignatura>? = emptyMap(),
    val nota_minima_aprobado: Double? = 5.0,
    val tipo_ponderacion: String? = "creditos",  // "creditos" (ECTS-weighted) o "simple"
    val tipo_periodo: String? = "Cuatrimestre"   // "Cuatrimestre" o "Trimestre"
)
