package com.example.edutrack.Registro.Registros

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField // Si estás usando OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.edutrack.ui.theme.EduTrackTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.Inicio.InicioActivity
import com.example.edutrack.Registro.signup.RegistrarUsuarioActivity
import androidx.core.content.edit
import com.example.edutrack.dataclass.Usuario

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
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var listo by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // --- Contenedor principal ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Fondo adaptado al tema
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navbar estilo Framework7
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.051f)
                .background(
                    brush = com.example.edutrack.ui.theme.PrimaryGradient
                ),
        ) {

        }

        Spacer(modifier = Modifier.height(screenHeight*0.2f))

        // Logo o título del login
        Text(
            text = "EduTrack",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
                ,
            modifier = Modifier.padding(bottom = screenHeight*0.014f)
        )

        // --- Caja estilo "list form" ---
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(screenHeight*0.02f))
                .shadow(screenHeight*0.41f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(screenHeight*0.01f),
                verticalArrangement = Arrangement.spacedBy(screenHeight*0.01f)
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Username") },
                    placeholder = { Text("Your username") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
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
                    //que las letras se vean de color negro
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Botones estilo F7 (fill + raised) ---
        Column(
            verticalArrangement = Arrangement.spacedBy(screenHeight*0.01f),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Button(
                onClick = {
                    listo = !listo
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ,
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor =
                     MaterialTheme.colorScheme.primary)

            ) {
                Text("Sign In", color = MaterialTheme.colorScheme.onPrimary)
            }

            if (listo) {
                Log.d("Login buscado",buscarUsuario(email, password,{}).toString())
                if (buscarUsuario(email, password) {
                        var sharedPreferences =
                            context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit { putBoolean("isLogged", true).apply() }
                        sharedPreferences.edit { putString("user","").apply() }
                        val intent = Intent(context, InicioActivity::class.java)
                        context.startActivity(intent)

                    })
                {
                    listo = false
                }
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(context, RegistrarUsuarioActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight*0.06f),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(screenHeight*0.286f))

        // --- Footer estilo Framework7 ---
        Text(
            text = "Some text about login information.Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}
@Composable
@Preview
fun LoginScreenPreview() {
    EduTrackTheme {
        LoginScreen()
    }
}

