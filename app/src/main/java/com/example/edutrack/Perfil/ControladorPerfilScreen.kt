package com.example.edutrack.Perfil

import android.content.Context
import android.util.Log
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Un Composable que obtiene el ID del usuario de SharedPreferences, escucha los cambios
 * en los datos del usuario en Firebase en tiempo real y devuelve un State<Usuario?>.
 *
 * Para usarlo:
 * val usuario by rememberUsuarioState()
 */
@Composable
fun rememberUsuarioState(): State<Usuario?> {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE) }
    val userId = remember { sharedPreferences.getString("USER", null) }
    Log.d("FirebaseListener", "ID de usuario: $userId")

    val usuarioState = remember { mutableStateOf<Usuario?>(null) }

    DisposableEffect(userId) {

        if (userId.isNullOrEmpty()) {
            // Si no hay ID de usuario, nos aseguramos de que el estado sea nulo.
            usuarioState.value = null
            Log.w("FirebaseListener", "ID de usuario no encontrado en SharedPreferences.")
            onDispose {}
        } else {
            // Referencia a la ruta específica del usuario en Firebase.
            val userRef = Firebase.database.reference.child("Edutrack").child("Usuario").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Deserializa el snapshot a un objeto Usuario y actualiza el estado.
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuarioState.value = usuario
                    Log.d("FirebaseListener", "Datos del usuario $userId actualizados.")
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejo de errores.
                    Log.e("FirebaseListener", "Error al leer datos del usuario $userId.", error.toException())
                    usuarioState.value = null
                }
            }

            // Se añade el listener para escuchar los cambios en tiempo real.
            userRef.addValueEventListener(valueEventListener)

            // Cuando el Composable se va, se elimina el listener para no gastar recursos.
            onDispose {
                userRef.removeEventListener(valueEventListener)
            }
        }
    }

    // Devuelve el estado que se actualizará automáticamente.
    return usuarioState
}

/**
 * Un campo de texto para contraseñas con un icono para mostrar/ocultar el contenido.
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else
                Icons.Filled.VisibilityOff

            val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        }
    )
}
