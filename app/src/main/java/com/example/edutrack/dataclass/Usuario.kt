package com.example.edutrack.dataclass

// Modelo de usuario.
data class Usuario(
    var id: String? = "",
    var nombre: String? = "",
    var email: String? = "",
    var photoUrl: String? = null,
    val anio: MutableList<Anio> = mutableListOf()
) : java.io.Serializable
