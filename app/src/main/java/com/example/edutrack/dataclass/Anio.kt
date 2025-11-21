package com.example.edutrack.dataclass


class Anio(
    var id: String? = "", //Id para el año (util para funciones)

    var nombre: String? = "", //Nombre del año (Lo pone el usuario)

    val descripcion: String? = "", //Descripcion del año (Lo pone el usuario)

    val fechaInicio: String? = "", //Fecha de inicio del año

    val fechaFin: String? = "", //Fecha de fin del año

    var numero_asignaturas: Int? = 0, //Numero de asignaturas que tiene el año

    var lista_asignaturas: List<Asignatura>? = mutableListOf<Asignatura>(),
    //Lista de asignaturas que tiene el año

    val id_user: String? = "", //Id del usuario que creo el año
)

