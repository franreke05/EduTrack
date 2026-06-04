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
import com.example.edutrack.data.setSelectedAnio
import com.example.edutrack.data.setUserSession
import com.example.edutrack.data.userIdFlow
import com.example.edutrack.Configuracion.ConfiguracionScreen
import com.example.edutrack.Groups.CrearGrupoScreen
import com.example.edutrack.Groups.GrupoDetalleScreen
import com.example.edutrack.Groups.GruposScreen
import com.example.edutrack.Groups.UnirseGrupoScreen
import com.example.edutrack.Registro.e.RegisteerScreen
import com.example.edutrack.Anio.AnioRoute
import com.example.edutrack.Inicio.CreacionAnioScreen
import com.example.edutrack.Onboarding.OnboardingScreen
import com.example.edutrack.Simulador.SimuladorScreen
import com.example.edutrack.Premium.PaywallScreen
import com.example.edutrack.data.isOnboardedFlow
import com.example.edutrack.data.setOnboarded
import kotlinx.coroutines.launch
import com.example.edutrack.DB_URL
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import com.example.edutrack.Groups.rememberUserGroupsState
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.Perfil.rememberUsuarioState
import com.example.edutrack.domain.rememberPremiumCache
import com.example.edutrack.domain.rememberUserPlan
import com.example.edutrack.ui.LocalAnios
import com.example.edutrack.ui.LocalPremiumCache
import com.example.edutrack.ui.LocalUserGroups
import com.example.edutrack.ui.LocalUsuario
import com.example.edutrack.ui.LocalUserPlan

// Maneja la navegacion principal y el estado de sesion.
class NavigationActivity : ComponentActivity() {
    // Configura el contenido Compose y las rutas de navegacion.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Debe llamarse antes de cualquier uso de FirebaseDatabase para activar caché en disco.
        FirebaseDatabase.getInstance(DB_URL).setPersistenceEnabled(true)
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val isLogged by context.isLoggedFlow().collectAsState(initial = false)
            val userIdFromStore by context.userIdFlow().collectAsState(initial = null)
            val isDarkMode by context.darkModeFlow().collectAsState(initial = false)
            val isOnboarded by context.isOnboardedFlow().collectAsState(initial = true)

            // Usa el uid de Firebase Auth directamente para evitar race conditions con DataStore.
            val userId = userIdFromStore ?: Firebase.auth.currentUser?.uid

            // Restaura sesion si Firebase ya tiene un usuario autenticado.
            LaunchedEffect(Unit) {
                val current = Firebase.auth.currentUser
                if (current != null) {
                    context.setUserSession(current.uid)
                }
            }

            // Mantiene los nodos más leídos sincronizados en caché local.
            LaunchedEffect(userId) {
                val uid = userId ?: return@LaunchedEffect
                val root = FirebaseDatabase.getInstance(DB_URL).reference.child("Edutrack")
                root.child("users/$uid/anios").keepSynced(true)
                root.child("users/$uid/premiumCache").keepSynced(true)
                root.child("users/$uid/profile").keepSynced(true)
                root.child("userGroups/$uid").keepSynced(true)
            }

            val userPlan by rememberUserPlan(userId)
            val anios by rememberAniosState(userId)
            val usuario by rememberUsuarioState(userId)
            val premiumCache by rememberPremiumCache(userId)
            val userGroups by rememberUserGroupsState(userId)

            EduTrackTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(
                    LocalUserPlan provides userPlan,
                    LocalAnios provides anios,
                    LocalUsuario provides usuario,
                    LocalPremiumCache provides premiumCache,
                    LocalUserGroups provides userGroups,
                ) {
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
                                val destination = if (isOnboarded) "inicio" else "onboarding"
                                navController.navigate(destination) {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("onboarding") {
                        OnboardingScreen(
                            onFinish = {
                                scope.launch { context.setOnboarded() }
                                navController.navigate("inicio") {
                                    popUpTo("onboarding") { inclusive = true }
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
                            onSimulador = { navController.navigate("simulador") },
                            onPaywall = { navController.navigate("paywall") },
                            onGrupos = { navController.navigate("grupos") }
                        )
                    }
                    composable("simulador") {
                        SimuladorScreen(
                            userId = userId,
                            onBack = { navController.popBackStack() },
                            onPaywall = { navController.navigate("paywall") }
                        )
                    }
                    composable("paywall") {
                        PaywallScreen(
                            userId = userId,
                            onDismiss = { navController.popBackStack() },
                            onPurchase = { navController.popBackStack() },
                            onRestorePurchase = { navController.popBackStack() }
                        )
                    }
                    composable("grupos") {
                        GruposScreen(
                            userId = userId,
                            onBack = { navController.popBackStack() },
                            onPaywall = { navController.navigate("paywall") },
                            onCrearGrupo = { navController.navigate("crearGrupo") },
                            onUnirseGrupo = { navController.navigate("unirseGrupo") },
                            onGrupoDetalle = { groupId -> navController.navigate("grupoDetalle/$groupId") }
                        )
                    }
                    composable("crearGrupo") {
                        CrearGrupoScreen(
                            userId = userId,
                            onBack = { navController.popBackStack() },
                            onPaywall = { navController.navigate("paywall") },
                            onGroupCreated = { groupId ->
                                navController.navigate("grupoDetalle/$groupId") {
                                    popUpTo("grupos")
                                }
                            }
                        )
                    }
                    composable("unirseGrupo") {
                        UnirseGrupoScreen(
                            userId = userId,
                            onBack = { navController.popBackStack() },
                            onPaywall = { navController.navigate("paywall") },
                            onGroupJoined = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "grupoDetalle/{groupId}",
                        arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                        GrupoDetalleScreen(
                            userId = userId,
                            groupId = groupId,
                            onBack = { navController.popBackStack() },
                            onPaywall = { navController.navigate("paywall") }
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
                            onPaywall = { navController.navigate("paywall") },
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
                            userId = userId ?: "",
                            notaMinima = 5.0,
                            onBack = { navController.popBackStack() },
                            onPaywall = { navController.navigate("paywall") }
                        )
                    }
                    composable("perfil") {
                        CuerpoPerfil(
                            userId = userId,
                            onSettings = { navController.navigate("configuracion") },
                            onFinish = { navController.popBackStack() },
                            onLogout = {
                                scope.launch {
                                    context.clearSession()
                                    Firebase.auth.signOut()
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onPaywall = { navController.navigate("paywall") }
                        )
                    }
                    composable("configuracion") {
                        ConfiguracionScreen(
                            onBack = { navController.popBackStack() }
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
}
