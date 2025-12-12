package com.example.edutrack.dataclass

class Usuario(
    var id: String? = "", // id para identificar el usuario y realizar las relaciones
    var nombre: String? = "", // Nombre que tiene puesto el usuario
    var email: String? = "", // Email asociado a la cuenta del usuario
    var password: String? = "", // Contraseña asociada a la cuenta del usuario
    val anio: MutableList<Anio> = mutableListOf(), // Lista de años que tiene el usuario
)
