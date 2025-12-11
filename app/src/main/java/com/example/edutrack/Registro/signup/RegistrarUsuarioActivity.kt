package com.example.edutrack.Registro.signup

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.R
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegistrarUsuarioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignUpScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
    @Deprecated("This method has been deprecated in favor of using the {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}. The OnBackPressedDispatcher controls how back button events are dispatched to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

private fun registerUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    username: String,
    context: Context,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.updateProfile(userProfileChangeRequest { displayName = username })
                persistUserRecord(user, username, email)
                sendVerificationEmail(user, context) {
                    setLoading(false)
                    (context as? Activity)?.finish()
                }
            } else {
                setLoading(false)
                val reason = task.exception?.localizedMessage ?: "intenta de nuevo"
                Toast.makeText(context, "No se pudo registrar: $reason", Toast.LENGTH_LONG).show()
            }
        }
}

private fun persistUserRecord(user: FirebaseUser?, username: String, email: String) {
    val uid = user?.uid ?: return
    val usuario = Usuario(
        id = uid,
        nombre = username,
        email = email,
        password = null
    )
    Firebase.database.reference
        .child("Edutrack")
        .child("Usuario")
        .child(uid)
        .setValue(usuario)
}

private fun sendVerificationEmail(user: FirebaseUser?, context: Context, onDone: () -> Unit) {
    val currentUser = user ?: run { onDone(); return }
    currentUser.sendEmailVerification()
        .addOnCompleteListener { task ->
            val message = if (task.isSuccessful) {
                "Te enviamos un correo de verificacion a ${currentUser.email}"
            } else {
                "Cuenta creada, pero no se pudo enviar el correo de verificacion."
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            onDone()
        }
}

private fun loginUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    context: Context,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            setLoading(false)
            if (task.isSuccessful) {
                Toast.makeText(context, "Sesion iniciada.", Toast.LENGTH_LONG).show()
                (context as? Activity)?.finish()
            } else {
                val reason = task.exception?.localizedMessage ?: "intenta de nuevo"
                Toast.makeText(context, "No se pudo iniciar sesion: $reason", Toast.LENGTH_LONG).show()
            }
        }
}

@Composable
fun SignUpScreen(
   modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginLoading by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // --- Contenedor principal ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(screenHeight * 0.04f))


        // Logo o título del login
        IconButton (onClick = {
            //Abrimos la galeria para que el usuario elija una imagen para su perfil si lo desea
            //Si no elige ninguna foto se le pondra una por defecto

        },modifier = Modifier.fillMaxWidth().height(screenHeight*0.3f)
        ){
            //asdad
            Image(
                painter = painterResource(id = R.drawable.agendita),
                contentDescription = "Logo",
                modifier = Modifier.shadow(8.dp, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(screenHeight*0.015f))

        // --- Caja estilo "list form" ---
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("Your Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                // username Input
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Your username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Your password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones separados
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (isLoading) return@Button
                    (context as? Activity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffff3b30))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_cancel_24),
                    contentDescription = "Cancel Icon",
                    modifier = Modifier.size(24.dp)
                )
            }

            Button(
                onClick = {
                    if (isLoading) return@Button
                    val emailTrimmed = email.trim()
                    val usernameTrimmed = username.trim()
                    val passwordTrimmed = password.trim()

                    when {
                        usernameTrimmed.isEmpty() -> {
                            Toast.makeText(context, "Introduce un nombre de usuario.", Toast.LENGTH_LONG).show()
                        }
                        emailTrimmed.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches() -> {
                            Toast.makeText(context, "Introduce un email valido.", Toast.LENGTH_LONG).show()
                        }
                        passwordTrimmed.length < 6 -> {
                            Toast.makeText(context, "La contrasena debe tener al menos 6 caracteres.", Toast.LENGTH_LONG).show()
                        }
                        else -> registerUser(
                            auth = auth,
                            email = emailTrimmed,
                            password = passwordTrimmed,
                            username = usernameTrimmed,
                            context = context,
                            setLoading = { isLoading = it }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff4cd964)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_check_24),
                        contentDescription = "Check Icon",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider(modifier = Modifier.fillMaxWidth(0.9f))

        Spacer(modifier = Modifier.height(16.dp))

        // Login manual compacto
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = loginEmail,
                    onValueChange = { loginEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Button(
                    onClick = {
                        if (loginLoading) return@Button
                        val mail = loginEmail.trim()
                        val pass = loginPassword.trim()
                        when {
                            mail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mail).matches() ->
                                Toast.makeText(context, "Email no valido.", Toast.LENGTH_LONG).show()
                            pass.isEmpty() ->
                                Toast.makeText(context, "Introduce tu contrasena.", Toast.LENGTH_LONG).show()
                            else -> loginUser(
                                auth = auth,
                                email = mail,
                                password = pass,
                                context = context,
                                setLoading = { loginLoading = it }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !loginLoading
                ) {
                    if (loginLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Footer estilo Framework7 ---
        Text(
            text = "Some text about login information.Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}
@Composable
@Preview
fun LoginScreenPreview() {
    EduTrackTheme {
        SignUpScreen()
    }
}
