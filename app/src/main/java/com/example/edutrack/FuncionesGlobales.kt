package com.example.edutrack

import android.util.Log
import androidx.compose.runtime.Composable
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Curso
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

val datosUsuario: MutableList<Usuario> = mutableListOf()
val datosAnio: MutableList<Anio> = mutableListOf()
val datosCurso: MutableList<Curso> = mutableListOf()
val datosAsignatura: MutableList<Asignatura> = mutableListOf()
lateinit var db_ref: DatabaseReference
@Composable
fun ConectarBaseDatosConUnValor(primervalor: String) {

     db_ref = Firebase.database.reference // Usa val si db_ref no se reasigna
    Log.d("Login Conectado", "Conectado")

    // No es necesario declarar 'datos' aquí arriba si su tipo depende de la lógica interna
    if (primervalor.isEmpty()) { // Forma más idiomática en Kotlin
        Log.d("No hay datos", "No hay datos")
        return  // Es buena práctica retornar si no hay nada que hacer
    }
        //Esta parte tiene que ejecutarse primero
            db_ref.child("Edutrack").child(primervalor).get().addOnSuccessListener { dataSnapshot ->
                if (primervalor == "Anio") {
                    Log.d("Login Anio", "conectado22")
                    // Declara y asigna 'datosAnio' con el tipo específico aquí
                    // Ahora debes poblar 'datosAnio' con los objetos Anio de Firebase
                    // Suponiendo que cada hijo bajo el nodo "Anio" es un objeto Anio
                    for (snapshot in dataSnapshot.children) {
                        val anio = snapshot.getValue(Anio::class.java)
                        anio?.let { datosAnio.add(it) } // Añade a la lista si no es null
                    }
                } else if (primervalor == "Curso") {
                    Log.d("Login Curso", "conectado22")
                    for (snapshot in dataSnapshot.children) {
                        val curso = snapshot.getValue(Curso::class.java)
                        curso?.let { datosCurso.add(it) } // Añade a la lista si no es null
                    }
                } else if (primervalor == "Usuario") {
                    Log.d("Login Usuario", "conectado22")
                    for (snapshot in dataSnapshot.children) {
                        Log.d("Login Usuario", snapshot.toString())
                        val usuario = snapshot.getValue(Usuario::class.java)
                        usuario?.let { datosUsuario.add(it) } // Añade a la lista si no es null
                    }
                } else if (primervalor == "Asignatura") {
                    Log.d("Login Asignatura", "conectado22")
                    for (snapshot in dataSnapshot.children) {
                        val asignatura = snapshot.getValue(Asignatura::class.java)
                        asignatura?.let { datosAsignatura.add(it) } // Añade a la lista si no es null
                    }
                } else {
                    Log.d("No hay datos2", "No hay datos2")
                }
            }
}
fun CrearUsuario(
    usuario: Usuario,
){

    val db_ref = Firebase.database.reference
    if (usuario.id=="") {
       //le ponemos un id unico al usuario
        usuario.id=db_ref.push().key.toString()
    }
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



