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
import com.example.edutrack.Inicio.CuerpoInicio
import com.example.edutrack.Notas.NotasScreen
import com.example.edutrack.Perfil.CuerpoPerfil
import com.example.edutrack.Splash.SplashScreen
import com.example.edutrack.ui.theme.EduTrackTheme
import com.example.edutrack.data.clearSession
import com.example.edutrack.data.isLoggedFlow
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
            EduTrackTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val isLogged by context.isLoggedFlow().collectAsState(initial = false)
                val userId by context.userIdFlow().collectAsState(initial = null)

                // Restaura sesion si Firebase ya tiene un usuario autenticado.
                LaunchedEffect(Unit) {
                    val current = Firebase.auth.currentUser
                    if (current != null) {
                        context.setUserSession(current.uid)
                    }
                }

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
                            onOpenNotas = { asignatura ->
                                navController.navigate(
                                    "notas/${asignatura.id}/${asignatura.nombre}/${asignatura.tipo_periodo ?: "Trimestre"}/${asignatura.numero_periodos ?: 3}"
                                )
                            }
                        )
                    }
                    composable(
                        route = "notas/{asignaturaId}/{asignaturaNombre}/{tipoPeriodo}/{numeroPeriodos}",
                        arguments = listOf(
                            navArgument("asignaturaId") { type = NavType.StringType },
                            navArgument("asignaturaNombre") { type = NavType.StringType },
                            navArgument("tipoPeriodo") { type = NavType.StringType },
                            navArgument("numeroPeriodos") { type = NavType.IntType },
                        )
                    ) { entry ->
                        val asignaturaId = entry.arguments?.getString("asignaturaId").orEmpty()
                        val asignaturaNombre = entry.arguments?.getString("asignaturaNombre").orEmpty()
                        val tipoPeriodo = entry.arguments?.getString("tipoPeriodo").orEmpty()
                        val numeroPeriodos = entry.arguments?.getInt("numeroPeriodos") ?: 3
                        NotasScreen(
                            asignaturaId = asignaturaId,
                            asignaturaNombre = asignaturaNombre,
                            tipoPeriodo = tipoPeriodo,
                            numeroPeriodos = numeroPeriodos,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("perfil") {
                        CuerpoPerfil(
                            userId = userId,
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
