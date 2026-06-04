package com.example.edutrack.Perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // ── Lógica: subir foto a Firebase Storage y actualizar perfil ──────────
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
                        if (success) snackbarHostState.showSnackbar("Foto actualizada")
                        else snackbarHostState.showSnackbar("Error actualizando la foto")
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
                title = { Text("Mi perfil", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // ── Lógica: guardar cambios de nombre ──────────────────
                    IconButton(onClick = {
                        usuario?.id?.let { uid ->
                            val updates = mutableMapOf<String, Any>()
                            if (nombreEditable.value != usuario.nombre) {
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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

                    // ── Avatar hero ────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                            )
                                        )
                                    )
                                    .padding(vertical = 28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Avatar clickable
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clickable { photoPickerLauncher.launch("image/*") }
                                    ) {
                                        if (!usuario.photoUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = usuario.photoUrl,
                                                contentDescription = "Foto de perfil",
                                                modifier = Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isUploadingPhoto) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(36.dp),
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        strokeWidth = 2.5.dp
                                                    )
                                                } else {
                                                    Text(
                                                        text = (usuario.nombre ?: "U").take(1).uppercase(),
                                                        style = MaterialTheme.typography.headlineLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        }
                                        // Botón cámara overlay
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

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = usuario.nombre ?: "",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = usuario.email ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                                        )
                                    }

                                    // Badge plan
                                    val isPremium = userPlan == UserPlan.PREMIUM
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (isPremium) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(horizontal = 14.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                if (isPremium) Icons.Rounded.Star else Icons.Rounded.School,
                                                contentDescription = null,
                                                tint = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isPremium) "Premium" else "Plan Gratis",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isPremium) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // ── Información de la cuenta ───────────────────────────
                    item {
                        SectionLabel("Información de la cuenta")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
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
                                    value = usuario.email ?: "",
                                    onValueChange = {},
                                    label = { Text("Email") },
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp, bottom = 16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // ── Mis cursos ─────────────────────────────────────────
                    item {
                        SectionLabel("Mis cursos")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            if (anios.isEmpty()) {
                                Text(
                                    text = "No hay cursos registrados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    anios.forEachIndexed { index, anio ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Rounded.School,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                text = anio.nombre ?: "",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (index < anios.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // ── Mi plan ────────────────────────────────────────────
                    item {
                        SectionLabel("Mi plan")
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
                        Spacer(Modifier.height(28.dp))
                    }

                    // ── Acciones de cuenta ─────────────────────────────────
                    item {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                            )
                        ) {
                            Text("Eliminar cuenta", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
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
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }

        // ── Dialog: confirmar eliminación de cuenta ────────────────────────
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
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    )
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

    // ── Dialog: gestionar suscripción (redirige a Google Play) ────────────
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
        // ── Estado Premium ─────────────────────────────────────────────────
        val renewalText = premiumCache.expiresAt?.let {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES"))
            "Se renueva el ${sdf.format(java.util.Date(it))}"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Text(
                            text = "Premium activo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Premium",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (renewalText != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = renewalText,
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))
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
        // ── Estado Gratis ──────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
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
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Gratis",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Block, contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(14.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onPaywall,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Rounded.WorkspacePremium, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ver Premium", fontWeight = FontWeight.Bold, color = Color.White)
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
