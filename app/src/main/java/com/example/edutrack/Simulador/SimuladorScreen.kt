package com.example.edutrack.Simulador

import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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

    val resultado: RequiredGradeResult? =
        if (selectedAsignatura != null && examWeightParsed != null && examWeightParsed > 0 && !examOverflow) {
            calculateRequiredGrade(
                currentWeightedPoints = currentWeightedPoints,
                usedWeight = usedWeight,
                remainingWeight = examWeightParsed,
                targetAverage = objetivo.toDouble()
            )
        } else null

    Scaffold(
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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

                // Subject selector
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            asignaturas.forEach { asignatura ->
                                AsignaturaSelectCard(
                                    asignatura = asignatura,
                                    isSelected = selectedAsignatura?.id == asignatura.id,
                                    onClick = { selectedAsignatura = asignatura }
                                )
                            }
                        }
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
                                label = { Text("$tipoPeriodo $p") }
                            )
                        }
                    }
                }

                // Loaded notes summary
                if (selectedAsignatura != null && notasPeriodo.isNotEmpty()) {
                    SimSectionLabel("Evaluaciones registradas")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${notasPeriodo.size} evaluaciones",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Peso evaluado: ${usedWeight.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            notasPeriodo.sortedBy { it.nombre }
                                .forEach { nota ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = nota.nombre?.ifBlank { "Evaluación" } ?: "Evaluación",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${nota.nota} · ${nota.porcentaje?.toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (5..10).forEach { n ->
                            val isLocked = !PlanManager.canUseCustomTargets(userPlan) && n > 5
                            FilterChip(
                                selected = objetivo == n,
                                onClick = {
                                    if (isLocked) showTargetUpgrade = true
                                    else objetivo = n
                                },
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
                                            Text("$n")
                                        }
                                    } else {
                                        Text("$n")
                                    }
                                }
                            )
                        }
                    }

                    resultado?.let { SimResultadoCard(it) }
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

@Composable
private fun AsignaturaSelectCard(
    asignatura: Asignatura,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = if (isSelected) colorScheme.primaryContainer else colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asignatura.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface
                )
                val media = asignatura.media?.takeIf { it > 0 }
                val creditos = asignatura.creditos?.takeIf { it > 0 }
                val subtitulo = listOfNotNull(
                    media?.let { "Media: ${String.format("%.2f", it)}" },
                    creditos?.let { "$it créditos" }
                ).joinToString(" · ")
                if (subtitulo.isNotBlank()) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                else colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SimResultadoCard(resultado: RequiredGradeResult) {
    val colorScheme = MaterialTheme.colorScheme
    val (titulo, cuerpo, esPositivo) = when (resultado) {
        is RequiredGradeResult.Needed -> Triple(
            "Necesitas un ${String.format("%.2f", resultado.grade)} en el examen",
            if (resultado.grade <= 7.0) "¡Está a tu alcance! Organiza tu estudio y llega preparado."
            else "Es exigente, pero posible. Centra todo el esfuerzo en este examen.",
            true
        )
        RequiredGradeResult.AlreadyEnough -> Triple(
            "¡Ya tienes suficiente!",
            "Con las notas que llevas ya superas tu objetivo aunque suspendieras el examen.",
            true
        )
        RequiredGradeResult.Impossible -> Triple(
            "No es posible con este examen",
            "Aunque saques un 10 no llegas al objetivo. Prueba a bajar la nota objetivo o revisa el porcentaje del examen.",
            false
        )
        RequiredGradeResult.Completed -> Triple(
            "Periodo cerrado",
            "El 100 % ya está evaluado. No hay margen para este examen.",
            false
        )
        RequiredGradeResult.InvalidData -> Triple(
            "Datos no válidos",
            "Comprueba que el objetivo esté entre 0 y 10.",
            false
        )
    }

    val containerColor = if (esPositivo) colorScheme.secondaryContainer else colorScheme.errorContainer
    val contentColor = if (esPositivo) colorScheme.onSecondaryContainer else colorScheme.onErrorContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (resultado is RequiredGradeResult.Needed) {
                Text(
                    text = String.format("%.2f", resultado.grade),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = cuerpo,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.85f)
            )
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
