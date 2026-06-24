package com.example.edutrack.Inicio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import com.example.edutrack.dataclass.GroupExam
import com.example.edutrack.dataclass.UserGroup
import com.example.edutrack.examenesRef
import com.example.edutrack.groupExamsRef
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.ui.LocalAnios
import com.example.edutrack.ui.LocalUserGroups
import com.example.edutrack.ui.LocalUserPlan
import com.example.edutrack.ui.LocalUsuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.edutrack.R
import com.example.edutrack.data.SessionPrefs
import com.example.edutrack.data.sessionDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    onPaywall: () -> Unit = {},
    onGrupos: () -> Unit = {}
) {
    val aniosFromFirebase = LocalAnios.current
    val userPlan = LocalUserPlan.current

    CuerpoInicioContent(
        modifier = modifier,
        anios = aniosFromFirebase,
        userPlan = userPlan,
        onAnioSelected = onAnioSelected,
        onCrearAnio = onCrearAnio,
        onPerfil = onPerfil,
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
                    isPremium = userPlan == UserPlan.PREMIUM
                )
            }

            item {
                AnimatedFeedItem(index = 0) {
                    ExamenesFeedCard(anios = anios, onAnioSelected = onAnioSelected)
                }
            }

            item {
                AnimatedFeedItem(index = 1) {
                    GruposQuickRow(onGrupos = onGrupos)
                }
            }

            if (anios.isEmpty()) {
                item {
                    EmptyFeedState(onCrearAnio = guardedCrearAnio)
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mis cursos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (PlanManager.canCreateCourse(userPlan, anios.size)) {
                            TextButton(onClick = guardedCrearAnio) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Añadir")
                            }
                        }
                    }
                }
                itemsIndexed(anios, key = { _, anio -> anio.id ?: anio.hashCode() }) { index, anio ->
                    AnimatedFeedItem(index = index + 2) {
                        AnioFeedCard(
                            anio = anio,
                            index = index + 1,
                            showMenu = showMenuFor == anio.id,
                            onMenuToggle = {
                                showMenuFor = if (showMenuFor == anio.id) null else anio.id
                            },
                            onMenuDismiss = { showMenuFor = null },
                            onOpen = { onAnioSelected(anio.id) },
                            onDelete = {
                                anioToDelete = anio
                                showDeleteDialog = true
                                showMenuFor = null
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.inicio_delete_title)) },
            text = { Text(stringResource(R.string.inicio_delete_text, anioToDelete?.nombre ?: "")) },
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
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showCourseUpgrade) {
        UpgradeSheet(
            title = stringResource(R.string.inicio_upgrade_title),
            message = stringResource(R.string.inicio_upgrade_message),
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
    isPremium: Boolean = false
) {
    // Slide-down de la barra al entrar
    val offsetY = remember { Animatable(-30f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { offsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(1f, animationSpec = tween(400)) }
    }

    val usuario = LocalUsuario.current
    val nombre = usuario?.nombre?.takeIf { it.isNotBlank() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (nombre != null) {
                Text(
                    text = stringResource(R.string.inicio_hello_user, nombre),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.inicio_app_name),
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
                            text = stringResource(R.string.inicio_premium_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        // Avatar con inicial en lugar de icono genérico
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onPerfil() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
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
            StoryItem(label = stringResource(R.string.inicio_story_new_label), isNew = true, index = 0, media = null, onClick = onCrearAnio)
        }
        itemsIndexed(anios) { index, anio ->
            val media = calcularMediaAnio(anio)
            StoryItem(
                label = anio.nombre?.take(10) ?: stringResource(R.string.inicio_story_default_label),
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
                        contentDescription = stringResource(R.string.inicio_story_new_cd),
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

@Composable
private fun GruposQuickRow(onGrupos: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val userGroups = LocalUserGroups.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onGrupos
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Group, contentDescription = null, tint = cs.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text("Grupos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (userGroups.isEmpty()) "Únete o crea un grupo"
                    else "${userGroups.size} grupo${if (userGroups.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = cs.onSurfaceVariant)
        }
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
        mediaAnio == null -> stringResource(R.string.inicio_anio_status_no_notas)
        mediaAnio >= minAprobado + 2.0 -> stringResource(R.string.inicio_anio_status_muy_bien)
        mediaAnio >= minAprobado -> stringResource(R.string.inicio_anio_status_aprobado)
        else -> stringResource(R.string.inicio_anio_status_riesgo)
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
                        text = anio.nombre ?: stringResource(R.string.inicio_story_default_label),
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
                            text = stringResource(R.string.inicio_anio_asignaturas_count, asignaturas, maxAsig),
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
                            text = { Text(stringResource(R.string.inicio_menu_abrir_curso), style = MaterialTheme.typography.bodyMedium) },
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
                            text = { Text(stringResource(R.string.inicio_menu_eliminar), style = MaterialTheme.typography.bodyMedium, color = colorScheme.error) },
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
                        text = stringResource(R.string.inicio_anio_media_actual),
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

            // Asignaturas inline
            val subjects = anio.lista_asignaturas?.values?.toList()?.sortedBy { it.nombre } ?: emptyList()
            if (subjects.isNotEmpty()) {
                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.3f))
                subjects.forEach { subject ->
                    val nota = subject.media ?: 0.0
                    val notaColor = when {
                        nota <= 0 -> colorScheme.onSurfaceVariant
                        nota >= 7 -> colorScheme.tertiary
                        nota >= (anio.nota_minima_aprobado ?: 5.0) -> colorScheme.primary
                        else -> colorScheme.error
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject.nombre ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (nota > 0) String.format("%.1f", nota) else "—",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = notaColor
                        )
                    }
                }
            }
        }
    }
}

private data class ExamenDelMes(
    val nombre: String,
    val hora: String,
    val anioId: String?,           // null para exámenes de grupo
    val groupName: String? = null  // non-null para exámenes de grupo
)

@Composable
private fun rememberExamenesMes(
    anios: List<Anio>,
    userId: String?,
    userGroups: List<UserGroup>,
    currentMonth: Int,
    currentYear: Int
): androidx.compose.runtime.State<Map<Int, List<ExamenDelMes>>> {
    val state = remember { androidx.compose.runtime.mutableStateOf<Map<Int, List<ExamenDelMes>>>(emptyMap()) }
    DisposableEffect(anios, userId, userGroups, currentMonth, currentYear) {
        val validAnios = if (userId.isNullOrEmpty()) emptyList() else anios.filter { !it.id.isNullOrEmpty() }
        val validGroups = userGroups.filter { !it.groupId.isNullOrEmpty() }
        val totalQueries = validAnios.size + validGroups.size

        if (totalQueries == 0) {
            state.value = emptyMap()
        } else {
            val temp = mutableMapOf<Int, MutableList<ExamenDelMes>>()
            var completed = 0
            fun checkDone() { if (++completed == totalQueries) state.value = temp }

            validAnios.forEach { anio ->
                com.example.edutrack.asignaturasRef(userId!!, anio.id!!)
                    .get()
                    .addOnSuccessListener { snap ->
                        snap.children.forEach { asigNode ->
                            asigNode.child("examenes").children.forEach { examenNode ->
                                val examen = examenNode.getValue(Examen::class.java)
                                if (examen != null && examen.fecha.isNotBlank() &&
                                    examen.nombre.isNotBlank() &&
                                    !examen.nombre.equals("prueba", ignoreCase = true)) {
                                    try {
                                        val parts = examen.fecha.split("/")
                                        if (parts.size == 3 &&
                                            parts[1].toInt() == currentMonth &&
                                            parts[2].toInt() == currentYear) {
                                            val hora = examen.hora.takeIf { it.isNotBlank() } ?: "--:--"
                                            temp.getOrPut(parts[0].toInt()) { mutableListOf() }
                                                .add(ExamenDelMes(examen.nombre, hora, anio.id!!))
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                        checkDone()
                    }
                    .addOnFailureListener { checkDone() }
            }

            validGroups.forEach { ug ->
                groupExamsRef(ug.groupId!!)
                    .get()
                    .addOnSuccessListener { snap ->
                        snap.children.forEach { node ->
                            val exam = node.getValue(GroupExam::class.java) ?: return@forEach
                            val fecha = exam.fecha?.takeIf { it.isNotBlank() } ?: return@forEach
                            try {
                                val parts = fecha.split("/")
                                if (parts.size == 3 &&
                                    parts[1].toInt() == currentMonth &&
                                    parts[2].toInt() == currentYear) {
                                    val hora = exam.hora?.takeIf { it.isNotBlank() } ?: "--:--"
                                    val nombre = exam.nombre?.takeIf { it.isNotBlank() }
                                        ?: exam.asignatura ?: "Examen"
                                    temp.getOrPut(parts[0].toInt()) { mutableListOf() }
                                        .add(ExamenDelMes(nombre, hora, null, ug.name))
                                }
                            } catch (_: Exception) {}
                        }
                        checkDone()
                    }
                    .addOnFailureListener { checkDone() }
            }
        }
        onDispose {}
    }
    return state
}

@Composable
private fun ExamenesFeedCard(anios: List<Anio>, onAnioSelected: (String?) -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme
    var mostrarCalendario by remember { mutableStateOf(false) }
    val userGroups = LocalUserGroups.current

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentYear = now.get(Calendar.YEAR)
    val currentDay = now.get(Calendar.DAY_OF_MONTH)

    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    val examensByDay by rememberExamenesMes(anios, userId, userGroups, currentMonth, currentYear)
    val daysWithExams = remember(examensByDay) { examensByDay.keys.toSet() }

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
                            text = stringResource(R.string.inicio_examenes_title),
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
                TextButton(
                    onClick = { mostrarCalendario = true },
                    modifier = Modifier.size(width = 140.dp, height = 40.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.inicio_calendario_btn), style = MaterialTheme.typography.labelSmall)
                }
            }

            // Mini calendario de la semana actual
            MiniCalendarGrid(
                currentDay = currentDay,
                daysWithExams = daysWithExams,
                currentMonth = currentMonth,
                currentYear = currentYear
            )

            // Timeline de exámenes de esta semana
            if (examensByDay.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sortedDays = examensByDay.keys.sorted()
                    items(sortedDays.size, key = { index -> sortedDays[index] }) { index ->
                        val day = sortedDays[index]
                        examensByDay[day]?.firstOrNull()?.let { entry ->
                            val ringColor = storyRingColors[index % storyRingColors.size]
                            ExamenBadge(
                                asignatura = entry.nombre,
                                dia = day.toString().padStart(2, '0'),
                                hora = entry.hora,
                                media = null,
                                colors = ringColor
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DateRange, null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        stringResource(R.string.inicio_examenes_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        stringResource(R.string.inicio_examenes_empty_hint),
                        style = MaterialTheme.typography.bodySmall,
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
    val userGroups = LocalUserGroups.current

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentYear = now.get(Calendar.YEAR)

    val monthName = now.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es"))
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)

    val examensByDay by rememberExamenesMes(anios, userId, userGroups, currentMonth, currentYear)
    val allDaysInMonth = remember(examensByDay) { examensByDay.keys.toSet() }

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
                    text = stringResource(R.string.inicio_calendar_header, monthName.replaceFirstChar { it.uppercase() }, currentYear),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.inicio_calendar_close_cd),
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
                    val hasExams = day in allDaysInMonth
                    val isSelected = day == selectedDay
                    val examesForDay = examensByDay[day] ?: emptyList()

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
            if (selectedDay != null && selectedDay in examensByDay) {
                Text(
                    text = stringResource(R.string.inicio_calendar_exams_day, selectedDay!!, monthName ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                examensByDay[selectedDay]?.forEach { entry ->
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
                                    text = entry.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
                                )
                                if (entry.groupName != null) {
                                    Text(
                                        text = entry.groupName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.primary.copy(alpha = 0.75f)
                                    )
                                }
                                Text(
                                    text = entry.hora,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            if (entry.anioId != null) {
                                TextButton(onClick = {
                                    onNavAsignatura(entry.anioId)
                                    onDismiss()
                                }) {
                                    Text(stringResource(R.string.inicio_calendar_ver), style = MaterialTheme.typography.labelSmall)
                                }
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
                text = stringResource(R.string.inicio_feed_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.inicio_feed_empty_body),
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
                Text(stringResource(R.string.inicio_feed_crear_curso))
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
            ) { Text(stringResource(R.string.action_accept)) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.action_cancel)) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Mantiene el estado de anios en tiempo real desde Firebase.
@Composable
fun rememberAniosState(id_user: String?): State<List<Anio>> {
    val context = LocalContext.current
    val aniosState = remember { mutableStateOf<List<Anio>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val cacheJson = remember { Json { ignoreUnknownKeys = true } }

    val resolvedUid = id_user?.takeIf { it.isNotBlank() }
        ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    // Muestra datos cacheados inmediatamente — sin esperar a Firebase
    LaunchedEffect(resolvedUid) {
        context.sessionDataStore.data.first()[SessionPrefs.ANIOS_JSON]?.let { json ->
            runCatching { cacheJson.decodeFromString<List<Anio>>(json) }
                .getOrNull()
                ?.takeIf { it.isNotEmpty() && aniosState.value.isEmpty() }
                ?.let { aniosState.value = it }
        }
    }

    DisposableEffect(resolvedUid) {
        if (resolvedUid.isNullOrEmpty()) {
            aniosState.value = emptyList()
            onDispose {}
        } else {
            val ref = com.example.edutrack.aniosRef(resolvedUid)
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { parseAnioSnapshot(it) }
                    aniosState.value = list
                    scope.launch {
                        context.sessionDataStore.edit {
                            it[SessionPrefs.ANIOS_JSON] = cacheJson.encodeToString(list)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            ref.addValueEventListener(valueEventListener)
            onDispose { ref.removeEventListener(valueEventListener) }
        }
    }

    return aniosState
}
