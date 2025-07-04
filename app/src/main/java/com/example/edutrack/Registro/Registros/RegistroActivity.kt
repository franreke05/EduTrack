package com.example.edutrack.Registro.Registros

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextFieldDefaults // Para acceder a outlinedTextFieldColors
import androidx.compose.material3.OutlinedTextField // Si estás usando OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.edutrack.ui.theme.EduTrackTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.example.edutrack.R
import com.example.edutrack.Registro.signup.RegistrarUsuarioActivity
import com.example.edutrack.dataclass.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                Scaffold { innerPadding ->
                    LoginScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // Guardamos el usuario de Firebase cuando se registre exitosamente
    var currentFirebaseUser by remember { mutableStateOf<FirebaseUser?>(null) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))
                )
            )
            .padding(horizontal = screenWidth * 0.005f, vertical = screenHeight * 0.01f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.8f)
                .clip(RoundedCornerShape(screenHeight * 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(screenHeight * 0.05f),
                modifier = Modifier.padding(screenHeight * 0.015f)
            ) {
                // Imagen circular (asegúrate de tener el recurso R.drawable.agendita)
                Image(
                    painter = painterResource(id = R.drawable.agendita),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(screenHeight * 0.345f)
                        .clip(CircleShape)
                )

                // Campo para ingresar el email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("email or username") },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.width(screenWidth * 0.7f),
                    colors = TextFieldDefaults.colors(
                        Color.White,
                    )

                    )

                // Campo para ingresar la contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("password") },
                    shape = RoundedCornerShape(50),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.width(screenWidth * 0.7f),
                    colors = TextFieldDefaults.colors(
                        Color.White,
                    )
                )

                // Fila de botones para LOGIN y SIGN UP
                Row(
                    horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.05f),
                    modifier = Modifier.width(screenWidth * 0.7f)
                ) {
                    // Botón LOGIN (mantiene acción pendiente)
                    Button(
                        onClick = {

                        } ,
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("LOGIN", color = Color.White)
                    }

                    // Botón SIGN UP
                    Button(
                        onClick = {
                            // Llamamos a registerUser pasándole email y password
                            val intent = Intent(context, RegistrarUsuarioActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("SIGN UP", color = Color.White)
                    }
                }

                // Botón para comprobar si el correo fue verificado

            }
        }
    }
}
