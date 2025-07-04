package com.example.edutrack.Registro.Registros

import android.util.Log
import android.util.Patterns
import com.example.edutrack.ConectarBaseDatos
import com.example.edutrack.CrearUsuario
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// Verifica si el correo tiene formato válido
// Registra un usuario en Firebase con email y password

fun registerUser(
    usuario: Usuario
) {

    CrearUsuario(usuario)


}

