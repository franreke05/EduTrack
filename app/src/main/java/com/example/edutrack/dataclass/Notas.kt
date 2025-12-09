package com.example.edutrack.dataclass

class Notas(
    var id: String? = "", // Id para identificar la nota
    var id_asignatura: String? = "", // Id de la asignatura a la que pertenece la nota
    val nombre: String? = "", // Nombre del examen/trabajo
    val nota: Double? = 0.0, // Calificacion
    val porcentaje: Double? = 0.0, // Peso de la nota dentro de la asignatura
    val fecha: String? = "", // Fecha del examen
    val periodo: Int? = 1 // Trimestre o cuatrimestre al que pertenece la nota
)
