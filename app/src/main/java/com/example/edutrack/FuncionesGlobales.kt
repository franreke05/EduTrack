package com.example.edutrack

import android.util.Log
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Curso
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
lateinit var db_ref: DatabaseReference

fun ConectarBaseDatos(primervalor: String? = "", segundovalor: String? = "") {
    val db_ref = Firebase.database.reference // Usa val si db_ref no se reasigna
    Log.d("Conectado", "Conectado")
    // No es necesario declarar 'datos' aquí arriba si su tipo depende de la lógica interna

    if (primervalor.isNullOrEmpty()) { // Forma más idiomática en Kotlin
        Log.d("No hay datos", "No hay datos")
        return // Es buena práctica retornar si no hay nada que hacer
    }

    if (segundovalor.isNullOrEmpty() && primervalor != "") {
        db_ref.child("Edutrack").child(primervalor).get().addOnSuccessListener { dataSnapshot ->
            if (primervalor == "Anio") {
                // Declara y asigna 'datosAnio' con el tipo específico aquí
                val datosAnio: MutableList<Anio> = mutableListOf()

                // Ahora debes poblar 'datosAnio' con los objetos Anio de Firebase
                // Suponiendo que cada hijo bajo el nodo "Anio" es un objeto Anio
                for (snapshot in dataSnapshot.children) {
                    val anio = snapshot.getValue(Anio::class.java)
                    anio?.let { datosAnio.add(it) } // Añade a la lista si no es null
                }

            }else if (primervalor == "Curso"){
                val datosCurso: MutableList<Curso> = mutableListOf()
                for (snapshot in dataSnapshot.children) {
                    val curso = snapshot.getValue(Curso::class.java)
                    curso?.let { datosCurso.add(it) } // Añade a la lista si no es null
                }
            }else if (primervalor == "Usuario"){
                Log.d("Conectado", "conectado22")
                val datosUsuario: MutableList<Usuario> = mutableListOf()
                for (snapshot in dataSnapshot.children) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let { datosUsuario.add(it) } // Añade a la lista si no es null
                }
            }else if (primervalor == "Asignatura"){
                val datosAsignatura: MutableList<Asignatura> = mutableListOf()
                for (snapshot in dataSnapshot.children) {
                    val asignatura = snapshot.getValue(Asignatura::class.java)
                    asignatura?.let { datosAsignatura.add(it) } // Añade a la lista si no es null
                }
            }else{
                Log.d("No hay datos2", "No hay datos2")
            }

            }
    }else{
            db_ref.child("Edutrack").child(primervalor).child(segundovalor.toString()).get().addOnSuccessListener { dataSnapshot ->
                if (primervalor == "Anio") {
                    // Declara y asigna 'datosAnio' con el tipo específico aquí
                    var datoAnio: String=""
                    //haremos un for para recorrer los nombres de los dates dentro para saber que quiere el usuario
                    for (snapshot in dataSnapshot.children) {
                        val anio = snapshot.getValue(Anio::class.java)
                        if (anio != null) {
                            if (anio.nombre == segundovalor) {
                                datoAnio=anio.nombre.toString()
                            }else if (anio.descripcion == segundovalor){
                                datoAnio=anio.descripcion.toString()
                            }else if (anio.fechaInicio == segundovalor){
                                datoAnio=anio.fechaInicio.toString()
                            }else if (anio.fechaFin == segundovalor) {
                                datoAnio = anio.fechaFin.toString()
                            }else if (anio.curso?.nombre == segundovalor){
                                datoAnio=anio.curso?.nombre.toString()
                            }
                        }

                    }

                } else if (primervalor == "Curso"){
                    val datosCurso: MutableList<Curso> = mutableListOf()
                    for (snapshot in dataSnapshot.children) {
                        val curso = snapshot.getValue(Curso::class.java)
                        curso?.let { datosCurso.add(it) } // Añade a la lista si no es null
                    }
                }else if (primervalor == "Usuario"){
                    var datosUsuario: String=""
                    for (snapshot in dataSnapshot.children) {
                        val usuario = snapshot.getValue(Usuario::class.java)
                        if (usuario != null) {
                            if(usuario.nombre == segundovalor) {
                                datosUsuario = usuario.nombre.toString()
                            }else if (usuario.anio.toString() == segundovalor){
                                datosUsuario = usuario.anio.toString()
                            }else if (usuario.password.toString() == segundovalor){
                                datosUsuario = usuario.password.toString()
                            }else if (usuario.email.toString() == segundovalor) {
                                datosUsuario = usuario.email.toString()
                            }
                        }
                    }

                }else if (primervalor == "Asignatura"){
                    var datosAsignatura: String=""
                    for (snapshot in dataSnapshot.children) {
                        val asignatura = snapshot.getValue(Asignatura::class.java)
                        if (asignatura != null) {
                            if(asignatura.nombre == segundovalor) {
                                datosAsignatura = asignatura.nombre.toString()
                            }else if (asignatura.creditos.toString() == segundovalor){
                                datosAsignatura = asignatura.creditos.toString()
                            }else if (asignatura.descripcion.toString() == segundovalor){
                                datosAsignatura = asignatura.descripcion.toString()
                            }
                        }
                    }
                }
            }
    }
}
fun CrearUsuario(
    usuario: Usuario,
){

    val db_ref = Firebase.database.reference
    usuario.id=db_ref.child("Edutrack").child("Usuario").child(usuario.id.toString()).key
    Log.d("Usuario222", usuario.id.toString())
    db_ref.child("Edutrack").child("Usuario").child(usuario.id.toString()).setValue(usuario)
}



fun CrearCurso(curso: Curso) {
    val db_ref = Firebase.database.reference
    curso.id=db_ref.child("Edutrack").child("Curso").child(curso.id.toString()).key
    db_ref.child("Edutrack").child("Curso").child(curso.id.toString()).setValue(curso)
}
fun CrearAnio(anio: Anio) {
    val db_ref = Firebase.database.reference
    anio.id=db_ref.child("Edutrack").child("Anio").child(anio.id.toString()).key
    db_ref.child("Edutrack").child("Anio").child(anio.id.toString()).setValue(anio)
}
fun CrearAsignatura(asignatura: Asignatura) {
    val db_ref = Firebase.database.reference
    asignatura.id=db_ref.child("Edutrack").child("Asignatura").child(asignatura.id.toString()).key
    db_ref.child("Edutrack").child("Asignatura").child(asignatura.id.toString()).setValue(asignatura)
}



