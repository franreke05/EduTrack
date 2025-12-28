package com.example.edutrack.Perfil

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// Mantiene el estado del usuario sincronizado con Firebase.
@Composable
fun rememberUsuarioState(userId: String?): State<Usuario?> {
    val usuarioState = remember { mutableStateOf<Usuario?>(null) }

    DisposableEffect(userId) {
        if (userId.isNullOrEmpty()) {
            usuarioState.value = null
            onDispose { }
        } else {
            val userRef = Firebase.database.reference.child("Edutrack").child("Usuario").child(userId)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usuarioState.value = snapshot.getValue(Usuario::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    usuarioState.value = null
                }
            }
            userRef.addValueEventListener(valueEventListener)
            onDispose { userRef.removeEventListener(valueEventListener) }
        }
    }
    return usuarioState
}

// Campo de texto para contrasena con alternancia de visibilidad.
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
