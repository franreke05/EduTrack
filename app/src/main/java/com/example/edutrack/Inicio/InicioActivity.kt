package com.example.edutrack.Inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edutrack.Premium.UpgradeSheet
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.domain.rememberUserPlan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

// Paleta de colores para anillos de stories (Instagram-style).
private val storyRingColors = listOf(
    listOf(Color(0xFFE040FB), Color(0xFF7C4DFF)),
    listOf(Color(0xFF4361EE), Color(0xFF00B0FF)),
    listOf(Color(0xFF2AA26E), Color(0xFF00E5FF)),
    listOf(Color(0xFFFF6D00), Color(0xFFFFD740)),
    listOf(Color(0xFFE53935), Color(0xFFFF6D00)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoInicio(
    modifier: Modifier = Modifier,
    userId: String?,
    onAnioSelected: (String?) -> Unit = {},
    onCrearAnio: () -> Unit = {},
    onPerfil: () -> Unit = {},
    onSimulador: () -> Unit = {},
    onPaywall: () -> Unit = {},
    onGrupos: () -> Unit = {}
) {
    val aniosFromFirebase by rememberAniosState(userId)
    val userPlan by rememberUserPlan(userId)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var anioToDelete by remember { mutableStateOf<Anio?>(null) }
    var showMenuFor by remember { mutableStateOf<String?>(null) }
    var showCourseUpgrade by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            val guardedCrearAnio = {
                if (!PlanManager.canCreateCourse(userPlan, aniosFromFirebase.size)) {
                    showCourseUpgrade = true
                } else {
                    onCrearAnio()
                }
            }

            item {
                InstagramTopBar(
                    onPerfil = onPerfil,
                    onGrupos = onGrupos,
                    isPremium = userPlan == com.example.edutrack.domain.UserPlan.PREMIUM
                )
            }

            item {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                StoriesRow(
                    anios = aniosFromFirebase,
                    onCrearAnio = guardedCrearAnio,
                    onAnioNavigate = { anio -> onAnioSelected(anio.id) }
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (aniosFromFirebase.isEmpty()) {
                item {
                    EmptyFeedState(onCrearAnio = guardedCrearAnio)
                }
            } else {
                item {
                    SimuladorFeedCard(onSimulador = onSimulador)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(
                    aniosFromFirebase,
                    key = { _, anio -> anio.id ?: anio.hashCode().toString() }
                ) { index, anio ->
                    AnioFeedCard(
                        anio = anio,
                        index = index + 1,
                        showMenu = showMenuFor == anio.id,
                        onMenuToggle = {
                            showMenuFor = if (showMenuFor == anio.id) null else anio.id
                        },
                        onMenuDismiss = { showMenuFor = null },
                        onOpen = { onAnioSelected(anio.id) },
                        onSimulador = onSimulador,
                        onDelete = {
                            anioToDelete = anio
                            showDeleteDialog = true
                            showMenuFor = null
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar el año '${anioToDelete?.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        anioToDelete?.id?.let { anioId ->
                            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@let
                            if (anioId.isNotEmpty()) {
                                com.example.edutrack.anioRef(uid, anioId).removeValue()
                            }
                        }
                        showDeleteDialog = false
                        anioToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showCourseUpgrade) {
        UpgradeSheet(
            title = "Has llegado al límite del plan gratis",
            message = "Con Edutrack gratis puedes crear hasta 2 cursos. Premium desbloquea cursos ilimitados y el simulador completo.",
            onUpgrade = onPaywall,
            onDismiss = { showCourseUpgrade = false }
        )
    }
}

@Composable
private fun InstagramTopBar(
    onPerfil: () -> Unit,
    onGrupos: () -> Unit = {},
    isPremium: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Edutrack",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (isPremium) {
                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        IconButton(onClick = onGrupos) {
            Icon(Icons.Default.Group, contentDescription = "Grupos", tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = onPerfil) {
            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun StoriesRow(
    anios: List<Anio>,
    onCrearAnio: () -> Unit,
    onAnioNavigate: (Anio) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StoryItem(
                label = "Nueva",
                isNew = true,
                index = 0,
                media = null,
                onClick = onCrearAnio
            )
        }
        itemsIndexed(anios) { index, anio ->
            val media = calcularMediaAnio(anio)
            StoryItem(
                label = anio.nombre?.take(10) ?: "Curso",
                isNew = false,
                index = index,
                media = media,
                onClick = { onAnioNavigate(anio) }
            )
        }
    }
}

@Composable
private fun StoryItem(
    label: String,
    isNew: Boolean,
    index: Int,
    media: Double?,
    onClick: () -> Unit
) {
    val ringColors = storyRingColors[index % storyRingColors.size]
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            if (isNew) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(ringColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = toRoman(index + 1),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                media?.let {
                                    Text(
                                        text = String.format("%.1f", it),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnioFeedCard(
    anio: Anio,
    index: Int,
    showMenu: Boolean,
    onMenuToggle: () -> Unit,
    onMenuDismiss: () -> Unit,
    onOpen: () -> Unit,
    onSimulador: () -> Unit,
    onDelete: () -> Unit
) {
    val mediaAnio = calcularMediaAnio(anio)
    val colorScheme = MaterialTheme.colorScheme
    val ringColors = storyRingColors[(index - 1) % storyRingColors.size]
    val mediaColor = when {
        mediaAnio == null -> colorScheme.onSurfaceVariant
        mediaAnio >= 7.0 -> colorScheme.tertiary
        mediaAnio >= 5.0 -> colorScheme.primary
        else -> colorScheme.error
    }
    val statusText = when {
        mediaAnio == null -> "Sin notas"
        mediaAnio >= 7.0 -> "Vas muy bien"
        mediaAnio >= 5.0 -> "Aprobado"
        else -> "En riesgo"
    }
    val statusColor = when {
        mediaAnio == null -> colorScheme.onSurfaceVariant
        mediaAnio >= 7.0 -> colorScheme.tertiary
        mediaAnio >= 5.0 -> colorScheme.primary
        else -> colorScheme.error
    }
    val asignaturas = anio.lista_asignaturas?.size ?: 0
    val maxAsig = anio.numero_asignaturas ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = onOpen,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(ringColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = toRoman(index),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = anio.nombre ?: "Curso $index",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$asignaturas / $maxAsig asignaturas",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = onMenuToggle) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = onMenuDismiss) {
                        DropdownMenuItem(
                            text = { Text("Abrir curso") },
                            onClick = { onMenuDismiss(); onOpen() }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulador") },
                            onClick = { onMenuDismiss(); onSimulador() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = colorScheme.error) },
                            onClick = onDelete
                        )
                    }
                }
            }

            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaAnio?.let { String.format("%.2f", it) } ?: "--",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = mediaColor
                    )
                    Text(
                        text = "media actual",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    if (!anio.fechaInicio.isNullOrBlank() && !anio.fechaFin.isNullOrBlank()) {
                        Text(
                            text = "${anio.fechaInicio} – ${anio.fechaFin}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun SimuladorFeedCard(onSimulador: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = onSimulador,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(colorScheme.primary, colorScheme.tertiary.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "¿Qué nota necesitas?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary
                    )
                }
                Text(
                    text = "Selecciona una asignatura y Edutrack te dice automáticamente qué necesitas sacar para alcanzar tu objetivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onPrimary.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Abrir simulador",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedState(onCrearAnio: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "📚", style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
        Text(
            text = "Tu feed está vacío",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Crea tu primer curso y empieza a controlar tus notas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onCrearAnio,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Crear curso")
        }
    }
}

// Dialogo para seleccionar fecha y devolverla formateada.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDeFecha(onFechaSeleccionada: (String) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    val fechaSeleccionada = datePickerState.selectedDateMillis?.let {
                        formatearFecha(it)
                    } ?: ""
                    onFechaSeleccionada(fechaSeleccionada)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Mantiene el estado de anios en tiempo real desde Firebase.
// Lee todos los anios y filtra por uid en cliente para evitar fallos de query/indexado.
@Composable
fun rememberAniosState(id_user: String?): State<List<Anio>> {
    val aniosState = remember { mutableStateOf<List<Anio>>(emptyList()) }

    val resolvedUid = id_user?.takeIf { it.isNotBlank() }
        ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    DisposableEffect(resolvedUid) {
        if (resolvedUid.isNullOrEmpty()) {
            aniosState.value = emptyList()
            onDispose {}
        } else {
            val ref = com.example.edutrack.aniosRef(resolvedUid)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    aniosState.value = snapshot.children.mapNotNull { parseAnioSnapshot(it) }
                }
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("Firebase", "Error leyendo anios: ${error.message} code=${error.code}")
                }
            }
            ref.addValueEventListener(valueEventListener)
            onDispose { ref.removeEventListener(valueEventListener) }
        }
    }

    return aniosState
}
