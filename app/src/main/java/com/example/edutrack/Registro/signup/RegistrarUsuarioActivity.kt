package com.example.edutrack.Registro.signup
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
import androidx.compose.ui.Alignment
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
import com.example.edutrack.Registro.Registros.registerUser
import java.util.UUID
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.edutrack.Registro.Registros.RegistroActivity

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
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

@Composable
fun SignUpScreen(
   modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var foto by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            ,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .height(screenWidth * 0.9f)
                .align(Alignment.Center),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF4AD9E2), Color(0xFF195FCF))
                        )
                    )
                    .padding(screenWidth * 0.05f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Imagen del logo
                    Image(
                        painter = painterResource(id = R.drawable.agendita),
                        contentDescription = "Logo",
                        modifier = Modifier.size(screenWidth * 0.2f)
                    )

                    // Campos de registro
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        placeholder = { Text("Nombre") },
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            Color.Black
                        ),
                        modifier =Modifier.width(screenWidth * 0.7f),
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            Color.Black
                        ),
                        modifier = Modifier.width(screenWidth * 0.7f),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            Color.Black
                        ),
                        modifier = Modifier.width(screenWidth * 0.7f),
                    )

                    val context = LocalContext.current
                    // Botón Sign Up
                   Row(modifier = Modifier.fillMaxWidth().height(screenWidth * 0.15f).padding(top = screenWidth*0.02f)) {
                       Button(

                           onClick = {
                               var usuario = Usuario(
                                   id = UUID.randomUUID().toString(),
                                   nombre = nombre,
                                   email = email,
                                   password = password,
                                   anio = mutableListOf()
                               )
                               Log.d("Usuario22", usuario.id.toString())
                               Log.d("Usuario22", usuario.nombre.toString())
                               Log.d("Usuario22", usuario.email.toString())
                               Log.d("Usuario22", usuario.password.toString())
                               Log.d("Usuario22", usuario.anio.toString())
                               registerUser(usuario)

                               val intent = Intent(context, RegistroActivity::class.java)
                               context.startActivity(intent)
                           },
                           shape = RoundedCornerShape(50),
                           border = BorderStroke(1.dp, Color.White),
                           colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                           modifier = Modifier
                               .weight(0.25f)
                               .height(screenWidth * 0.05f)
                       ) {
                           Text("SIGN UP", color = Color.White)
                       }
                       Button(

                           onClick = {
                               val intent = Intent(context, RegistroActivity::class.java)
                               context.startActivity(intent)
                           },
                           shape = RoundedCornerShape(50),
                           border = BorderStroke(1.dp, Color.White),
                           colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                           modifier = Modifier
                               .weight(0.25f)
                               .height(screenWidth * 0.05f)
                       ) {
                           Text("Cancelar", color = Color.White)
                       }
                   }
                }
            }
        }
    }
}
