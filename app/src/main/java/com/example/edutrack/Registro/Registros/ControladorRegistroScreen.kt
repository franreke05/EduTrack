package com.example.edutrack.Registro.Registros

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.db_ref
import com.google.firebase.Firebase
import com.google.firebase.database.database

// Verifica si el correo tiene formato válido
// Registra un usuario en Firebase con email y password



@Composable
fun buscarUsuario(email: String, password: String, function: () -> Unit): Boolean {
    db_ref = Firebase.database.reference
    var comprobacion = false
    LaunchedEffect(true) {
        Log.d("Login Buscando usuario","Buscando usuario")
        db_ref.child("Edutrack").child("Usuario").get().addOnSuccessListener { dataSnapshot ->
            for (snapshot in dataSnapshot.children) {
                val usuario = snapshot.getValue(Usuario::class.java)
                if (usuario != null && usuario.email == email && usuario.password == password) {
                    Log.d("Login Buscando usuario","Usuario encontrado")
                    function()
                    comprobacion = true

                }
            }

        }
    }
    return comprobacion
}




