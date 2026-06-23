package com.example.edutrack.Anio

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.edutrack.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edutrack.CrearAsignatura
import com.example.edutrack.borrarAnioCompleto
import com.example.edutrack.editarAnio
import com.example.edutrack.Inicio.toRoman
import com.example.edutrack.ui.LocalAnios
import com.example.edutrack.ui.LocalUserGroups
import com.example.edutrack.ui.LocalUsuario
import com.example.edutrack.ui.LocalUserPlan
import com.example.edutrack.compartirAsignaturaConGrupo
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.pdf.CursoPdfExporter
import com.example.edutrack.reminders.ExamReminderScheduler
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLocale
import com.example.edutrack.gestures.swipeBackGesture

// Ruta de entrada: carga el anio seleccionado y muestra un indicador mientras falta data.
@Composable
fun AnioRoute(
    userId: String?,
    anioId: String?,
    onBack: () -> Unit = {},
    onOpenNotas: (Asignatura, String?) -> Unit = { _, _ -> },
    onPaywall: () -> Unit = {}
) {
    val anios = LocalAnios.current
    val anio = anios.firstOrNull { it.id == anioId }
    if (anio == null) {
        CircularProgressIndicator()
    } else {
        AnioScreen(anio = anio, userId = userId, pageIndex = anios.indexOf(anio), onBack = onBack, onOpenNotas = onOpenNotas, onPaywall = onPaywall)
    }
}

// Envuelve la pantalla del anio con navegacion por deslizamiento entre anios.
@Composable
fun AnioScreenWrapper(userId: String?, initialAnioId: String?) {
    val anios = LocalAnios.current
    if (anios.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val initialPageIndex = anios.indexOfFirst { it.id == initialAnioId }.coerceAtLeast(0)
        var currentIndex by remember { mutableStateOf(initialPageIndex) }
        SwipeToNavigate(
            currentIndex = currentIndex,
            totalItems = anios.size,
            onIndexChange = { newIndex -> currentIndex = newIndex }
        ) { page ->
            AnioScreen(anio = anios[page], pageIndex = page)
        }
    }
}

