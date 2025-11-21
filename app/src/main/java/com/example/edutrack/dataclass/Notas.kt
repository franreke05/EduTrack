package com.example.edutrack.dataclass

class Notas (
    var id: String?="", //Id para identificar la nota

    var id_asignatura: String?="", //Id de la asignatura a la que pertenece la nota

    val nombre: String?="", //Nombre de la asignatura

    val nota: Double?=0.0, //Nota obtenida en la asignatura

    val porcentaje : Double?=0.0 //Porcentaje de la nota del examen/trabajo realizado


)