package com.example.edutrack.feature.tools

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.edutrack.data.reminders.ReminderRepository
import com.example.edutrack.dataclass.Reminder
import com.example.edutrack.ui.components.EdutrackButton
import com.example.edutrack.ui.components.EdutrackCard
import com.example.edutrack.ui.components.LimitReachedDialog
import com.example.edutrack.ui.components.ProfessionalTopBar
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun RemindersScreen(
    isPremium: Boolean,
    onBack: () -> Unit,
    onPremiumRequested: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ReminderRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Examen") }
    var days by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            snackbarHostState.showSnackbar(if (granted) "Permiso concedido" else "Activa las notificaciones en Ajustes")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProfessionalTopBar(
                title = "Recordatorios",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Nuevo recordatorio", style = MaterialTheme.typography.headlineSmall)
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = days,
                            onValueChange = { days = it.filter(Char::isDigit) },
                            label = { Text("Días desde hoy") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
                        EdutrackButton(
                            text = "Guardar recordatorio",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (!isPremium) {
                                    onPremiumRequested()
                                    return@EdutrackButton
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                val dueAt = System.currentTimeMillis() + (days.toLongOrNull() ?: 1L) * 24 * 60 * 60 * 1000L
                                repository.schedule(
                                    Reminder(
                                        id = UUID.randomUUID().toString(),
                                        title = title.ifBlank { "Recordatorio académico" },
                                        type = type.ifBlank { "Fecha importante" },
                                        dueAtMillis = dueAt,
                                        notes = notes
                                    )
                                )
                                scope.launch { snackbarHostState.showSnackbar("Recordatorio programado") }
                            }
                        )
                    }
                }
            }
        }
    }

    if (!isPremium) {
        LimitReachedDialog(
            title = "Recordatorios Premium",
            message = "Premium desbloquea recordatorios de exámenes, entregas y fechas importantes.",
            onDismiss = onBack,
            onUnlockPremium = onPremiumRequested
        )
    }
}
