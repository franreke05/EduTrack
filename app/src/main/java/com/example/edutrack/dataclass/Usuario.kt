package com.example.edutrack.dataclass

// Modelo de usuario.
data class Usuario(
    // Id para identificar al usuario y relacionarlo en la base de datos.
    var id: String? = "",
    // Nombre que tiene el usuario.
    var nombre: String? = "",
    // Email asociado a la cuenta.
    var email: String? = "",
    // Contrasena asociada a la cuenta.
    var password: String? = "",
    // Lista de anios que tiene el usuario.
    val anio: MutableList<Anio> = mutableListOf()
) : java.io.Serializable
