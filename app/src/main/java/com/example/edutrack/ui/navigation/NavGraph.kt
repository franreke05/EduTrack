package com.example.edutrack.ui.navigation

// EduNavRoot: Raíz de navegación con Navigation Drawer + NavHost
// Comentario: Implementa Navigation Compose y un Drawer al estilo Instagram.
// De forma progresiva iremos eliminando Intents en pantallas internas y usaremos navController.

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.edutrack.Inicio.CuerpoInicio
import com.example.edutrack.Perfil.CuerpoPerfil
import com.example.edutrack.Registro.Registros.LoginScreen
import com.example.edutrack.Registro.signup.SignUpScreen
import kotlinx.coroutines.launch

sealed class Dest(val route: String, val label: String) {
    data object Inicio: Dest("inicio", "Inicio")
    data object Perfil: Dest("perfil", "Perfil")
    data object Login: Dest("login", "Login")
    data object SignUp: Dest("signup", "Crear cuenta")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduNavRoot(startDestination: String) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Comentario: Si el Splash nos indicó una ruta inicial, navega ahí al iniciar.
    LaunchedEffect(startDestination) {
        navController.navigate(startDestination) {
            popUpTo(0) // limpiar back stack
            launchSingleTop = true
        }
    }

    val showDrawer by remember {
        // Comentario: Drawer visible solo en pantallas internas de app (inicio/perfil)
        mutableStateOf(true)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Comentario: Items del Drawer que navegan por rutas principales.
            NavigationDrawerItem(
                label = { Text(Dest.Inicio.label) },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Dest.Inicio.route) { launchSingleTop = true }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.surface,
                    unselectedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            NavigationDrawerItem(
                label = { Text(Dest.Perfil.label) },
                selected = false,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Dest.Perfil.route) { launchSingleTop = true }
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.surface,
                    unselectedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "EduTrack") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            // Comentario: Icono de menú para abrir el Drawer (placeholder)
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Dest.Login.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Dest.Login.route) {
                    // Comentario: Pantalla de Login existente. Más adelante, reemplazaremos intents internos.
                    LoginScreen()
                }
                composable(Dest.SignUp.route) {
                    SignUpScreen()
                }
                composable(Dest.Inicio.route) {
                    // Comentario: Pantalla de Inicio existente.
                    CuerpoInicio()
                }
                composable(Dest.Perfil.route) {
                    CuerpoPerfil()
                }
            }
        }
    }
}
