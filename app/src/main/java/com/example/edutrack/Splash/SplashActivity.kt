package com.example.edutrack.Splash

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.edutrack.utils.wrapWithLocale
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edutrack.R
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Activity que inicializa Firebase y muestra el splash.
class EduTrack : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(newBase.wrapWithLocale())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            EduTrackTheme {
                Scaffold { innerPadding ->
                    SplashScreen(Modifier.padding(innerPadding)) { }
                }
            }
        }
    }
}

// Splash con animacion secuencial: logo spring → nombre fade → tagline fade.
@Composable
fun SplashScreen(modifier: Modifier = Modifier, onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo entra con spring bounce y fade simultáneo
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(500))
        }

        // Nombre aparece cuando el logo está estabilizándose
        delay(450)
        launch { nameAlpha.animateTo(1f, animationSpec = tween(400)) }

        // Tagline aparece un poco después
        delay(250)
        taglineAlpha.animateTo(1f, animationSpec = tween(400))

        delay(900)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C3FA0),
                        Color(0xFF4361EE),
                        Color(0xFF5B6ABE)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.agendita),
                contentDescription = "Logo Edutrack",
                modifier = Modifier
                    .size(110.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Edutrack",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.alpha(nameAlpha.value)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Controla tu futuro académico",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.80f),
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
