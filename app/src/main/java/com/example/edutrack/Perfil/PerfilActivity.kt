package com.example.edutrack.Perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.EditarUsuario
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.borrarUsuarioCompleto
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.domain.PremiumCache
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.domain.rememberPremiumCache
import com.example.edutrack.domain.rememberUserPlan
import com.example.edutrack.premiumCacheRef
import com.example.edutrack.ui.theme.EduTrackTheme
import kotlinx.coroutines.launch

// Pantalla de perfil con edicion basica y acciones de cuenta.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoPerfil(
    modifier: Modifier = Modifier,
    userId: String? = null,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onFinish: () -> Unit = {},
    onLogout: () -> Unit = {},
    onPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val usuario by rememberUsuarioState(userId)
    val anios by rememberAniosState(userId)
    val userPlan by rememberUserPlan(userId)
    val premiumCache by rememberPremiumCache(userId)
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var nombreEditable = remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var passwordEditable = remember { mutableStateOf("") }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null || userId == null) return@rememberLauncherForActivityResult
        isUploadingPhoto = true
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("avatars/$userId.jpg")
        storageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                storageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                com.example.edutrack.EditarUsuario(userId, mapOf("photoUrl" to downloadUri.toString())) { _ -> }
                isUploadingPhoto = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Foto actualizada") }
            }
            .addOnFailureListener {
                isUploadingPhoto = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Error subiendo la foto") }
            }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val horizontalPadding = 24.dp
    val verticalPadding = 16.dp

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
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
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (usuario == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(verticalPadding))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = MaterialTheme.shapes.extraLarge,
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                ) {
                                    if (!usuario?.photoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = usuario?.photoUrl,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier.size(72.dp).clip(CircleShape),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                if (isUploadingPhoto) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(28.dp),
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Text(
                                                        text = (usuario?.nombre ?: "U").take(1).uppercase(),
                                                        style = MaterialTheme.typography.headlineMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Botón cámara superpuesto
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isUploadingPhoto) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 1.5.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                contentDescription = "Cambiar foto",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        usuario?.nombre ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        usuario?.email ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(verticalPadding))
                    }

                    item {
                        Text(
                            text = "Información de la cuenta",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontalPadding / 2)) {
                                OutlinedTextField(
                                    value = nombreEditable.value,
                                    onValueChange = { nombreEditable.value = it },
                                    label = { Text("Nombre") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                )
                                OutlinedTextField(
                                    value = usuario?.email ?: "",
                                    onValueChange = { },
                                    label = { Text("Email") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                )
                                PasswordTextField(
                                    value = passwordEditable.value,
                                    onValueChange = { passwordEditable.value = it },
                                    label = { Text("Nueva Contraseña (opcional)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp, bottom = 16.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(verticalPadding))
                        Text(
                            text = "Mis cursos",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                    if (anios.isNotEmpty()) {
                        items(anios) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                ListItem(
                                    headlineContent = { Text(it.nombre ?: "") }
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                "No hay años escolares registrados.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(verticalPadding))
                        Text(
                            text = "Mi plan",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        MiPlanCard(
                            userPlan = userPlan,
                            premiumCache = premiumCache,
                            userId = userId,
                            onPaywall = onPaywall,
                            onCancelSubscription = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Suscripción cancelada")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(verticalPadding))
                    }

                    item {
                        OutlinedButton(
                            onClick = onToggleDarkMode,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (isDarkMode) "Cambiar a modo claro" else "Cambiar a modo oscuro"
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
                            )
                        ) {
                            Text("Eliminar Cuenta")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onLogout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Cerrar sesión",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(verticalPadding))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("¿Eliminar tu cuenta?") },
                text = {
                    Text("Esta acción es permanente. Se borrarán todos tus datos, incluidos los años y asignaturas. ¿Estás seguro?")
                },
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Si, eliminar todo")
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

@Composable
private fun MiPlanCard(
    userPlan: UserPlan,
    premiumCache: PremiumCache = PremiumCache(),
    userId: String? = null,
    onPaywall: () -> Unit,
    onCancelSubscription: () -> Unit = {}
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("¿Cancelar suscripción?") },
            text = { Text("Perderás acceso a todas las funciones Premium al final del período actual.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (userId != null) {
                            premiumCacheRef(userId).updateChildren(mapOf("isPremium" to false))
                        }
                        showCancelDialog = false
                        onCancelSubscription()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Cancelar suscripción") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Mantener Premium") }
            }
        )
    }

    if (userPlan == UserPlan.PREMIUM) {
        val renewalText = premiumCache.expiresAt?.let {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES"))
            "Se renueva el ${sdf.format(java.util.Date(it))}"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Premium activo ⭐",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Premium",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (renewalText != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = renewalText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Suscripción activa",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                val premiumFeatures = listOf(
                    "Sin anuncios", "Cursos ilimitados", "Simulador completo",
                    "Estadísticas avanzadas", "Recordatorios", "Exportación PDF",
                    "Crear y gestionar grupos"
                )
                premiumFeatures.forEach { feature ->
                    Text(
                        text = "• $feature",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar suscripción", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Plan actual: Gratis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Gratis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                val freeFeatures = listOf(
                    "Hasta 2 cursos",
                    "Notas ilimitadas",
                    "Cálculo básico para aprobar",
                    "1 grupo de estudio",
                    "Anuncios suaves"
                )
                freeFeatures.forEach { feature ->
                    Text(
                        text = "• $feature",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onPaywall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Ver Premium", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Vista previa de la pantalla de perfil.
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {
        CuerpoPerfil()
    }
}
