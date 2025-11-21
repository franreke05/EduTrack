package com.example.edutrack.dataclass



class Usuario (
    var id: String ?= "", //id para identificar el usuario y relizar las relaciones
    //en la base de datos

    var nombre: String? = "", //Nombre que tiene puesto el usuario

    var email: String? = "", //El Email asociado a la cuenta que tiene el usuario

    var password: String? = "", //Contraseña asociada a la cuenta del usuario

    val anio:MutableList<Anio> = mutableListOf() //Lista de años que tiene el usuario

):java.io.Serializable