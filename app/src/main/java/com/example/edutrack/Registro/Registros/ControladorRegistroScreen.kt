package com.example.edutrack.Registro.Registros

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import com.example.edutrack.Inicio.InicioActivity
import androidx.core.content.edit
import com.example.edutrack.ConectarBaseDatosConUnValor
import com.example.edutrack.datosUsuario

// Verifica si el correo tiene formato válido
// Registra un usuario en Firebase con email y password


@Composable
fun LoginUser(
    context: Context,
    email: String,
    password: String

) {
    ConectarBaseDatosConUnValor("Usuario")
    var sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    Log.d("Login pasado", true.toString())

    if (true) {
        var datos = datosUsuario
        Log.d("Login", datos.toString())
        for (dato in datos) {
            Log.d("Login", dato.email.toString())
            Log.d("Login", dato.password.toString())
            if (dato.email == email && dato.password == password) {
                sharedPreferences.edit {
                    putString("email", email)
                    putString("nombre", dato.nombre)
                    apply()
                }
                sharedPreferences.edit {
                    putBoolean("login",true)
                }
                val intent = Intent(context, InicioActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

}

