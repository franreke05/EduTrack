package com.example.edutrack

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

lateinit var db_ref: DatabaseReference




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
fun AgregarNota_Asignatura(asignatura: Asignatura, nota: Notas){
    val db_ref = Firebase.database.reference
    db_ref.child("Edutrack").child("Asignatura").child(asignatura.id.toString()).child("Notas").child(nota.id.toString()).setValue(nota)
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



