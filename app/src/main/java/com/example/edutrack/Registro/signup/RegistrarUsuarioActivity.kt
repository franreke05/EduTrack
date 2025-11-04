package com.example.edutrack.Registro.signup
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.edutrack.dataclass.Usuario
import com.example.edutrack.ui.theme.EduTrackTheme
import com.example.edutrack.R

import java.util.UUID
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.edutrack.CrearUsuario
import com.example.edutrack.Registro.Registros.LoginScreen
import com.example.edutrack.Registro.Registros.LoginUser
import com.example.edutrack.Registro.Registros.RegistroActivity
import com.example.edutrack.datosUsuario

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
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

@Composable
fun SignUpScreen(
   modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var listo by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // --- Contenedor principal ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // Fondo gris claro estilo F7
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navbar estilo Framework7
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.051f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))
                    )),
        ) {

                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                    ,
                    modifier = Modifier.align(Alignment.Center)
                )

        }
        Spacer(modifier = Modifier.height(screenHeight*0.0505f))


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

            )
        }
        Spacer(modifier = Modifier.height(screenHeight*0.015f))

        // --- Caja estilo "list form" ---
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(screenHeight*0.02f))
                .shadow(screenHeight*0.41f),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(screenHeight*0.01f),
                verticalArrangement = Arrangement.spacedBy(screenHeight*0.01f)
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
                    shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(12.dp)
                )

            }
        }

        Spacer(modifier = Modifier.height(screenHeight*0.05f))

        // --- Botones estilo F7 (fill + raised) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(screenHeight*0.01f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    listo = true
                },
                modifier = Modifier
                    .width(screenWidth*0.5f)
                    .height(screenHeight*0.06f)
                    .padding(start = screenHeight*0.06f)
                ,
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor =
                    (Color(0xff4cd964)))

            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_check_24),
                    contentDescription = "Check Icon",
                    modifier = Modifier.size(screenHeight*0.04f)
                )
            }

            if (listo) {
                CrearUsuario(usuario=Usuario(email=email,nombre=username, password = password))
            }

            Button(
                onClick = {
                    var intent = Intent(context, RegistroActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight*0.06f)
                    .padding(end = screenHeight*0.06f)
                ,
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor =
                    (Color(0xffff3b30)))

            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_cancel_24),
                    contentDescription = "Check Icon",
                    modifier = Modifier.size(screenHeight*0.04f)
                )
            }
        }

        Spacer(modifier = Modifier.height(screenHeight*0.286f))

        // --- Footer estilo Framework7 ---
        Text(
            text = "Some text about login information.\nLorem ipsum dolor sit amet, consectetur adipiscing elit.",
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
