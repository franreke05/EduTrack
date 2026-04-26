package com.example.edutrack.Perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.EditarUsuario
import com.example.edutrack.borrarUsuarioCompleto
import com.example.edutrack.data.profile.UserPreferencesRepository
import com.example.edutrack.dataclass.UserPreferences
import com.example.edutrack.ui.components.EdutrackButton
import com.example.edutrack.ui.components.EdutrackCard
import com.example.edutrack.ui.components.HeroPanel
import com.example.edutrack.ui.components.LimitReachedDialog
import com.example.edutrack.ui.components.PremiumBadge
import com.example.edutrack.ui.components.ProfessionalTopBar
import com.example.edutrack.ui.theme.EduTrackTheme
import kotlinx.coroutines.launch

@Composable
fun CuerpoPerfil(
    modifier: Modifier = Modifier,
    userId: String? = null,
    isPremium: Boolean = false,
    onFinish: () -> Unit = {},
    onLogout: () -> Unit = {},
    onPremium: () -> Unit = {},
    onGroups: () -> Unit = {},
    onStats: () -> Unit = {},
    onReminders: () -> Unit = {}
) {
    val context = LocalContext.current
    val usuario by rememberUsuarioState(userId)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val preferencesRepository = remember { UserPreferencesRepository() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var nombreEditable by remember { mutableStateOf("") }
    var accentColor by remember { mutableStateOf("blue") }
    var avatarStyle by remember { mutableStateOf("initials") }
    var advancedTheme by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuario?.id) {
        nombreEditable = usuario?.nombre.orEmpty()
        accentColor = usuario?.preferences?.accentColor ?: "blue"
        avatarStyle = usuario?.preferences?.avatarStyle ?: "initials"
        advancedTheme = usuario?.preferences?.advancedTheme
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProfessionalTopBar(
                title = "Perfil",
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val uid = usuario?.id ?: return@IconButton
                            val updates = mutableMapOf<String, Any>("nombre" to nombreEditable)
                            EditarUsuario(uid, updates) { success ->
                                scope.launch {
                                    if (success) {
                                        preferencesRepository.savePreferences(
                                            uid,
                                            UserPreferences(
                                                accentColor = accentColor,
                                                avatarStyle = avatarStyle,
                                                advancedTheme = advancedTheme
                                            )
                                        )
                                        snackbarHostState.showSnackbar("Perfil actualizado")
                                    } else {
                                        snackbarHostState.showSnackbar("No se pudo actualizar el perfil")
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        if (usuario == null) {
            Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HeroPanel(
                    title = usuario?.nombre.orEmpty().ifBlank { "Tu perfil" },
                    subtitle = usuario?.email.orEmpty().ifBlank { "Gestiona tu cuenta, estilo y accesos." },
                    icon = Icons.Default.Person,
                    trailing = { if (isPremium) PremiumBadge() }
                )
            }

            item {
                EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Cuenta", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = nombreEditable,
                            onValueChange = { nombreEditable = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = usuario?.email.orEmpty(),
                            onValueChange = {},
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            singleLine = true
                        )
                    }
                }
            }

            item {
                EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Palette, contentDescription = null)
                            Text("Personalización", style = MaterialTheme.typography.titleMedium)
                        }
                        Text("Color básico", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("blue", "green", "gold").forEach { color ->
                                AssistChip(
                                    onClick = { accentColor = color },
                                    label = { Text(if (accentColor == color) "${color.replaceFirstChar { it.uppercase() }} ✓" else color.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                        Text("Avatar básico", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("initials", "academic").forEach { avatar ->
                                AssistChip(
                                    onClick = { avatarStyle = avatar },
                                    label = { Text(if (avatar == "initials") "Iniciales" else "Académico") }
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Temas avanzados", style = MaterialTheme.typography.titleMedium)
                                Text("Portada, insignias y modo oscuro personalizado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!isPremium) PremiumBadge()
                        }
                        OutlinedButton(
                            onClick = {
                                if (isPremium) advancedTheme = "focus" else showPremiumDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (advancedTheme == "focus") "Tema Focus seleccionado" else "Usar tema Focus")
                        }
                    }
                }
            }

            item {
                EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Accesos", style = MaterialTheme.typography.titleMedium)
                        EdutrackButton("Premium", onClick = onPremium, icon = Icons.Default.WorkspacePremium, modifier = Modifier.fillMaxWidth())
                        EdutrackButton("Grupos", onClick = onGroups, icon = Icons.Default.Groups, modifier = Modifier.fillMaxWidth(), secondary = true)
                        EdutrackButton("Estadísticas", onClick = onStats, icon = Icons.Default.Analytics, modifier = Modifier.fillMaxWidth(), secondary = true)
                        EdutrackButton("Recordatorios", onClick = onReminders, icon = Icons.Default.Notifications, modifier = Modifier.fillMaxWidth(), secondary = true)
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text("Eliminar cuenta", modifier = Modifier.padding(start = 8.dp))
                    }
                    EdutrackButton("Cerrar sesión", onClick = onLogout, icon = Icons.Default.Logout, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (showPremiumDialog) {
        LimitReachedDialog(
            title = "Personalización Premium",
            message = "Los temas visuales, portada de perfil, insignias y modo oscuro personalizado se desbloquean con Premium.",
            onDismiss = { showPremiumDialog = false },
            onUnlockPremium = {
                showPremiumDialog = false
                onPremium()
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("Esta acción borra tus datos académicos y no se puede deshacer.") },
            confirmButton = {
                EdutrackButton(
                    text = "Eliminar",
                    onClick = {
                        usuario?.id?.let {
                            borrarUsuarioCompleto(context, it) {
                                showDeleteDialog = false
                                onLogout()
                            }
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilPreview() {
    EduTrackTheme {
        CuerpoPerfil()
    }
}
