package com.example.edutrack.Splash

import android.content.Context
import android.content.Intent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
// Comentario: Según petición del usuario, desde Splash navegamos por Intent
// directamente a Inicio o Registro, sin usar MainActivity.
import com.example.edutrack.Inicio.InicioActivity
import com.example.edutrack.R
import com.example.edutrack.Registro.Registros.RegistroActivity
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay

class EduTrack : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                Scaffold { innerPadding ->
                    SplashScreen(Modifier.Companion.padding(innerPadding))
                    FirebaseApp.initializeApp(this)
                }
            }
        }
    }
}

@Composable
fun SplashScreen(modifier: Modifier) {
    var context = LocalContext.current
    val scale = remember { Animatable(0.5f) }
    // Lanzar animación al iniciar

    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            )
        )
        delay(500)
        // Comentario: Si está logueado vamos a Inicio; si no, a Registro.
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val login = sharedPreferences.getBoolean("isLogged", false)
        val intent = if (login) {
            Intent(context, InicioActivity::class.java)
        } else {
            Intent(context, RegistroActivity::class.java)
        }
        context.startActivity(intent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.agendita), // Reemplaza con tu logo
            contentDescription = "Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
        )
    }

}