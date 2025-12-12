package com.example.edutrack.dataclass

class Asignatura(
    var id: String? = "", // Id de la asignatura del año
    val nombre: String? = "", // Nombre de la asignatura (lo pone el usuario)
    val creditos: Int? = 0, // Créditos de la asignatura (lo pone el usuario)
    val descripcion: String? = "", // Descripción de la asignatura (lo pone el usuario)
    val lista_notas: MutableList<Notas>? = mutableListOf(), // Lista de notas con porcentaje
    val media: Double? = 0.0, // Media de la asignatura
    val id_usuario: String? = "", // Id del usuario al que pertenece
    val id_anio: String? = "", // Id del año al que pertenece
    // Configuración de periodos: "Trimestre" (3) o "Cuatrimestre" (4)
    val tipo_periodo: String? = "Trimestre",
    val numero_periodos: Int? = 3,
)
