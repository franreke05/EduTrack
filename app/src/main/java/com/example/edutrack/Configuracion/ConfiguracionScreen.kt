package com.example.edutrack.Configuracion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edutrack.data.darkModeFlow
import com.example.edutrack.data.languageFlow
import com.example.edutrack.data.notificationsEnabledFlow
import com.example.edutrack.data.setDarkMode
import com.example.edutrack.data.setLanguage
import com.example.edutrack.data.setNotificationsEnabled
import com.example.edutrack.gestures.swipeBackGesture
import kotlinx.coroutines.launch

private const val SUPPORT_EMAIL = "franreke506@gmail.com"
private const val PLAY_STORE_PACKAGE_ID = "com.edutrack.app"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isDarkMode by context.darkModeFlow().collectAsState(initial = false)
    val language by context.languageFlow().collectAsState(initial = "es")
    val notificationsEnabled by context.notificationsEnabledFlow().collectAsState(initial = true)
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        }.getOrDefault("")
    }

    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = language,
            onSelect = { lang ->
                scope.launch { context.setLanguage(lang) }
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .swipeBackGesture(onBack),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    SectionHeader("Apariencia")
                    SettingsCard {
                        SettingToggleItem(
                            icon = Icons.Default.DarkMode,
                            title = "Modo oscuro",
                            subtitle = if (isDarkMode) "Activado" else "Desactivado",
                            checked = isDarkMode,
                            onToggle = { scope.launch { context.setDarkMode(!isDarkMode) } }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingItem(
                            icon = Icons.Default.Language,
                            title = "Idioma",
                            subtitle = if (language == "es") "Español" else "English",
                            onClick = { showLanguageDialog = true }
                        )
                    }
                }

                item {
                    SectionHeader("Notificaciones")
                    SettingsCard {
                        SettingToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "Notificaciones push",
                            subtitle = if (notificationsEnabled) "Activadas" else "Desactivadas",
                            checked = notificationsEnabled,
                            onToggle = { scope.launch { context.setNotificationsEnabled(!notificationsEnabled) } }
                        )
                    }
                }

                item {
                    SectionHeader("Privacidad y legal")
                    SettingsCard {
                        SettingItem(
                            icon = Icons.Default.Lock,
                            title = "Política de privacidad",
                            onClick = { context.openExternalUrl("https://edutrack.app/privacy") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingItem(
                            icon = Icons.Default.Gavel,
                            title = "Términos de servicio",
                            onClick = { context.openExternalUrl("https://edutrack.app/terms") }
                        )
                    }
                }

                item {
                    SectionHeader("Soporte")
                    SettingsCard {
                        SettingItem(
                            icon = Icons.Default.Email,
                            title = "Contactar soporte",
                            subtitle = SUPPORT_EMAIL,
                            onClick = { context.openSupportEmail() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingItem(
                            icon = Icons.Default.Star,
                            title = "Valorar la app",
                            onClick = { context.openPlayStoreListing() }
                        )
                    }
                }

                item {
                    SectionHeader("Acerca de")
                    SettingsCard {
                        SettingItem(
                            icon = Icons.Default.Info,
                            title = "Versión",
                            subtitle = versionName
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 6.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun LanguageDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "es" to "Español",
        "en" to "English"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Idioma") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { (code, label) ->
                    val isEnabled = code == "es"
                    val clickModifier = if (isEnabled) Modifier.clickable { onSelect(code) } else Modifier
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(clickModifier)
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = if (isEnabled) ({ onSelect(code) }) else null,
                            enabled = isEnabled
                        )
                        Column {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isEnabled)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                            if (!isEnabled) {
                                Text(
                                    text = "Próximamente",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

private fun Context.openExternalUrl(url: String) {
    startActivitySafely(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun Context.openSupportEmail() {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$SUPPORT_EMAIL")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "Soporte EduTrack")
    }
    startActivitySafely(Intent.createChooser(intent, "Contactar soporte"))
}

private fun Context.openPlayStoreListing() {
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PLAY_STORE_PACKAGE_ID"))
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$PLAY_STORE_PACKAGE_ID")
    )
    startActivitySafely(marketIntent, fallback = webIntent)
}

private fun Context.startActivitySafely(intent: Intent, fallback: Intent? = null) {
    val opened = runCatching {
        startActivity(intent)
        true
    }.getOrDefault(false)

    if (opened) return

    val fallbackOpened = fallback?.let {
        runCatching {
            startActivity(it)
            true
        }.getOrDefault(false)
    } ?: false

    if (!fallbackOpened) {
        Toast.makeText(this, "No hay una aplicación disponible", Toast.LENGTH_SHORT).show()
    }
}
