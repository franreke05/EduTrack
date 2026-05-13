package com.example.edutrack.Inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edutrack.Premium.UpgradeSheet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Examen
import com.example.edutrack.examenesRef
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.domain.rememberUserPlan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Paleta de colores para anillos de stories (Instagram-style).
private val storyRingColors = listOf(
    listOf(Color(0xFFE040FB), Color(0xFF7C4DFF)),
    listOf(Color(0xFF4361EE), Color(0xFF00B0FF)),
    listOf(Color(0xFF2AA26E), Color(0xFF00E5FF)),
    listOf(Color(0xFFFF6D00), Color(0xFFFFD740)),
    listOf(Color(0xFFE53935), Color(0xFFFF6D00)),
)

@Preview(showBackground = true)
@Composable
fun Iniciopreview() {
    val mockAnios = listOf(
        Anio(id = "1", nombre = "1º Grado Ing. Informática", numero_asignaturas = 10),
        Anio(id = "2", nombre = "2º Grado Ing. Informática", numero_asignaturas = 12)
    )
    MaterialTheme {
        CuerpoInicioContent(
            anios = mockAnios,
            userPlan = UserPlan.FREE,
            onAnioSelected = {},
            onCrearAnio = {},
            onPerfil = {},
            onSimulador = {},
            onPaywall = {},
            onGrupos = {}
        )
    }
}
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

    CuerpoInicioContent(
        modifier = modifier,
        anios = aniosFromFirebase,
        userPlan = userPlan,
        onAnioSelected = onAnioSelected,
        onCrearAnio = onCrearAnio,
        onPerfil = onPerfil,
        onSimulador = onSimulador,
        onPaywall = onPaywall,
        onGrupos = onGrupos
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoInicioContent(
    modifier: Modifier = Modifier,
    anios: List<Anio>,
    userPlan: UserPlan,
    onAnioSelected: (String?) -> Unit = {},
    onCrearAnio: () -> Unit = {},
    onPerfil: () -> Unit = {},
    onSimulador: () -> Unit = {},
    onPaywall: () -> Unit = {},
    onGrupos: () -> Unit = {}
) {
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
                if (!PlanManager.canCreateCourse(userPlan, anios.size)) {
                    showCourseUpgrade = true
                } else {
                    onCrearAnio()
                }
            }

            item {
                InstagramTopBar(
                    onPerfil = onPerfil,
                    onGrupos = onGrupos,
                    isPremium = userPlan == UserPlan.PREMIUM
                )
            }

            item {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                StoriesRow(
                    anios = anios,
                    onCrearAnio = guardedCrearAnio,
                    onAnioNavigate = { anio -> onAnioSelected(anio.id) }
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (anios.isEmpty()) {
                item {
                    EmptyFeedState(onCrearAnio = guardedCrearAnio)
                }
            } else {
                item {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isTablet = maxWidth > 600.dp

                        if (isTablet) {
                            // Layout en dos columnas para tablets
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Primera fila: Resumen Académico + Simulador
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AnimatedFeedItem(index = 0) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            ResumenGeneralCard(
                                                anios = anios,
                                                onSimulador = onSimulador
                                            )
                                        }
                                    }
                                    AnimatedFeedItem(index = 1) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            SimuladorFeedCard(onSimulador = onSimulador)
                                        }
                                    }
                                }

                                // Segunda fila: Exámenes del mes (fullwidth)
                                AnimatedFeedItem(index = 2) {
                                    ExamenesFeedCard(anios = anios, onAnioSelected = onAnioSelected)
                                }
                            }
                        } else {
                            // Layout en columna para móviles
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedFeedItem(index = 0) {
                                    ResumenGeneralCard(
                                        anios = anios,
                                        onSimulador = onSimulador
                                    )
                                }
                                AnimatedFeedItem(index = 1) {
                                    SimuladorFeedCard(onSimulador = onSimulador)
                                }
                                AnimatedFeedItem(index = 2) {
                                    ExamenesFeedCard(anios = anios, onAnioSelected = onAnioSelected)
                                }
                            }
                        }
                    }
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
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
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

// Wrapper que anima la entrada de cada item del feed con stagger por índice.
@Composable
private fun AnimatedFeedItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L + 80L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(350)) +
                slideInVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) { it / 3 }
    ) {
        content()
    }
}

