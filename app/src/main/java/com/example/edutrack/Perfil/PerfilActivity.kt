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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.EditarUsuario
import com.example.edutrack.ui.LocalAnios
import com.example.edutrack.ui.LocalUsuario
import com.example.edutrack.ui.LocalUserPlan
import com.example.edutrack.borrarUsuarioCompleto
import com.example.edutrack.domain.PremiumCache
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.ui.LocalPremiumCache
import com.example.edutrack.ui.theme.EduTrackTheme
import com.example.edutrack.gestures.swipeBackGesture
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoPerfil(
    modifier: Modifier = Modifier,
    userId: String? = null,
    onSettings: () -> Unit = {},
    onFinish: () -> Unit = {},
    onLogout: () -> Unit = {},
    onPaywall: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val usuario = LocalUsuario.current
    val anios = LocalAnios.current
    val userPlan = LocalUserPlan.current
    val premiumCache = LocalPremiumCache.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val nombreEditable = remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null || userId == null) return@rememberLauncherForActivityResult
        isUploadingPhoto = true
        val storageRef = FirebaseStorage.getInstance().reference.child("avatars/$userId.jpg")
        storageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                storageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                EditarUsuario(userId, mapOf("photoUrl" to downloadUri.toString())) { success ->
                    isUploadingPhoto = false
                    coroutineScope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("Foto actualizada")
                        } else {
                            snackbarHostState.showSnackbar("Error actualizando la foto")
                        }
                    }
                }
            }
            .addOnFailureListener {
                isUploadingPhoto = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Error subiendo la foto") }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        usuario?.id?.let { uid ->
                            val updates = mutableMapOf<String, Any>()
                            if (nombreEditable.value != usuario?.nombre) {
                                updates["nombre"] = nombreEditable.value
                            }
                            if (updates.isNotEmpty()) {
                                EditarUsuario(uid, updates) { success ->
                                    coroutineScope.launch {
                                        if (success) snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                                        else snackbarHostState.showSnackbar("Error al actualizar el perfil")
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .swipeBackGesture(onFinish),
            color = MaterialTheme.colorScheme.background
        ) {
            if (usuario == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar card centrado
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = MaterialTheme.shapes.extraLarge,
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clickable { photoPickerLauncher.launch("image/*") }
                                ) {
                                    if (!usuario?.photoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = usuario?.photoUrl,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clip(CircleShape),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (isUploadingPhoto) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(36.dp),
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        strokeWidth = 2.5.dp
                                                    )
                                                } else {
                                                    Text(
                                                        text = (usuario?.nombre ?: "U").take(1).uppercase(),
                                                        style = MaterialTheme.typography.headlineLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isUploadingPhoto) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 1.5.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Default.CameraAlt,
                                                contentDescription = "Cambiar foto",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = usuario?.nombre ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = usuario?.email ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Información de la cuenta
                    item {
                        Text(
                            text = "Información de la cuenta",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 6.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                                    onValueChange = {},
                                    label = { Text("Email") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp, bottom = 16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Mis cursos
                    item {
                        Text(
                            text = "Mis cursos",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 6.dp)
                        )
                        if (anios.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Text(
                                    text = "No hay años escolares registrados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                anios.forEachIndexed { index, anio ->
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = anio.nombre ?: "",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                    if (index < anios.lastIndex) {
                                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Mi plan
                    item {
                        Text(
                            text = "Mi plan",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 6.dp)
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
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Acciones de cuenta
                    item {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
                            )
                        ) {
                            Text("Eliminar cuenta")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onLogout,
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
                        Spacer(modifier = Modifier.height(24.dp))
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
            title = { Text("Gestionar suscripción") },
            text = { Text("Las suscripciones se gestionan directamente desde Google Play. Abre la aplicación de Google Play para cancelar o cambiar tu plan.") },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Abrir Google Play Manage Subscriptions
                        // Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Abrir Google Play") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Cerrar") }
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Premium activo",
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

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = renewalText ?: "Suscripción activa",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {
        CuerpoPerfil()
    }
}
