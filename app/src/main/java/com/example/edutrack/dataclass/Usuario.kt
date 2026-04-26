package com.example.edutrack.dataclass

// Modelo de usuario.
class Usuario(
    // Id para identificar al usuario y relacionarlo en la base de datos.
    var id: String? = "",
    // Nombre que tiene el usuario.
    var nombre: String? = "",
    // Email asociado a la cuenta.
    var email: String? = "",
    var photoUrl: String? = "",
    var preferences: UserPreferences? = UserPreferences(),
    var createdAt: Long? = 0L,
    var updatedAt: Long? = 0L,
    var lastLoginAt: Long? = 0L,
    // Lista de anios que tiene el usuario.
    val anio: MutableList<Anio> = mutableListOf()
) : java.io.Serializable

data class UserPreferences(
    val accentColor: String? = "blue",
    val avatarStyle: String? = "initials",
    val visualTheme: String? = "system",
    val advancedTheme: String? = null,
    val coverStyle: String? = null,
    val badge: String? = null
) : java.io.Serializable
