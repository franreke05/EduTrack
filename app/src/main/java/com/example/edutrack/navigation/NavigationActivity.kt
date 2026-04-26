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
import com.example.edutrack.data.isPremiumFlow
import com.example.edutrack.data.rewardedSimulatorUntilFlow
import com.example.edutrack.data.setSelectedAnio
import com.example.edutrack.data.setUserSession
import com.example.edutrack.data.userIdFlow
import com.example.edutrack.Registro.e.RegisteerScreen
import com.example.edutrack.Anio.AnioRoute
import com.example.edutrack.Inicio.CreacionAnioScreen
import com.example.edutrack.feature.groups.CreateGroupScreen
import com.example.edutrack.feature.groups.GroupDetailScreen
import com.example.edutrack.feature.groups.GroupScreen
import com.example.edutrack.feature.premium.PaywallScreen
import com.example.edutrack.feature.tools.RemindersScreen
import com.example.edutrack.feature.tools.StatsScreen
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.edutrack.data.premium.BillingRepository

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
                val isPremium by context.isPremiumFlow().collectAsState(initial = false)
                val rewardedSimulatorUntil by context.rewardedSimulatorUntilFlow().collectAsState(initial = 0L)
                val hasSimulatorAccess = isPremium || rewardedSimulatorUntil > System.currentTimeMillis()
                val billingRepository = remember { BillingRepository(context.applicationContext) }

                // Restaura sesion si Firebase ya tiene un usuario autenticado.
                LaunchedEffect(Unit) {
                    val current = Firebase.auth.currentUser
                    if (current != null) {
                        context.setUserSession(current.uid)
                    }
                }

                LaunchedEffect(userId, isLogged) {
                    if (!userId.isNullOrBlank() || Firebase.auth.currentUser != null || isLogged) {
                        billingRepository.connectAndRefresh()
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
                            onPerfil = { navController.navigate("perfil") },
                            onGroups = { navController.navigate("groups") },
                            onStats = { navController.navigate("stats") },
                            isPremium = isPremium,
                            onPremiumRequested = { navController.navigate("paywall") }
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
                            isPremium = isPremium,
                            onBack = { navController.popBackStack() },
                            onPremiumRequested = { navController.navigate("paywall") },
                            onOpenNotas = { asignatura ->
                                navController.navigate(
                                    "notas/$anioId/${asignatura.id}/${asignatura.nombre}/${asignatura.tipo_periodo ?: "Trimestre"}/${asignatura.numero_periodos ?: 3}"
                                )
                            }
                        )
                    }
                    composable(
                        route = "notas/{yearId}/{asignaturaId}/{asignaturaNombre}/{tipoPeriodo}/{numeroPeriodos}",
                        arguments = listOf(
                            navArgument("yearId") { type = NavType.StringType },
                            navArgument("asignaturaId") { type = NavType.StringType },
                            navArgument("asignaturaNombre") { type = NavType.StringType },
                            navArgument("tipoPeriodo") { type = NavType.StringType },
                            navArgument("numeroPeriodos") { type = NavType.IntType },
                        )
                    ) { entry ->
                        val yearId = entry.arguments?.getString("yearId").orEmpty()
                        val asignaturaId = entry.arguments?.getString("asignaturaId").orEmpty()
                        val asignaturaNombre = entry.arguments?.getString("asignaturaNombre").orEmpty()
                        val tipoPeriodo = entry.arguments?.getString("tipoPeriodo").orEmpty()
                        val numeroPeriodos = entry.arguments?.getInt("numeroPeriodos") ?: 3
                        NotasScreen(
                            asignaturaId = asignaturaId,
                            yearId = yearId,
                            asignaturaNombre = asignaturaNombre,
                            tipoPeriodo = tipoPeriodo,
                            numeroPeriodos = numeroPeriodos,
                            isPremium = isPremium,
                            hasSimulatorAccess = hasSimulatorAccess,
                            onPremiumRequested = { navController.navigate("paywall") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("perfil") {
                        CuerpoPerfil(
                            userId = userId,
                            isPremium = isPremium,
                            onFinish = { navController.popBackStack() },
                            onPremium = { navController.navigate("paywall") },
                            onGroups = { navController.navigate("groups") },
                            onStats = { navController.navigate("stats") },
                            onReminders = { navController.navigate("reminders") },
                            onLogout = {
                                scope.launch {
                                    runCatching {
                                        CredentialManager.create(context)
                                            .clearCredentialState(ClearCredentialStateRequest())
                                    }
                                    context.clearSession()
                                }
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
                            isPremium = isPremium,
                            currentCourseCount = 0,
                            onPremiumRequested = { navController.navigate("paywall") },
                            onFinish = { navController.popBackStack() }
                        )
                    }
                    composable("paywall") {
                        PaywallScreen(onBack = { navController.popBackStack() })
                    }
                    composable("groups") {
                        GroupScreen(
                            userId = userId,
                            isPremium = isPremium,
                            onBack = { navController.popBackStack() },
                            onCreateGroup = { navController.navigate("createGroup") },
                            onOpenGroup = { groupId -> navController.navigate("group/$groupId") },
                            onPremiumRequested = { navController.navigate("paywall") }
                        )
                    }
                    composable("createGroup") {
                        CreateGroupScreen(
                            userId = userId,
                            isPremium = isPremium,
                            onBack = { navController.popBackStack() },
                            onCreated = { groupId ->
                                navController.navigate("group/$groupId") {
                                    popUpTo("groups")
                                }
                            },
                            onPremiumRequested = { navController.navigate("paywall") }
                        )
                    }
                    composable(
                        route = "group/{groupId}",
                        arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                    ) { entry ->
                        GroupDetailScreen(
                            groupId = entry.arguments?.getString("groupId"),
                            userId = userId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("stats") {
                        StatsScreen(
                            userId = userId,
                            isPremium = isPremium,
                            onBack = { navController.popBackStack() },
                            onPremiumRequested = { navController.navigate("paywall") }
                        )
                    }
                    composable("reminders") {
                        RemindersScreen(
                            isPremium = isPremium,
                            onBack = { navController.popBackStack() },
                            onPremiumRequested = { navController.navigate("paywall") }
                        )
                    }
                }
            }
        }
    }
}
