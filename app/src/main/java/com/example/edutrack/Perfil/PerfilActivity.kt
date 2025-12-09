package com.example.edutrack.Perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.EditarUsuario
import com.example.edutrack.borrarUsuarioCompleto
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.ui.theme.EduTrackTheme
import kotlinx.coroutines.launch

@Composable
fun CuerpoPerfil(
    modifier: Modifier = Modifier,
    userId: String? = null,
    onFinish: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val usuario by rememberUsuarioState(userId)
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var nombreEditable = remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var passwordEditable = remember { mutableStateOf("") }

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
                            if (nombreEditable.value != usuario?.nombre) {
                                updates["nombre"] = nombreEditable.value
                            }
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
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                value = nombreEditable.value,
                                onValueChange = { nombreEditable.value = it },
                                label = { Text("Nombre") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                            )
                            OutlinedTextField(
                                value = usuario?.email ?: "",
                                onValueChange = { },
                                label = { Text("Email") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                            )
                            PasswordTextField(
                                value = passwordEditable.value,
                                onValueChange = { passwordEditable.value = it },
                                label = { Text("Nueva Contraseña (opcional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f)
                            )
                        }
                    }
                }

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

                item {
                    Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                    androidx.compose.material3.OutlinedButton(
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
                            onLogout()
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
                                    onLogout()
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
