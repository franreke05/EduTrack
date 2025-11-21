package com.example.edutrack.Registro.Registros

//El contenido del archivo esta comentado (si se agrega alguna otra funcion cambiar el check )❌

//Cambios para el registro
/*
1. Codificar la contraseña para guardar en la base de datos
2. Hacer una funcion privada para que descodifique la contraseña (Hacerlo mediante token)
3. Que la app pueda enviar correos de verificacion a gmail

 */

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.db_ref
import com.google.firebase.Firebase
import com.google.firebase.database.database
import androidx.core.content.edit

// Verifica si el correo tiene formato válido
// Registra un usuario en Firebase con email y password



@Composable
fun buscarUsuario(email: String, password: String, function: () -> Unit): Boolean {
    db_ref = Firebase.database.reference
    val context = LocalContext.current
    var sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    var comprobacion = false
    LaunchedEffect(true) {
        Log.d("Login Buscando usuario","Buscando usuario")
        db_ref.child("Edutrack").child("Usuario").get().addOnSuccessListener { dataSnapshot ->
            for (snapshot in dataSnapshot.children) {
                val usuario = snapshot.getValue(Usuario::class.java)
                if (usuario != null && usuario.email == email && usuario.password == password) {
                    Log.d("Login Buscando usuario","Usuario encontrado")
                    sharedPreferences.edit { putString("USER", usuario.id).apply() }
                    Log.d("Login Buscando usuario f",sharedPreferences.getString("USER", "").toString())
                    function()
                    comprobacion = true

                }
            }

        }
    }
    return comprobacion
}




