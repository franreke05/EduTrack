package com.example.edutrack.Simulador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.Premium.UpgradeSheet
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.domain.RequiredGradeResult
import com.example.edutrack.domain.calculateRequiredGrade
import com.example.edutrack.domain.rememberUserPlan
import com.example.edutrack.notasRef
import com.example.edutrack.gestures.swipeBackGesture
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimuladorScreen(userId: String?, onBack: () -> Unit = {}, onPaywall: () -> Unit = {}) {
    val anios by rememberAniosState(userId)
    val userPlan by rememberUserPlan(userId)
    var selectedAnio by remember { mutableStateOf<Anio?>(null) }
    var selectedAsignatura by remember { mutableStateOf<Asignatura?>(null) }
    var anioMenuExpanded by remember { mutableStateOf(false) }
    var examPct by rememberSaveable { mutableStateOf("") }
    var examPctAutoFilled by remember { mutableStateOf(false) }
    var objetivo by rememberSaveable { mutableIntStateOf(5) }
    var periodoSel by rememberSaveable { mutableIntStateOf(1) }
    var showTargetUpgrade by remember { mutableStateOf(false) }

    // Stagger entrance animations
    var heroVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60L); heroVisible = true
        delay(120L); contentVisible = true
    }

    // Auto-select if only one anio; keep reference in sync with live data.
    LaunchedEffect(anios) {
        if (anios.size == 1 && selectedAnio == null) selectedAnio = anios.first()
        selectedAnio?.id?.let { id -> selectedAnio = anios.find { it.id == id } ?: selectedAnio }
    }
    LaunchedEffect(selectedAnio?.id) { selectedAsignatura = null; examPct = ""; periodoSel = 1 }
    LaunchedEffect(selectedAsignatura?.id) { examPct = ""; periodoSel = 1; examPctAutoFilled = false }

    val notas by rememberNotasForAsignatura(userId, selectedAnio?.id, selectedAsignatura?.id)

    // Auto-fill exam weight with remaining percentage once notes load (once per subject selection).
    LaunchedEffect(notas, periodoSel) {
        if (selectedAsignatura != null && !examPctAutoFilled && notas.isNotEmpty()) {
            val usedWt = notas.filter { it.periodo == periodoSel }.sumOf { it.porcentaje ?: 0.0 }
            val remaining = (100.0 - usedWt).coerceIn(0.0, 100.0).toInt()
            if (remaining in 1..99) {
                examPct = remaining.toString()
                examPctAutoFilled = true
            }
        }
    }

    val numeroPeriodos = selectedAsignatura?.numero_periodos ?: 1
    val tipoPeriodo = selectedAsignatura?.tipo_periodo ?: "Trimestre"
    val notasPeriodo = notas.filter { it.periodo == periodoSel }

    val usedWeight = notasPeriodo.sumOf { it.porcentaje ?: 0.0 }
    val currentWeightedPoints = notasPeriodo.sumOf { (it.nota ?: 0.0) * (it.porcentaje ?: 0.0) }
    val examWeightParsed = examPct.toDoubleOrNull()
    val examOverflow = examWeightParsed != null && (usedWeight + examWeightParsed) > 100.0 + 1e-6

    // Computed values for "Situación actual" card
    val currentPartialAvg = if (usedWeight > 1e-6) currentWeightedPoints / usedWeight else null
    val notaMinimaAnio = selectedAnio?.nota_minima_aprobado ?: 5.0

    val resultado: RequiredGradeResult? =
        if (selectedAsignatura != null && examWeightParsed != null && examWeightParsed > 0 && !examOverflow) {
            calculateRequiredGrade(
                currentWeightedPoints = currentWeightedPoints,
                usedWeight = usedWeight,
                remainingWeight = examWeightParsed,
                targetAverage = objetivo.toDouble()
            )
        } else null

    // Hoisted animated weight progress — must be at composable scope
    val animatedUsedWeight by animateFloatAsState(
        targetValue = (usedWeight / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(600)
    )

    // CHANGE 1: Hero breathing glow infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "heroPulse")
    val heroGlow by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = androidx.compose.animation.core.CubicBezierEasing(0.45f, 0f, 0.55f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroGlow"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Simulador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .swipeBackGesture(onBack),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero header with stagger entrance
            AnimatedVisibility(
                visible = heroVisible,
                enter = fadeIn(tween(400)) + slideInVertically { it / 4 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = heroGlow)
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = heroGlow * 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "¿Qué nota necesitas en el examen?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Selecciona curso y asignatura. Solo dinos el peso del examen y tu objetivo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Main content with stagger entrance
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(tween(350)) + slideInVertically { it / 5 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (anios.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sin cursos todavía",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Crea un curso y añade asignaturas para que el simulador pueda calcular qué nota necesitas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Course selector (only when more than one)
                        if (anios.size > 1) {
                            SimSectionLabel("Curso")
                            ExposedDropdownMenuBox(
                                expanded = anioMenuExpanded,
                                onExpandedChange = { anioMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedAnio?.nombre ?: "Selecciona un curso",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = anioMenuExpanded)
                                    },
                                    shape = MaterialTheme.shapes.large
                                )
                                ExposedDropdownMenu(
                                    expanded = anioMenuExpanded,
                                    onDismissRequest = { anioMenuExpanded = false }
                                ) {
                                    anios.forEach { anio ->
                                        DropdownMenuItem(
                                            text = { Text(anio.nombre ?: "Curso") },
                                            onClick = {
                                                selectedAnio = anio
                                                anioMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Subject selector — CHANGE 2: CourseCirclesRow replaces AsignaturaSelectCard list
                        if (selectedAnio != null) {
                            val asignaturas = selectedAnio?.lista_asignaturas?.values?.toList().orEmpty()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                SimSectionLabel("Asignatura")
                                if (anios.size == 1) {
                                    Text(
                                        text = selectedAnio?.nombre ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (asignaturas.isEmpty()) {
                                Text(
                                    text = "Este curso no tiene asignaturas aún.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                CourseCirclesRow(
                                    asignaturas = asignaturas,
                                    selectedId = selectedAsignatura?.id,
                                    notaMinima = notaMinimaAnio,
                                    onSelect = { selectedAsignatura = it }
                                )
                            }
                        }

                        // Period selector (only when subject has multiple periods)
                        if (selectedAsignatura != null && numeroPeriodos > 1) {
                            SimSectionLabel(tipoPeriodo)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..numeroPeriodos).forEach { p ->
                                    FilterChip(
                                        selected = periodoSel == p,
                                        onClick = { periodoSel = p; examPct = "" },
                                        label = { Text("$tipoPeriodo $p") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        // "Situación actual" card — replaces old "Evaluaciones registradas"
                        if (selectedAsignatura != null && notasPeriodo.isNotEmpty()) {
                            val avgColor = when {
                                currentPartialAvg == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                currentPartialAvg >= 7.0 -> MaterialTheme.colorScheme.tertiary
                                currentPartialAvg >= notaMinimaAnio -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            }
                            SimSectionLabel("Situación actual")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Top row: current average + evaluated %
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "Media actual",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (currentPartialAvg != null) String.format("%.2f", currentPartialAvg) else "--",
                                                style = MaterialTheme.typography.displaySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (currentPartialAvg != null) avgColor else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "de lo evaluado hasta ahora",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "${usedWeight.toInt()}%",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "evaluado",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    // Progress bar
                                    LinearProgressIndicator(
                                        progress = { animatedUsedWeight },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                    // Notes list
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    notasPeriodo.sortedBy { it.nombre }.forEach { nota ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = nota.nombre?.ifBlank { "Evaluación" } ?: "Evaluación",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            val notaVal = nota.nota ?: 0.0
                                            val notaColor = when {
                                                notaVal >= 7.0 -> MaterialTheme.colorScheme.tertiary
                                                notaVal >= notaMinimaAnio -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.error
                                            }
                                            Text(
                                                text = "${nota.nota} · ${nota.porcentaje?.toInt()}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = notaColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Exam input + target (appears once subject is selected)
                        if (selectedAsignatura != null) {
                            SimSectionLabel("El examen")
                            OutlinedTextField(
                                value = examPct,
                                onValueChange = { v ->
                                    if (v.length <= 3 && v.all { it.isDigit() }) {
                                        examPct = v
                                        examPctAutoFilled = true
                                    }
                                },
                                label = { Text("¿Qué % representa el examen?") },
                                placeholder = { Text("ej. 40") },
                                suffix = { Text("%") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = MaterialTheme.shapes.large,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = examOverflow,
                                supportingText = when {
                                    examOverflow -> {
                                        {
                                            Text(
                                                "El total supera el 100 % (ya evaluado: ${usedWeight.toInt()} %)",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    notasPeriodo.isEmpty() && examPct.isBlank() -> {
                                        {
                                            Text(
                                                "Sin evaluaciones previas: el examen cubrirá el % que indiques",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    else -> null
                                }
                            )

                            Text(
                                text = "Nota objetivo",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (5..10).forEach { n ->
                                    val isLocked = !PlanManager.canUseCustomTargets(userPlan) && n > 5
                                    FilterChip(
                                        selected = objetivo == n,
                                        onClick = {
                                            if (isLocked) showTargetUpgrade = true
                                            else objetivo = n
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        label = {
                                            if (isLocked) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text("$n", maxLines = 1, softWrap = false)
                                                }
                                            } else {
                                                Text("$n", maxLines = 1, softWrap = false)
                                            }
                                        }
                                    )
                                }
                            }

                            resultado?.let { SimResultadoCard(it) }

                            // Scenario table: shown when exam weight is set and no overflow
                            if (examWeightParsed != null && examWeightParsed > 0 && !examOverflow) {
                                SimScenarioTable(
                                    currentWeightedPoints = currentWeightedPoints,
                                    examWeight = examWeightParsed,
                                    objetivo = objetivo,
                                    notaMinimaAprobado = notaMinimaAnio
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showTargetUpgrade) {
        UpgradeSheet(
            title = "Objetivos personalizados",
            message = "Los objetivos personalizados son Premium. Gratis puedes calcular qué necesitas para aprobar con objetivo 5.",
            onUpgrade = onPaywall,
            onDismiss = { showTargetUpgrade = false }
        )
    }
}

@Composable
private fun SimSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

// CHANGE 2: CourseCirclesRow — replaces AsignaturaSelectCard list
@Composable
private fun CourseCirclesRow(
    asignaturas: List<Asignatura>,
    selectedId: String?,
    notaMinima: Double,
    onSelect: (Asignatura) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(asignaturas) { index, asignatura ->
            SubjectRingItem(
                asignatura = asignatura,
                isSelected = asignatura.id == selectedId,
                notaMinima = notaMinima,
                index = index,
                onClick = { onSelect(asignatura) }
            )
        }
    }
}

@Composable
private fun SubjectRingItem(
    asignatura: Asignatura,
    isSelected: Boolean,
    notaMinima: Double,
    index: Int,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val media = asignatura.media?.takeIf { it > 0.0 }

    // Semantic color for this subject's ring
    val ringColor = when {
        media == null -> cs.outlineVariant
        media >= 7.0 -> cs.tertiary
        media >= notaMinima -> cs.primary
        else -> cs.error
    }

    // Spring entrance scale per item
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 80L + 100L)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    // Selected scale spring
    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "selectedScale"
    )

    // Animated arc progress
    val animatedArc by animateFloatAsState(
        targetValue = if (media != null) (media / 10.0).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "arc"
    )

    val abbrev = (asignatura.nombre ?: "?")
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .take(2)
        .ifBlank { "?" }

    val trackColor = cs.outlineVariant.copy(alpha = 0.3f)
    val ringStroke = if (isSelected) 10.dp else 6.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value * selectedScale
                scaleY = scale.value * selectedScale
            }
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp)
        ) {
            val strokePx = ringStroke
            Canvas(modifier = Modifier.size(72.dp)) {
                val stroke = Stroke(strokePx.toPx(), cap = StrokeCap.Round)
                val inset = strokePx.toPx() / 2
                val arcSize = Size(size.width - strokePx.toPx(), size.height - strokePx.toPx())
                val topLeft = Offset(inset, inset)

                // Track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                    topLeft = topLeft,
                    size = arcSize
                )
                // Progress arc
                if (animatedArc > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = animatedArc * 360f,
                        useCenter = false,
                        style = stroke,
                        topLeft = topLeft,
                        size = arcSize
                    )
                }
                // Selected glow ring (outer)
                if (isSelected) {
                    drawCircle(
                        color = ringColor.copy(alpha = 0.2f),
                        radius = size.minDimension / 2,
                        style = Stroke(4.dp.toPx())
                    )
                }
            }

            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = abbrev,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) ringColor else cs.onSurface
                )
                if (media != null) {
                    Text(
                        text = String.format("%.1f", media),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = ringColor
                    )
                }
            }
        }

        // Subject name label below circle (truncated to 10 chars)
        Text(
            text = (asignatura.nombre ?: "?").take(10),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) ringColor else cs.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SimResultadoCard(resultado: RequiredGradeResult) {
    val cs = MaterialTheme.colorScheme

    val badgeLabel: String
    val badgeContainerColor: androidx.compose.ui.graphics.Color
    val badgeContentColor: androidx.compose.ui.graphics.Color
    val cardContainerColor: androidx.compose.ui.graphics.Color
    val cardContentColor: androidx.compose.ui.graphics.Color
    val titulo: String
    val cuerpo: String
    val gradeDisplayColor: androidx.compose.ui.graphics.Color

    when (resultado) {
        is RequiredGradeResult.Needed -> {
            val g = resultado.grade
            badgeLabel = when { g <= 5.0 -> "Cómodo"; g <= 7.0 -> "Alcanzable"; g <= 9.0 -> "Exigente"; else -> "Límite" }
            badgeContainerColor = when { g <= 5.0 -> cs.tertiaryContainer; g <= 7.0 -> cs.primaryContainer; g <= 9.0 -> cs.secondaryContainer; else -> cs.errorContainer }
            badgeContentColor = when { g <= 5.0 -> cs.onTertiaryContainer; g <= 7.0 -> cs.onPrimaryContainer; g <= 9.0 -> cs.onSecondaryContainer; else -> cs.onErrorContainer }
            cardContainerColor = cs.surface
            cardContentColor = cs.onSurface
            titulo = "Nota necesaria en el examen"
            cuerpo = when { g <= 5.0 -> "Está bien encaminado. Sigue repasando y llegarás tranquilo."; g <= 7.0 -> "Al alcance con un buen repaso. Organiza bien el tiempo de estudio."; g <= 9.0 -> "Es exigente. Centra todo el esfuerzo en este examen."; else -> "Solo tienes margen mínimo. Máximo rendimiento el día del examen." }
            gradeDisplayColor = when { g <= 7.0 -> cs.tertiary; g <= 9.0 -> cs.primary; else -> cs.error }
        }
        RequiredGradeResult.AlreadyEnough -> {
            badgeLabel = "Aprobado asegurado"; badgeContainerColor = cs.tertiaryContainer; badgeContentColor = cs.onTertiaryContainer
            cardContainerColor = cs.tertiaryContainer; cardContentColor = cs.onTertiaryContainer
            titulo = "¡Ya tienes suficiente!"; cuerpo = "Con las notas que llevas ya superas tu objetivo aunque suspendieras el examen."
            gradeDisplayColor = cs.tertiary
        }
        RequiredGradeResult.Impossible -> {
            badgeLabel = "Imposible"; badgeContainerColor = cs.errorContainer; badgeContentColor = cs.onErrorContainer
            cardContainerColor = cs.errorContainer; cardContentColor = cs.onErrorContainer
            titulo = "No es posible con este examen"; cuerpo = "Aunque saques un 10 no llegas al objetivo. Prueba a bajar la nota objetivo o revisa el porcentaje del examen."
            gradeDisplayColor = cs.error
        }
        RequiredGradeResult.Completed -> {
            badgeLabel = "Periodo cerrado"; badgeContainerColor = cs.surfaceVariant; badgeContentColor = cs.onSurfaceVariant
            cardContainerColor = cs.surfaceVariant; cardContentColor = cs.onSurfaceVariant
            titulo = "Periodo cerrado"; cuerpo = "El 100 % ya está evaluado. No hay margen para este examen."
            gradeDisplayColor = cs.onSurfaceVariant
        }
        RequiredGradeResult.InvalidData -> {
            badgeLabel = "Error"; badgeContainerColor = cs.errorContainer; badgeContentColor = cs.onErrorContainer
            cardContainerColor = cs.errorContainer; cardContentColor = cs.onErrorContainer
            titulo = "Datos no válidos"; cuerpo = "Comprueba que el objetivo esté entre 0 y 10."
            gradeDisplayColor = cs.error
        }
    }

    // CHANGE 3: Slower, more dramatic animation for gauge
    val animatedGrade by animateFloatAsState(
        targetValue = if (resultado is RequiredGradeResult.Needed) resultado.grade.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = badgeContainerColor, shape = MaterialTheme.shapes.extraLarge) {
                Text(
                    badgeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeContentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            // CHANGE 3: Circular gauge replaces LinearProgressIndicator for Needed case
            if (resultado is RequiredGradeResult.Needed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val gaugeSize = 180.dp
                    val gaugeStroke = 16.dp
                    Canvas(modifier = Modifier.size(gaugeSize)) {
                        val stroke = Stroke(gaugeStroke.toPx(), cap = StrokeCap.Round)
                        val inset = gaugeStroke.toPx() / 2
                        val arcRect = Size(size.width - gaugeStroke.toPx(), size.height - gaugeStroke.toPx())
                        val arcOffset = Offset(inset, inset)
                        val startAngle = 135f
                        val totalSweep = 270f

                        // Track arc
                        drawArc(
                            color = gradeDisplayColor.copy(alpha = 0.15f),
                            startAngle = startAngle,
                            sweepAngle = totalSweep,
                            useCenter = false,
                            style = stroke,
                            topLeft = arcOffset,
                            size = arcRect
                        )
                        // Filled arc
                        if (animatedGrade > 0f) {
                            drawArc(
                                color = gradeDisplayColor,
                                startAngle = startAngle,
                                sweepAngle = (animatedGrade / 10f).coerceIn(0f, 1f) * totalSweep,
                                useCenter = false,
                                style = stroke,
                                topLeft = arcOffset,
                                size = arcRect
                            )
                        }
                    }
                    // Centered grade text inside gauge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format("%.2f", animatedGrade),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = gradeDisplayColor
                        )
                        Text(
                            text = "/ 10",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cardContentColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = cardContentColor)
            Text(cuerpo, style = MaterialTheme.typography.bodyMedium, color = cardContentColor.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun SimScenarioTable(
    currentWeightedPoints: Double,
    examWeight: Double,
    objetivo: Int,
    notaMinimaAprobado: Double
) {
    val cs = MaterialTheme.colorScheme
    val grades = listOf(4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "¿Qué pasa si sacas en el examen?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = cs.primary
            )
            Spacer(Modifier.height(4.dp))
            // CHANGE 4: Animated stagger for scenario table rows
            grades.forEachIndexed { idx, g ->
                var rowVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { delay(idx * 60L + 80L); rowVisible = true }
                AnimatedVisibility(
                    visible = rowVisible,
                    enter = fadeIn(tween(250)) + slideInVertically { it / 2 }
                ) {
                    val resultingAvg = (currentWeightedPoints + g * examWeight) / 100.0
                    val meetsObjective = resultingAvg >= objetivo.toDouble() - 1e-9
                    val meetsMinima = resultingAvg >= notaMinimaAprobado - 1e-9
                    val rowColor = when {
                        meetsObjective -> cs.tertiary
                        meetsMinima -> cs.primary
                        else -> cs.error
                    }
                    Column {
                        if (idx > 0) HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Exam grade chip
                            Surface(
                                color = if (meetsMinima) cs.primaryContainer else cs.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = if (g == g.toLong().toDouble()) "${g.toInt()}" else String.format("%.1f", g),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (meetsMinima) cs.onPrimaryContainer else cs.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            // Arrow + resulting average + icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("→", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                                Text(
                                    text = "media ${String.format("%.2f", resultingAvg)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = rowColor
                                )
                                Icon(
                                    imageVector = if (meetsObjective) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = rowColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberNotasForAsignatura(
    userId: String?,
    anioId: String?,
    asignaturaId: String?
): State<List<Notas>> {
    val state = remember { mutableStateOf<List<Notas>>(emptyList()) }
    DisposableEffect(userId, anioId, asignaturaId) {
        if (userId.isNullOrBlank() || anioId.isNullOrBlank() || asignaturaId.isNullOrBlank()) {
            state.value = emptyList()
            onDispose {}
        } else {
            val ref = notasRef(userId, anioId, asignaturaId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    state.value = snapshot.children.mapNotNull { it.getValue(Notas::class.java) }
                }
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("Simulador", "Error leyendo notas: ${error.message}")
                }
            }
            ref.addValueEventListener(listener)
            onDispose { ref.removeEventListener(listener) }
        }
    }
    return state
}
