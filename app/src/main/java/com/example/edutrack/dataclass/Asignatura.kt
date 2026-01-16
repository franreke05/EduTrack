package com.example.edutrack.dataclass

// Modelo de asignatura.
data class Asignatura(
    // Id de la asignatura del anio.
    var id: String? = "",
    // Nombre de la asignatura (lo define el usuario).
    val nombre: String? = "",
    // Creditos de la asignatura (lo define el usuario).
    val creditos: Int? = 0,
    // Descripcion de la asignatura (lo define el usuario).
    val descripcion: String? = "",
    // Lista de notas con su porcentaje.
    val lista_notas: MutableList<Notas>? = mutableListOf(),
    // Media de la asignatura.
    val media: Double? = 0.0,
    // Id del usuario al que pertenece.
    val id_usuario: String? = "",
    // Id del anio al que pertenece.
    val id_anio: String? = "",
    // Tipo de periodo: "Trimestre" (3) o "Cuatrimestre" (4).
    val tipo_periodo: String? = "Trimestre",
    // Numero de periodos configurado.
    val numero_periodos: Int? = 3
) : java.io.Serializable