// Pantalla principal del anio: header con progreso, busqueda animada y grilla de asignaturas.
@SuppressLint("NonObservableLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnioScreen(
    modifier: Modifier = Modifier,
    anio: com.example.edutrack.dataclass.Anio,
    userId: String? = null,
    pageIndex: Int,
    onBack: () -> Unit = {},
    onOpenNotas: (Asignatura, String?) -> Unit = { _, _ -> },
    onPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val userGroups = LocalUserGroups.current
    val userPlan = LocalUserPlan.current
    var asignaturaToShare by remember { mutableStateOf<Asignatura?>(null) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showAsignaturaSheet by remember { mutableStateOf(false) }
    val maxAsignaturas = anio.numero_asignaturas ?: 0
    val actuales = anio.lista_asignaturas?.size ?: 0
    val anioLleno = actuales >= maxAsignaturas
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteAnioDialog by remember { mutableStateOf(false) }
    var showEditAnioSheet by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }
    val asignaturasBase = remember(anio.lista_asignaturas) {
        anio.lista_asignaturas?.values?.toList().orEmpty()
    }
    val asignaturasFiltradas by remember(showSearch, searchQuery, asignaturasBase) {
        derivedStateOf {
            if (showSearch && searchQuery.isNotBlank()) {
                asignaturasBase.filter { it.nombre?.contains(searchQuery, ignoreCase = true) == true }
            } else {
                asignaturasBase
            }
        }
    }
    val spacing = 16.dp
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxAsignaturas > 0) actuales.toFloat() / maxAsignaturas.toFloat() else 0f,
        animationSpec = tween(900),
        label = "headerProgress"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = anio.nombre ?: stringResource(R.string.anio_default_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.anio_topbar_back_cd),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showEditAnioSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.anio_topbar_edit_cd),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.anio_topbar_search_cd),
                            tint = if (showSearch) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { showActionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.anio_topbar_more_cd),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showActionsMenu,
                            onDismissRequest = { showActionsMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.anio_menu_export_pdf),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showActionsMenu = false
                                    if (PlanManager.canExportPdf(userPlan)) {
                                        snackbarScope.launch { CursoPdfExporter.exportAndShare(context, anio) }
                                    } else {
                                        onPaywall()
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.anio_menu_delete_course),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showActionsMenu = false
                                    showDeleteAnioDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            val limitMsg = stringResource(R.string.anio_limit_snackbar, maxAsignaturas)
            ExtendedFloatingActionButton(
                onClick = {
                    if (anioLleno) {
                        snackbarScope.launch {
                            snackbarHostState.showSnackbar(limitMsg)
                        }
                    } else {
                        showAsignaturaSheet = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.anio_fab_asignatura), fontWeight = FontWeight.Bold)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .swipeBackGesture(onBack = onBack),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = spacing, vertical = spacing)
            ) {
                AnimatedVisibility(
                    visible = showSearch,
                    enter = slideInVertically { -it } + fadeIn(tween(250)),
                    exit = slideOutVertically { -it } + fadeOut(tween(250))
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.anio_search_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing),
                        singleLine = true
                    )
                }

                // Header del curso
                val asigs = anio.lista_asignaturas?.values.orEmpty()

                val totalCreditos = asigs.sumOf { it.creditos ?: 0 }

                val mediaGlobal = run {
                    val medias = asigs
                        .filter { (it.numero_notas ?: 0) > 0 }
                        .mapNotNull { it.media }

                    if (medias.isEmpty()) null else medias.average()
                }

                val notaAprobado = anio.nota_minima_aprobado ?: 5.0

                val mediaColor = when {
                    mediaGlobal == null -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.45f)
                    mediaGlobal >= notaAprobado + 2.0 -> MaterialTheme.colorScheme.tertiary
                    mediaGlobal >= notaAprobado -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }

                val progressTarget = if (maxAsignaturas > 0) {
                    (actuales.toFloat() / maxAsignaturas.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }

                val progressAnimated by animateFloatAsState(
                    targetValue = progressTarget,
                    animationSpec = tween(
                        durationMillis = 750,
                        easing = FastOutSlowInEasing
                    ),
                    label = "yearCardProgress"
                )

                var mounted by remember(pageIndex, anio.nombre) {
                    mutableStateOf(false)
                }

                LaunchedEffect(pageIndex, anio.nombre) {
                    mounted = false
                    delay(40)
                    mounted = true
                }

                val cardScale by animateFloatAsState(
                    targetValue = if (mounted) 1f else 0.96f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "yearCardScale"
                )

                val cardAlpha by animateFloatAsState(
                    targetValue = if (mounted) 1f else 0f,
                    animationSpec = tween(320),
                    label = "yearCardAlpha"
                )

                val cardShape = RoundedCornerShape(30.dp)
                val primary = MaterialTheme.colorScheme.primary
                val tertiary = MaterialTheme.colorScheme.tertiary
                val onContainer = MaterialTheme.colorScheme.onPrimaryContainer

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing)
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = cardAlpha
                        }
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = cardShape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = onContainer.copy(alpha = 0.10f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.96f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                                    )
                                )
                            )
                            .drawBehind {
                                drawCircle(
                                    color = primary.copy(alpha = 0.13f),
                                    radius = size.maxDimension * 0.42f,
                                    center = Offset(
                                        x = size.width * 1.02f,
                                        y = -size.height * 0.10f
                                    )
                                )

                                drawCircle(
                                    color = tertiary.copy(alpha = 0.09f),
                                    radius = size.maxDimension * 0.34f,
                                    center = Offset(
                                        x = -size.width * 0.10f,
                                        y = size.height * 1.08f
                                    )
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.28f),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = toRoman(pageIndex + 1),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        letterSpacing = 0.4.sp
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.anio_anio_academico_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = onContainer.copy(alpha = 0.48f),
                                        letterSpacing = 0.9.sp
                                    )

                                    Text(
                                        text = anio.nombre ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = onContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (!anio.fechaInicio.isNullOrBlank() && !anio.fechaFin.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = onContainer.copy(alpha = 0.07f)
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                text = "${anio.fechaInicio} – ${anio.fechaFin}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = onContainer.copy(alpha = 0.68f)
                                            )
                                        }
                                    }
                                }

                                if (anio.descripcion?.isNotBlank() == true) {
                                    IconButton(
                                        onClick = { showDescriptionDialog = true },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(15.dp))
                                            .background(onContainer.copy(alpha = 0.07f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = stringResource(R.string.anio_description_info_cd),
                                            tint = onContainer.copy(alpha = 0.70f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PremiumYearStatChip(
                                label = if (mediaGlobal != null) {
                                    String.format(LocalLocale.current.platformLocale, "%.2f", mediaGlobal)
                                } else {
                                    "–"
                                },
                                sublabel = stringResource(R.string.anio_stat_media),
                                color = mediaColor,
                                modifier = Modifier.weight(1f)
                            )

                                PremiumYearStatChip(
                                    label = "$actuales / $maxAsignaturas",
                                    sublabel = stringResource(R.string.anio_stat_asignaturas),
                                    color = onContainer,
                                    modifier = Modifier.weight(1f)
                                )

                                if (totalCreditos > 0) {
                                    PremiumYearStatChip(
                                        label = "$totalCreditos",
                                        sublabel = stringResource(R.string.anio_stat_creditos),
                                        color = onContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            PremiumYearProgressBar(
                                label = stringResource(R.string.anio_progress_label),
                                progress = progressAnimated,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (asignaturasFiltradas.isEmpty() && !showSearch) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📚", style = MaterialTheme.typography.displaySmall)
                            Text(
                                text = stringResource(R.string.anio_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.anio_empty_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        itemsIndexed(asignaturasFiltradas) { index, asignatura ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 60L + 80L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(300)) + slideInVertically { it / 3 }
                            ) {
                                AsignaturaCard(
                                    index = index + 1,
                                    asignatura = asignatura,
                                    notaMinAprobado = anio.nota_minima_aprobado ?: 5.0,
                                    onClick = { onOpenNotas(asignatura, anio.id) },
                                    onLongClick = if (userGroups.isNotEmpty()) {
                                        { asignaturaToShare = asignatura }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text(stringResource(R.string.anio_description_title, anio.nombre ?: "")) },
            text = { Text(anio.descripcion ?: stringResource(R.string.anio_description_empty)) },
            confirmButton = {
                TextButton(onClick = { showDescriptionDialog = false }) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    if (showDeleteAnioDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAnioDialog = false },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    stringResource(R.string.anio_delete_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    stringResource(R.string.anio_delete_text, anio.nombre ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAnioDialog = false
                        val uid = userId ?: return@Button
                        val id = anio.id ?: return@Button
                        borrarAnioCompleto(uid, id) { success ->
                            if (success) onBack()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(stringResource(R.string.action_delete), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAnioDialog = false },
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showEditAnioSheet) {
        val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEditAnioSheet = false },
            sheetState = editSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            EditarAnioSheetContent(
                anio = anio,
                actuales = actuales,
                onDismiss = { showEditAnioSheet = false },
                onSave = { nombre, descripcion, maxAsig, tipoPeriodo ->
                    val uid = userId ?: return@EditarAnioSheetContent
                    val id = anio.id ?: return@EditarAnioSheetContent
                    editarAnio(uid, id, nombre, descripcion, maxAsig, tipoPeriodo) {
                        showEditAnioSheet = false
                    }
                }
            )
        }
    }

    asignaturaToShare?.let { asig ->
        PublicarAsignaturaDialog(
            asignatura = asig,
            grupos = userGroups,
            userId = userId,
            onDismiss = { asignaturaToShare = null }
        )
    }

    if (showAsignaturaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAsignaturaSheet = false },
            sheetState = sheetState
        ) {
            CrearAsignaturaSheetContent(
                anioId = anio.id,
                maxAsignaturas = maxAsignaturas,
                actuales = actuales,
                tipoPeriodo = anio.tipo_periodo ?: "Cuatrimestre",
                onDismiss = { showAsignaturaSheet = false },
                isPremium = PlanManager.isPremium(userPlan)
            )
        }
    }
}

// Tarjeta para una asignatura con media visible y thresholds dinámicos.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AsignaturaCard(
    index: Int,
    asignatura: Asignatura,
    notaMinAprobado: Double = 5.0,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val media = asignatura.media
    val colorScheme = MaterialTheme.colorScheme
    val mediaColor = when {
        media == null -> colorScheme.onSurfaceVariant
        media >= 7.0 -> colorScheme.tertiary
        media >= notaMinAprobado -> colorScheme.primary
        else -> colorScheme.error
    }
    val abbreviation = abbreviateName(asignatura.nombre ?: "")

    val cardShape = MaterialTheme.shapes.extraLarge
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = cardShape, ambientColor = mediaColor.copy(alpha = 0.08f), spotColor = mediaColor.copy(alpha = 0.12f))
            .background(colorScheme.surface, cardShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        // Accent bar izquierda con color de la media
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(4.dp)
                .fillMaxHeight()
                .background(mediaColor.copy(alpha = 0.7f), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(mediaColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = abbreviation,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = mediaColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (media != null) String.format("%.1f", media) else "–",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = mediaColor
                    )
                    Text(
                        text = stringResource(R.string.anio_stat_media),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = asignatura.nombre ?: stringResource(R.string.anio_asig_default_name, index),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            val statusLabel = when {
                media == null -> stringResource(R.string.anio_card_sin_nota)
                media >= notaMinAprobado + 2.0 -> stringResource(R.string.anio_card_notable)
                media >= notaMinAprobado -> stringResource(R.string.anio_card_aprobado)
                else -> stringResource(R.string.anio_card_suspenso)
            }

            // Chips: cada uno toma solo lo que necesita, sin medir en absoluto
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val creditos = asignatura.creditos ?: 0
                if (creditos > 0) {
                    Surface(
                        color = colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "$creditos cr",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
                asignatura.tipo_periodo?.let { tipo ->
                    Surface(
                        color = colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = abbreviatePeriodo(tipo),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Dot de estado en su propia fila — sin competir por espacio
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(mediaColor)
                )
                Text(
                    text = statusLabel,
                    color = mediaColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Barra de progreso de la nota
            val noteProgress = media?.div(10.0)?.toFloat()?.coerceIn(0f, 1f) ?: 0f
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(mediaColor.copy(alpha = 0.15f))
            ) {
                if (media != null) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(noteProgress)
                            .background(mediaColor, RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

private fun abbreviatePeriodo(tipo: String): String = when {
    tipo.startsWith("Cuatrimestre", ignoreCase = true) -> "Cuatrim."
    tipo.startsWith("Trimestre", ignoreCase = true) -> "Trimest."
    tipo.equals("Anual", ignoreCase = true) -> "Anual"
    else -> tipo.take(6)
}

@Composable
private fun AnioStatChip(
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EditarAnioSheetContent(
    anio: com.example.edutrack.dataclass.Anio,
    actuales: Int,
    onDismiss: () -> Unit,
    onSave: (nombre: String, descripcion: String, maxAsignaturas: Int, tipoPeriodo: String) -> Unit
) {
    var nombre by remember { mutableStateOf(anio.nombre ?: "") }
    var descripcion by remember { mutableStateOf(anio.descripcion ?: "") }
    var maxAsigText by remember { mutableStateOf((anio.numero_asignaturas ?: 12).toString()) }
    var tipoPeriodo by remember { mutableStateOf(anio.tipo_periodo ?: "Cuatrimestre") }
    var nombreError by remember { mutableStateOf(false) }
    var maxAsigError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    stringResource(R.string.anio_edit_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.anio_edit_sheet_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; nombreError = false },
            label = { Text(stringResource(R.string.anio_edit_field_nombre)) },
            isError = nombreError,
            supportingText = if (nombreError) {{ Text(stringResource(R.string.error_name_required)) }} else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text(stringResource(R.string.anio_edit_field_descripcion)) },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        OutlinedTextField(
            value = maxAsigText,
            onValueChange = { v ->
                val digits = v.filter { it.isDigit() }
                if (digits.isEmpty() || (digits.toIntOrNull() ?: 0) <= 20) {
                    maxAsigText = digits
                }
                maxAsigError = false
            },
            label = { Text(stringResource(R.string.anio_edit_field_max_asig)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = maxAsigError,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            supportingText = {
                if (maxAsigError) {
                    Text(
                        stringResource(R.string.anio_edit_max_asig_error, actuales),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(if (actuales != 1) stringResource(R.string.anio_edit_max_asig_hint_plural, actuales) else stringResource(R.string.anio_edit_max_asig_hint, actuales))
                }
            }
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.anio_edit_periodo_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                stringResource(R.string.anio_edit_periodo_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cuatrimestre", "Trimestre").forEach { opcion ->
                    FilterChip(
                        selected = tipoPeriodo == opcion,
                        onClick = { tipoPeriodo = opcion },
                        label = {
                            Text(
                                opcion,
                                fontWeight = if (tipoPeriodo == opcion) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            ) {
                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = {
                    val maxAsig = maxAsigText.toIntOrNull() ?: 0
                    if (nombre.isBlank()) { nombreError = true; return@Button }
                    if (maxAsig < actuales || maxAsig > 20) { maxAsigError = true; return@Button }
                    onSave(nombre.trim(), descripcion.trim(), maxAsig, tipoPeriodo)
                },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// Bottom sheet para crear una nueva asignatura con validación inline.
@Composable
private fun CrearAsignaturaSheetContent(
    anioId: String?,
    maxAsignaturas: Int,
    actuales: Int,
    tipoPeriodo: String,
    onDismiss: () -> Unit,
    isPremium: Boolean = false
) {
    val context = LocalContext.current
    val nombre = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val creditos = remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var creditosError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.anio_nueva_asignatura_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$actuales / $maxAsignaturas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        OutlinedTextField(
            value = nombre.value,
            onValueChange = { nombre.value = it; nombreError = null },
            label = { Text(stringResource(R.string.anio_asig_field_nombre)) },
            modifier = Modifier.fillMaxWidth(),
            isError = nombreError != null,
            supportingText = nombreError?.let { msg -> { Text(msg) } },
            singleLine = true
        )
        OutlinedTextField(
            value = descripcion.value,
            onValueChange = { descripcion.value = it },
            label = { Text(stringResource(R.string.anio_asig_field_descripcion)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
        OutlinedTextField(
            value = creditos.value,
            onValueChange = { creditos.value = it.filter { c -> c.isDigit() }; creditosError = null },
            label = { Text(stringResource(R.string.anio_asig_field_creditos)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = creditosError != null,
            supportingText = creditosError?.let { msg -> { Text(msg) } },
            singleLine = true
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    stringResource(R.string.anio_asig_periodo_label, tipoPeriodo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        val msgNombreObligatorio = stringResource(R.string.anio_asig_nombre_error)
        val msgCreditosError = stringResource(R.string.anio_asig_creditos_error)
        Button(
            onClick = {
                val trimmedNombre = nombre.value.trim()
                val creditosInt = creditos.value.toIntOrNull() ?: 0
                var hasError = false
                if (anioId.isNullOrEmpty() || trimmedNombre.isBlank()) {
                    nombreError = msgNombreObligatorio
                    hasError = true
                }
                if (creditosInt <= 0) {
                    creditosError = msgCreditosError
                    hasError = true
                }
                if (hasError) return@Button
                val numeroPeriodos = if (tipoPeriodo == "Cuatrimestre") 2 else 3
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                val asignatura = Asignatura(
                    nombre = trimmedNombre,
                    descripcion = descripcion.value,
                    creditos = creditosInt,
                    tipo_periodo = tipoPeriodo,
                    numero_periodos = numeroPeriodos
                )
                CrearAsignatura(uid, anioId!!, asignatura)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.anio_asig_create_btn), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PublicarAsignaturaDialog(
    asignatura: Asignatura,
    grupos: List<com.example.edutrack.dataclass.UserGroup>,
    userId: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val usuario = LocalUsuario.current
    var selectedGroupId by remember { mutableStateOf(grupos.firstOrNull()?.groupId) }
    var isSharing by remember { mutableStateOf(false) }
    val defaultUser = stringResource(R.string.anio_default_user)
    val defaultGroup = stringResource(R.string.anio_default_group)
    val msgSuccess = stringResource(R.string.anio_publish_success)
    val msgError = stringResource(R.string.anio_publish_error)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.anio_publish_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "\"${asignatura.nombre}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                val media = asignatura.media
                if (media != null && media > 0.0) {
                    Text(
                        text = stringResource(R.string.anio_publish_media, media),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.anio_publish_choose),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                grupos.forEach { grupo ->
                    val isSelected = selectedGroupId == grupo.groupId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroupId = grupo.groupId },
                        label = { Text(grupo.name ?: defaultGroup) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gid = selectedGroupId ?: return@Button
                    val uid = userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                    val userName = usuario?.nombre?.takeIf { it.isNotBlank() }
                        ?: FirebaseAuth.getInstance().currentUser?.displayName
                        ?: defaultUser
                    isSharing = true
                    val subject = GroupSharedSubject(
                        name = asignatura.nombre,
                        tipoPeriodo = asignatura.tipo_periodo,
                        numeroPeriodos = asignatura.numero_periodos,
                        media = asignatura.media,
                        sharedBy = uid,
                        sharedByName = userName,
                        sharedByPhotoUrl = usuario?.photoUrl,
                        sharedAt = System.currentTimeMillis()
                    )
                    compartirAsignaturaConGrupo(gid, subject) { success ->
                        isSharing = false
                        Toast.makeText(
                            context,
                            if (success) msgSuccess else msgError,
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }
                },
                enabled = selectedGroupId != null && !isSharing
            ) {
                if (isSharing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.anio_publish_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

// Vista previa para validar el layout del anio.
@Preview(showBackground = true)
@Composable
fun AnioScreenPreview() {
    EduTrackTheme {
        val mockAsignaturas = List(20) { Asignatura(id = "$it", nombre = "Asignatura ${it + 1}") }
        val mockAnio = com.example.edutrack.dataclass.Anio(
            id = "1",
            nombre = "Año 2023-2024",
            descripcion = "Descripción de prueba",
            lista_asignaturas = mockAsignaturas.associateBy { it.id ?: it.nombre ?: it.toString() },
            numero_asignaturas = 20
        )
        AnioScreen(anio = mockAnio, pageIndex = 12)
    }
}
@Composable
private fun PremiumYearStatChip(
    label: String,
    sublabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(350),
        label = "yearStatColor"
    )

    Surface(
        modifier = modifier.heightIn(min = 62.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 7.dp)
                    .width(28.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(animatedColor.copy(alpha = 0.65f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = animatedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.56f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumYearProgressBar(
    label: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val safeProgress = progress.coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.62f)
            )

            Text(
                text = "${(safeProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = safeProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.72f),
                                color
                            )
                        )
                    )
            )
        }
    }
}
