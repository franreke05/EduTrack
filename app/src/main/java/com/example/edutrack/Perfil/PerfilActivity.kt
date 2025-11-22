package com.example.edutrack.Perfil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.edutrack.EditarUsuario
import com.example.edutrack.Registro.Registros.RegistroActivity
import com.example.edutrack.borrarUsuarioCompleto
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.ui.theme.EduTrackTheme
import kotlinx.coroutines.launch

class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EduTrackTheme {
                CuerpoPerfil(onFinish = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoPerfil(modifier: Modifier = Modifier, onFinish: () -> Unit = {}) {
    val context = LocalContext.current
    val usuario by rememberUsuarioState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Estados editables
    var nombreEditable by remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var passwordEditable by remember { mutableStateOf("") } // Inicia vacío por seguridad

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        usuario?.id?.let {
                            val updates = mutableMapOf<String, Any>()
                            if (nombreEditable != usuario?.nombre) {
                                updates["nombre"] = nombreEditable
                            }
                            // TODO: Añadir lógica para cambiar contraseña y email si es necesario

                            if (updates.isNotEmpty()) {
                                EditarUsuario(it, updates) { success ->
                                    coroutineScope.launch {
                                        if (success) {
                                            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                                        } else {
                                            snackbarHostState.showSnackbar("Error al actualizar el perfil")
                                        }
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sección de la foto de perfil
                item {
                    Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(screenHeight * 0.15f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))
                    Text(usuario?.nombre ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(usuario?.email ?: "", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                }

                // Sección de información personal
                item {
                    Text(
                        text = "Información de la Cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                    )
                }
                item {
                    Card(modifier = Modifier.padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.005f)) {
                        Column(modifier = Modifier.padding(vertical = screenHeight * 0.01f)) {
                            OutlinedTextField(
                                value = nombreEditable,
                                onValueChange = { nombreEditable = it },
                                label = { Text("Nombre") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                            )
                            OutlinedTextField(
                                value = usuario?.email ?: "",
                                onValueChange = { /* El email no es editable */ },
                                label = { Text("Email") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                            )
                            PasswordTextField(
                                value = passwordEditable,
                                onValueChange = { passwordEditable = it },
                                label = { Text("Nueva Contraseña (opcional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                            )
                        }
                    }
                }

                // Sección de años escolares
                item {
                    Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                    Text(
                        text = "Años Escolares",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                    )
                }
                val anios = usuario?.anio ?: emptyList()
                if (anios.isNotEmpty()) {
                    items(anios) {
                        Card(modifier = Modifier.padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.005f)) {
                            ListItem(
                                headlineContent = { Text(it.nombre ?: "") }
                            )
                        }
                    }
                } else {
                    item {
                        Text("No hay años escolares registrados.", modifier = Modifier.padding(screenWidth * 0.04f), color = Color.Gray)
                    }
                }

                // Sección de acciones de cuenta
                item {
                    Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.04f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar Cuenta")
                    }
                    Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                    Button(
                        onClick = {
                            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().clear().apply()

                            val intent = Intent(context, RegistroActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.04f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cerrar Sesión")
                    }
                    Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("¿Eliminar tu cuenta?") },
                text = { Text("Esta acción es permanente. Se borrarán todos tus datos, incluidos los años y asignaturas. ¿Estás seguro?") },
                confirmButton = {
                    Button(
                        onClick = {
                            usuario?.id?.let {
                                borrarUsuarioCompleto(context, it) {
                                    showDeleteDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Sí, eliminar todo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {
        CuerpoPerfil()
    }
}
