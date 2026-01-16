package com.example.edutrack.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.net.Uri
import com.example.edutrack.Inicio.CuerpoInicio
import com.example.edutrack.Notas.NotasScreen
import com.example.edutrack.Perfil.CuerpoPerfil
import com.example.edutrack.Splash.SplashScreen
import com.example.edutrack.ui.theme.EduTrackTheme
import com.example.edutrack.data.clearSession
import com.example.edutrack.data.darkModeFlow
import com.example.edutrack.data.isLoggedFlow
import com.example.edutrack.data.setDarkMode
import com.example.edutrack.data.setSelectedAnio
import com.example.edutrack.data.setUserSession
import com.example.edutrack.data.userIdFlow
import com.example.edutrack.Registro.e.RegisteerScreen
import com.example.edutrack.Anio.AnioRoute
import com.example.edutrack.Inicio.CreacionAnioScreen
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect

// Maneja la navegacion principal y el estado de sesion.
class NavigationActivity : ComponentActivity() {
    // Configura el contenido Compose y las rutas de navegacion.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val isLogged by context.isLoggedFlow().collectAsState(initial = false)
            val userId by context.userIdFlow().collectAsState(initial = null)
            val isDarkMode by context.darkModeFlow().collectAsState(initial = false)

            // Restaura sesion si Firebase ya tiene un usuario autenticado.
            LaunchedEffect(Unit) {
                val current = Firebase.auth.currentUser
                if (current != null) {
                    context.setUserSession(current.uid)
                }
            }

            EduTrackTheme(darkTheme = isDarkMode) {
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(
                            onFinished = {
                                val hasUser = Firebase.auth.currentUser != null || isLogged
                                if (hasUser) navController.navigate("inicio") {
                                    popUpTo("splash") { inclusive = true }
                                } else navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("login") {
                        RegisteerScreen(
                            isLoadingOverride = null,
                            onAuthSuccess = { uid ->
                                scope.launch { context.setUserSession(uid) }
                                navController.navigate("inicio") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("inicio") {
                        CuerpoInicio(
                            userId = userId,
                            onAnioSelected = { anioId ->
                                scope.launch { context.setSelectedAnio(anioId ?: "") }
                                navController.navigate("anio/$anioId")
                            },
                            onCrearAnio = { navController.navigate("crearAnio") },
                            onPerfil = { navController.navigate("perfil") }
                        )
                    }
                    composable(
                        route = "anio/{anioId}",
                        arguments = listOf(navArgument("anioId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val anioId = backStackEntry.arguments?.getString("anioId")
                        AnioRoute(
                            userId = userId,
                            anioId = anioId,
                            onBack = { navController.popBackStack() },
                            onOpenNotas = { asignatura, anioActualId ->
                                val asignaturaId = Uri.encode(asignatura.id.orEmpty())
                                val asignaturaNombre = Uri.encode(asignatura.nombre.orEmpty())
                                val tipoPeriodo = Uri.encode(asignatura.tipo_periodo ?: "Trimestre")
                                val numeroPeriodos = asignatura.numero_periodos ?: 3
                                val encodedAnioId = Uri.encode(anioActualId.orEmpty())
                                navController.navigate(
                                    "notas/$asignaturaId/$asignaturaNombre/$tipoPeriodo/$numeroPeriodos?anioId=$encodedAnioId"
                                )
                            }
                        )
                    }
                    composable(
                        route = "notas/{asignaturaId}/{asignaturaNombre}/{tipoPeriodo}/{numeroPeriodos}?anioId={anioId}",
                        arguments = listOf(
                            navArgument("asignaturaId") { type = NavType.StringType },
                            navArgument("asignaturaNombre") { type = NavType.StringType },
                            navArgument("tipoPeriodo") { type = NavType.StringType },
                            navArgument("numeroPeriodos") { type = NavType.IntType },
                            navArgument("anioId") { type = NavType.StringType; defaultValue = "" },
                        )
                    ) { entry ->
                        val asignaturaId = Uri.decode(entry.arguments?.getString("asignaturaId").orEmpty())
                        val asignaturaNombre = Uri.decode(entry.arguments?.getString("asignaturaNombre").orEmpty())
                        val tipoPeriodo = Uri.decode(entry.arguments?.getString("tipoPeriodo").orEmpty())
                        val numeroPeriodos = entry.arguments?.getInt("numeroPeriodos") ?: 3
                        val anioId = entry.arguments?.getString("anioId")?.takeIf { it.isNotBlank() }
                        NotasScreen(
                            asignaturaId = asignaturaId,
                            asignaturaNombre = asignaturaNombre,
                            tipoPeriodo = tipoPeriodo,
                            numeroPeriodos = numeroPeriodos,
                            anioId = anioId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("perfil") {
                        CuerpoPerfil(
                            userId = userId,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = {
                                scope.launch { context.setDarkMode(!isDarkMode) }
                            },
                            onFinish = { navController.popBackStack() },
                            onLogout = {
                                scope.launch { context.clearSession() }
                                Firebase.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("crearAnio") {
                        CreacionAnioScreen(
                            userId = userId,
                            onFinish = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
