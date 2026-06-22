package com.example.edutrack.dataclass

import kotlinx.serialization.Serializable

// Modelo de usuario.
@Serializable
data class Usuario(
    var id: String? = "",
    var nombre: String? = "",
    var email: String? = "",
    var photoUrl: String? = null,
    val anio: MutableList<Anio> = mutableListOf()
)
