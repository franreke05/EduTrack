package com.example.edutrack.dataclass

class Asignatura(
    var id: String?="", //Id de la asignatura del año

    val nombre: String?="", //Nombre de la asignatura (lo pone el usuario)

    val creditos: Int?=0, //Creditos de la asignatura(lo pone el usuario)

    val descripcion: String?="", //Descripcion de la asignatura(lo pone el usuario)

    val lista_notas : MutableList<Notas>?= mutableListOf(), //Lista de todas las notas
    //que tendra la asignatura (cada una con su porcentaje)

    val media : Double?=0.0, //Media de la asignatura (Lo calcula una funcion a partir de
    //la lista de notas y sus porcentajes)

    val id_usuario: String?="", //Id del usuario al que pertenece la asignatura)

    val id_anio: String?="" //Id del año al que pertenece la asignatura

) : java.io.Serializable