@Composable
private fun InstagramTopBar(
    onPerfil: () -> Unit,
    onGrupos: () -> Unit = {},
    isPremium: Boolean = false
) {
    // Slide-down de la barra al entrar
    val offsetY = remember { Animatable(-30f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { offsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(1f, animationSpec = tween(400)) }
    }

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
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Premium",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
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
            StoryItem(label = "Nueva", isNew = true, index = 0, media = null, onClick = onCrearAnio)
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

    // Spring bounce al aparecer, con stagger por índice
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 55L)
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch { alpha.animateTo(1f, animationSpec = tween(250)) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .scale(scale.value)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.size(68.dp), contentAlignment = Alignment.Center) {
            if (isNew) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
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
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(ringColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
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
            modifier = Modifier.width(68.dp),
            textAlign = TextAlign.Center
        )
    }
}

// Tarjeta de resumen global: media total, cursos activos, asignaturas totales.
@Composable
private fun ResumenGeneralCard(anios: List<Anio>, onSimulador: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val medias = anios.mapNotNull { calcularMediaAnio(it) }
    val mediaGlobal = if (medias.isNotEmpty()) medias.average() else null
    val totalAsignaturas = anios.sumOf { it.lista_asignaturas?.size ?: 0 }

    val animatedMedia by animateFloatAsState(
        targetValue = mediaGlobal?.toFloat() ?: 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "mediaGlobal"
    )

    val mediaColor = when {
        mediaGlobal == null -> colorScheme.onSurfaceVariant
        mediaGlobal >= 7.0 -> colorScheme.tertiary
        mediaGlobal >= 5.0 -> colorScheme.primary
        else -> colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media global
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Resumen académico",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (mediaGlobal != null) String.format("%.2f", animatedMedia) else "--",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = mediaColor
                )
                Text(
                    text = "media global",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Stats laterales
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBadge(label = "${anios.size}", sublabel = if (anios.size == 1) "curso" else "cursos", color = colorScheme.primary)
                StatBadge(label = "$totalAsignaturas", sublabel = "asignaturas", color = colorScheme.tertiary)
                Surface(
                    onClick = onSimulador,
                    color = colorScheme.primary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Simulador",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, sublabel: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val minAprobado = anio.nota_minima_aprobado ?: 5.0
    val mediaColor = when {
        mediaAnio == null -> colorScheme.onSurfaceVariant
        mediaAnio >= minAprobado + 2.0 -> colorScheme.tertiary
        mediaAnio >= minAprobado -> colorScheme.primary
        else -> colorScheme.error
    }
    val statusText = when {
        mediaAnio == null -> "Sin notas"
        mediaAnio >= minAprobado + 2.0 -> "Vas muy bien"
        mediaAnio >= minAprobado -> "Aprobado"
        else -> "En riesgo"
    }
    val statusColor = mediaColor
    val asignaturas = anio.lista_asignaturas?.size ?: 0
    val maxAsig = anio.numero_asignaturas ?: 0

    // Barra de progreso animada
    val progress = if (maxAsig > 0) asignaturas.toFloat() / maxAsig.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "progress_$index"
    )

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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(ringColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$asignaturas / $maxAsig asignaturas",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box {
                    val menuRotation by animateFloatAsState(
                        targetValue = if (showMenu) 90f else 0f,
                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                        label = "menuRotation_$index"
                    )
                    IconButton(onClick = onMenuToggle) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = if (showMenu) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(menuRotation)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = onMenuDismiss
                    ) {
                        DropdownMenuItem(
                            text = { Text("Abrir curso", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { onMenuDismiss(); onOpen() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        DropdownMenuItem(
                            text = { Text("Simulador", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { onMenuDismiss(); onSimulador() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        DropdownMenuItem(
                            text = { Text("Eliminar", style = MaterialTheme.typography.bodyMedium, color = colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = onDelete
                        )
                    }
                }
            }

            // Barra de progreso de asignaturas
            if (maxAsig > 0) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = statusColor.copy(alpha = 0.7f),
                    trackColor = colorScheme.outlineVariant.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round
                )
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

// Tarjeta del simulador con shimmer animado sobre el gradiente.
@Composable
private fun SimuladorFeedCard(onSimulador: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

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
        ) {
            // Shimmer layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerProgress * 600f, 0f),
                            end = Offset(shimmerProgress * 600f + 400f, 400f)
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "¿Qué nota necesitas?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary
                    )
                }
                Text(
                    text = "Selecciona una asignatura y Edutrack te dice exactamente qué necesitas para alcanzar tu objetivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onPrimary.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
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
private fun ExamenesFeedCard(anios: List<Anio>, onAnioSelected: (String?) -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme
    var mostrarCalendario by remember { mutableStateOf(false) }

    // Parsear exámenes del mes actual
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentYear = now.get(Calendar.YEAR)

    // Estructura: (nombre, hora, media)
    val examensByDay = mutableMapOf<Int, MutableList<Triple<String, String, Double?>>>()
    val daysWithExams = mutableSetOf<Int>()

    anios.forEach { anio ->
        anio.lista_asignaturas?.values?.forEach { asignatura ->
            if (!asignatura.fechaExamen.isNullOrEmpty()) {
                try {
                    val date = sdf.parse(asignatura.fechaExamen)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        if (cal.get(Calendar.MONTH) + 1 == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                            val day = cal.get(Calendar.DAY_OF_MONTH)
                            val asigName = asignatura.nombre ?: "Examen"
                            val hora = asignatura.horaExamen ?: "--:--"
                            examensByDay.getOrPut(day) { mutableListOf() }
                                .add(Triple(asigName, hora, asignatura.media))
                            daysWithExams.add(day)
                        }
                    }
                } catch (e: Exception) {
                    // Fecha inválida, ignorar
                }
            }
        }
    }

    val monthName = now.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es"))
    val yearStr = currentYear.toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Exámenes de este mes",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${monthName.replaceFirstChar { it.uppercase() }} $yearStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = { mostrarCalendario = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver calendario", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Mini calendario de la semana actual
            MiniCalendarGrid(
                currentDay = now.get(Calendar.DAY_OF_MONTH),
                daysWithExams = daysWithExams,
                currentMonth = currentMonth,
                currentYear = currentYear
            )

            // Timeline de exámenes del mes
            if (examensByDay.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sortedDays = examensByDay.keys.sorted()
                    items(sortedDays.size, key = { index -> sortedDays[index] }) { index ->
                        val day = sortedDays[index]
                        examensByDay[day]?.firstOrNull()?.let { (asigName, hora, media) ->
                            val ringColor = storyRingColors[index % storyRingColors.size]
                            ExamenBadge(
                                asignatura = asigName,
                                dia = day.toString().padStart(2, '0'),
                                hora = hora,
                                media = media,
                                colors = ringColor
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin exámenes programados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (mostrarCalendario) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        CalendarioBottomSheet(
            anios = anios,
            userId = userId,
            onDismiss = { mostrarCalendario = false },
            onNavAsignatura = onAnioSelected
        )
    }
}

@Composable
private fun MiniCalendarGrid(currentDay: Int, daysWithExams: Set<Int>, currentMonth: Int, currentYear: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val now = Calendar.getInstance()
    val weekStart = now.clone() as Calendar
    weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val days = mutableListOf<Pair<Int, Int>>() // (dayOfMonth, monthOfDay: 0=current, -1=prev, 1=next)
    for (i in 0..6) {
        val dayOfMonth = weekStart.get(Calendar.DAY_OF_MONTH)
        val monthOfDay = when {
            weekStart.get(Calendar.MONTH) + 1 < currentMonth -> -1  // Previous month
            weekStart.get(Calendar.MONTH) + 1 > currentMonth -> 1   // Next month
            else -> 0  // Current month
        }
        days.add(dayOfMonth to monthOfDay)
        weekStart.add(Calendar.DAY_OF_MONTH, 1)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Encabezados
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Días
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEach { (day, monthOfDay) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day == currentDay && monthOfDay == 0) colorScheme.primary
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (day == currentDay && monthOfDay == 0) colorScheme.onPrimary
                                        else if (monthOfDay != 0) colorScheme.onSurfaceVariant
                                        else colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (day in daysWithExams && monthOfDay == 0) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.error)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamenBadge(asignatura: String, dia: String, hora: String?, media: Double?, colors: List<Color>) {
    val colorScheme = MaterialTheme.colorScheme

    val mediaColor = when {
        media == null || media <= 0 -> colorScheme.onSurfaceVariant
        media >= 7.0 -> colorScheme.tertiary
        media >= 5.0 -> colorScheme.primary
        else -> colorScheme.error
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dia,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = asignatura.take(12),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = hora ?: "--:--",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        if (media != null && media > 0) {
            Text(
                text = String.format("%.1f", media),
                style = MaterialTheme.typography.labelSmall,
                color = mediaColor,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarioBottomSheet(
    anios: List<Anio>,
    userId: String?,
    onDismiss: () -> Unit,
    onNavAsignatura: (String?) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentYear = now.get(Calendar.YEAR)

    val monthName = now.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es"))
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)

    val examensByDay = remember { mutableStateOf<Map<Int, List<Pair<String, String>>>>(emptyMap()) }
    val allDaysInMonth = remember { mutableStateOf(setOf<Int>()) }

    // Cargar exámenes desde Firebase en tiempo real
    DisposableEffect(anios, userId, currentMonth, currentYear) {
        if (userId.isNullOrEmpty() || anios.isEmpty()) {
            examensByDay.value = emptyMap()
            allDaysInMonth.value = emptySet()
            onDispose {}
        } else {
            var completedYears = 0
            val totalYears = anios.size
            val examensByDayTemp = mutableMapOf<Int, MutableList<Pair<String, String>>>()
            val allDaysTemp = mutableSetOf<Int>()

            anios.forEach { anio ->
                if (anio.id.isNullOrEmpty()) {
                    completedYears++
                    return@forEach
                }

                val anioRef = com.example.edutrack.aniosRef(userId!!).child(anio.id!!)
                anioRef.child("asignaturas").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { asigSnapshot ->
                            val asigId = asigSnapshot.key ?: return@forEach

                            // Cargar exámenes para esta asignatura
                            val examenesRef = com.example.edutrack.examenesRef(userId!!, anio.id!!, asigId)
                            examenesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(exSnapshot: DataSnapshot) {
                                    exSnapshot.children.forEach { examenSnapshot ->
                                        val examen = examenSnapshot.getValue(Examen::class.java)
                                        // Mostrar exámenes con fecha válida (hora es opcional)
                                        if (examen != null &&
                                            examen.fecha.isNotBlank() &&
                                            examen.nombre.isNotBlank() &&
                                            !examen.nombre.equals("prueba", ignoreCase = true)) {
                                            try {
                                                val parts = examen.fecha.split("/")
                                                if (parts.size == 3) {
                                                    val day = parts[0].toInt()
                                                    val month = parts[1].toInt()
                                                    val year = parts[2].toInt()

                                                    if (month == currentMonth && year == currentYear) {
                                                        val horaDisplay = examen.hora.takeIf { it.isNotBlank() } ?: "--:--"
                                                        examensByDayTemp.getOrPut(day) { mutableListOf() }.add(
                                                            Pair(examen.nombre, horaDisplay)
                                                        )
                                                        allDaysTemp.add(day)
                                                        android.util.Log.d("CalendarioBottomSheet", "Loaded examen: ${examen.nombre} on day $day at $horaDisplay")
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("CalendarioBottomSheet", "Error parsing fecha: ${e.message}")
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    android.util.Log.e("CalendarioBottomSheet", "Error loading examenes: ${error.message}")
                                }
                            })
                        }

                        completedYears++
                        if (completedYears == totalYears) {
                            examensByDay.value = examensByDayTemp
                            allDaysInMonth.value = allDaysTemp
                            android.util.Log.d("CalendarioBottomSheet", "Calendar loaded: ${examensByDayTemp.size} days with exams")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        android.util.Log.e("CalendarioBottomSheet", "Error loading asignaturas: ${error.message}")
                        completedYears++
                        if (completedYears == totalYears) {
                            examensByDay.value = examensByDayTemp
                            allDaysInMonth.value = allDaysTemp
                        }
                    }
                })
            }

            onDispose {}
        }
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Exámenes • ${monthName.replaceFirstChar { it.uppercase() }} $currentYear",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Cerrar",
                        modifier = Modifier.rotate(90f)
                    )
                }
            }

            // Calendario completo
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Headers
                items(7) { idx ->
                    val dayName = listOf("L", "M", "X", "J", "V", "S", "D")[idx]
                    Box(
                        modifier = Modifier.height(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Days
                val firstDay = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }.get(Calendar.DAY_OF_WEEK) - 2 // Monday = 0

                items(firstDay) { // Empty cells before month starts
                    Box(modifier = Modifier.height(40.dp))
                }

                items(daysInMonth) { dayIdx ->
                    val day = dayIdx + 1
                    val isCurrentDay = day == now.get(Calendar.DAY_OF_MONTH)
                    val hasExams = day in allDaysInMonth.value
                    val isSelected = day == selectedDay
                    val examesForDay = examensByDay.value[day] ?: emptyList()

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrentDay -> colorScheme.primary
                                    isSelected -> colorScheme.primaryContainer
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { selectedDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isCurrentDay) colorScheme.onPrimary else colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (hasExams && !isCurrentDay) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.tertiary)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)

            // Exámenes del día seleccionado
            if (selectedDay != null && selectedDay in examensByDay.value) {
                Text(
                    text = "Exámenes del ${selectedDay} de ${monthName}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                examensByDay.value[selectedDay]?.forEach { (examen, hora) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = examen,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = hora,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = {
                                // Solo cierra el calendario
                                onDismiss()
                            }) {
                                Text("Ver", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmptyFeedState(onCrearAnio: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500, easing = FastOutSlowInEasing)) { it / 4 }
    ) {
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
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Mantiene el estado de anios en tiempo real desde Firebase.
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
