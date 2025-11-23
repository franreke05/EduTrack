package com.example.edutrack

// MainActivity: Nueva Activity contenedora para Navigation + Drawer (MVVM-ready)
// Comentario: Esta Activity sustituye la navegación basada en Intents por Navigation Compose.
// Mantiene Theme y sirve de punto único para NavHost + Drawer.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.edutrack.ui.navigation.EduNavRoot
import com.example.edutrack.ui.theme.EduTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Comentario: Se aplica el tema global con modo claro/oscuro.
            EduTrackTheme {
                // Comentario: Raíz de navegación con Drawer + NavHost.
                // Pasamos el destino inicial que nos envía el Splash (login/inicio).
                val startDestination = intent?.getStringExtra("startDestination") ?: "login"
                EduNavRoot(startDestination = startDestination)
            }
        }
    }
}
