package com.example.edutrack.Registro.signup

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegistrarUsuarioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SignUpScreen()
                }
            }
        }
    }

    @Deprecated("Use OnBackPressedDispatcher instead.")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

@Composable
fun SignUpScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val email = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val loading = remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Regístrate con tu correo electrónico para continuar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("correo@ejemplo.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !loading.value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        label = { Text("Nombre") },
                        placeholder = { Text("Tu nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !loading.value
                    )

                    OutlinedTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = { Text("Contraseña") },
                        placeholder = { Text("Mínimo 6 caracteres") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !loading.value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    OutlinedTextField(
                        value = confirmPassword.value,
                        onValueChange = { confirmPassword.value = it },
                        label = { Text("Confirmar contraseña") },
                        placeholder = { Text("Repite tu contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !loading.value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Button(
                        onClick = {
                            if (loading.value) return@Button
                            registerWithEmail(
                                auth = auth,
                                email = email.value,
                                password = password.value,
                                confirmPassword = confirmPassword.value,
                                displayName = name.value,
                                context = context,
                                loading = loading
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F5824),
                            contentColor = Color.White
                        ),
                        enabled = !loading.value
                    ) {
                        if (loading.value) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text("Crear cuenta")
                        }
                    }

                    ClickableText(
                        text = buildAnnotatedString {
                            append("¿Ya tienes cuenta? ")
                            val start = length
                            append("Inicia sesión")
                            addStyle(
                                style = SpanStyle(
                                    color = Color(0xFF1F5824),
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.None
                                ),
                                start = start,
                                end = length
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (!loading.value && context is ComponentActivity) context.finish()
                        }
                    )
                }
            }
        }
    }
}

private fun registerWithEmail(
    auth: FirebaseAuth,
    email: String,
    password: String,
    confirmPassword: String,
    displayName: String,
    context: Context,
    loading: MutableState<Boolean>
) {
    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
        Toast.makeText(context, "Completa todos los campos.", Toast.LENGTH_LONG).show()
        return
    }
    if (password != confirmPassword) {
        Toast.makeText(context, "Las contraseñas no coinciden.", Toast.LENGTH_LONG).show()
        return
    }
    if (password.length < 6) {
        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_LONG).show()
        return
    }

    loading.value = true
    auth.createUserWithEmailAndPassword(email.trim(), password)
        .addOnCompleteListener { task ->
            loading.value = false
            if (task.isSuccessful) {
                val user = task.result?.user ?: auth.currentUser
                persistUserInDatabase(user, displayName, password, context) { saved ->
                    if (saved) {
                        sendVerificationEmail(user, context)
                        Toast.makeText(context, "Cuenta creada. Revisa tu correo para verificarla.", Toast.LENGTH_LONG).show()
                        if (context is ComponentActivity) context.finish()
                    } else {
                        Toast.makeText(context, "No se pudo guardar el usuario en la base de datos.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    task.exception?.localizedMessage ?: "No se pudo crear la cuenta.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}

private fun persistUserInDatabase(
    user: FirebaseUser?,
    displayName: String,
    password: String,
    context: Context,
    onComplete: (Boolean) -> Unit
) {
    val currentUser = user ?: run {
        Toast.makeText(context, "No se pudo obtener el usuario creado.", Toast.LENGTH_LONG).show()
        onComplete(false)
        return
    }
    val dbRef = Firebase.database.reference
    val userRef = dbRef.child("Edutrack").child("Usuario").child(currentUser.uid)
    val calculatedName = displayName.ifBlank { currentUser.email?.substringBefore("@") ?: "Usuario" }
    val usuario = Usuario(
        id = currentUser.uid,
        nombre = calculatedName,
        email = currentUser.email ?: "",
        password = password
    )
    userRef.setValue(usuario)
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener {
            Toast.makeText(context, "No se pudo guardar el usuario en la base de datos.", Toast.LENGTH_LONG).show()
            onComplete(false)
        }
}

private fun sendVerificationEmail(user: FirebaseUser?, context: Context) {
    val currentUser = user ?: return
    currentUser.sendEmailVerification()
        .addOnFailureListener {
            Toast.makeText(context, "No se pudo enviar el correo de verificación.", Toast.LENGTH_LONG).show()
        }
}

@Composable
@Preview(showBackground = true)
fun SignUpScreenPreview() {
    EduTrackTheme {
        SignUpScreen()
    }
}
