package com.example.edutrack.Notas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.edutrack.Inicio.SelectorDeFecha
import com.example.edutrack.borrarAsignaturaCompleta
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

// Activity que muestra la pantalla de notas de una asignatura.
class NotasActivity : ComponentActivity() {
    // Lee los parametros de la intent y abre la UI de notas.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val asignaturaId = intent.getStringExtra("ASIGNATURA_ID") ?: ""
        val asignaturaNombre = intent.getStringExtra("ASIGNATURA_NOMBRE") ?: "Asignatura"
        val tipoPeriodo = intent.getStringExtra("TIPO_PERIODO") ?: "Trimestre"
        val numeroPeriodos = intent.getIntExtra("NUMERO_PERIODOS", 3)
        val anioId = intent.getStringExtra("ANIO_ID")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (asignaturaId.isEmpty()) {
            Toast.makeText(this, "Asignatura no encontrada.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            EduTrackTheme {
                NotasScreen(
                    asignaturaId = asignaturaId,
                    asignaturaNombre = asignaturaNombre,
                    tipoPeriodo = tipoPeriodo,
                    numeroPeriodos = numeroPeriodos,
                    anioId = anioId,
                    userId = userId,
                    onBack = { finish() }
                )
            }
        }
    }
}

// Pantalla principal para gestionar notas por periodo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    asignaturaId: String,
    asignaturaNombre: String,
    tipoPeriodo: String,
    numeroPeriodos: Int,
    anioId: String?,
    userId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notasState = remember { mutableStateOf<List<Notas>>(emptyList()) }
    var notasLoaded by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val notaEnEdicion = remember { mutableStateOf<Notas?>(null) }
    val showDeleteConfirm = remember { mutableStateOf<Notas?>(null) }
    var showDeleteAsignatura by remember { mutableStateOf(false) }
    DisposableEffect(asignaturaId) {
        val notasRef = com.example.edutrack.notasRef(userId, anioId ?: "", asignaturaId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(Notas::class.java) }
                notasState.value = lista
                notasLoaded = true
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error leyendo notas", Toast.LENGTH_SHORT).show()
            }
        }
        notasRef.addValueEventListener(listener)
        onDispose { notasRef.removeEventListener(listener) }
    }

    val promedio by remember(notasState.value) {
        derivedStateOf { calcularPromedio(notasState.value) }
    }
    val porcentajeTotal by remember(notasState.value) {
        derivedStateOf { notasState.value.sumOf { it.porcentaje ?: 0.0 } }
    }
    val resumenNotas by remember(notasState.value) {
        derivedStateOf { calcularResumenNotas(notasState.value) }
    }
    var ultimoResumen by remember { mutableStateOf<ResumenNotas?>(null) }

    DisposableEffect(resumenNotas, notasLoaded, asignaturaId, anioId) {
        if (notasLoaded && ultimoResumen != resumenNotas) {
            ultimoResumen = resumenNotas
            actualizarMediaAsignatura(userId, anioId, asignaturaId, resumenNotas)
        }
        onDispose { }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(asignaturaNombre) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        notaEnEdicion.value = null
                        showDialog.value = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir nota")
                    }
                    IconButton(onClick = {
                        Toast.makeText(
                            context,
                            "Gestiona notas con porcentaje por $tipoPeriodo.",
                            Toast.LENGTH_LONG
                        ).show()
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Información")
                    }
                    IconButton(onClick = { showDeleteAsignatura = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar asignatura",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { inner ->
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EncabezadoNotas(
                    nombre = asignaturaNombre,
                    promedio = promedio,
                    porcentajeTotal = porcentajeTotal,
                )

                ListaNotasPorPeriodo(
                    notas = notasState.value,
                    numeroPeriodos = numeroPeriodos,
                    tipoPeriodo = tipoPeriodo,
                    onEditar = { nota ->
                        notaEnEdicion.value = nota
                        showDialog.value = true
                    },
                    onEliminar = { nota ->
                        showDeleteConfirm.value = nota
                    }
                )
            }
        }
    }

    if (showDialog.value) {
        NotaDialog(
            numeroPeriodos = numeroPeriodos,
            nota = notaEnEdicion.value,
            onDismiss = { showDialog.value = false },
            onSave = { nota ->
                guardarNota(userId, anioId ?: "", asignaturaId, nota) {
                    showDialog.value = false
                }
            },
            notasActuales = notasState.value
        )
    }

    showDeleteConfirm.value?.let { nota ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        eliminarNota(userId, anioId ?: "", asignaturaId, nota)
                        showDeleteConfirm.value = null
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm.value = null }) { Text("Cancelar") }
            },
            title = { Text("Eliminar nota") },
            text = { Text("¿Seguro que deseas eliminar ${nota.nombre}?") }
        )
    }

    if (showDeleteAsignatura) {
        AlertDialog(
            onDismissRequest = { showDeleteAsignatura = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        borrarAsignaturaCompleta(userId, anioId ?: "", asignaturaId)
                        showDeleteAsignatura = false
                        onBack()
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAsignatura = false }) { Text("Cancelar") }
            },
            title = { Text("Eliminar asignatura") },
            text = { Text("¿Deseas eliminar toda la asignatura y sus notas?") }
        )
    }
}

// Encabezado con media, porcentaje y estado visual de la asignatura.
@Composable
private fun EncabezadoNotas(nombre: String, promedio: Double, porcentajeTotal: Double) {
    val colorScheme = MaterialTheme.colorScheme
    val (estadoLabel, estadoColor) = when {
        porcentajeTotal <= 0.0 -> "Sin notas" to colorScheme.onSurfaceVariant
        promedio >= 5.0 -> "Vas bien" to colorScheme.tertiary
        promedio >= 4.0 -> "En riesgo" to colorScheme.error
        else -> "Atención" to colorScheme.error
    }
    val iniciales = abreviarNombre(nombre)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(colorScheme.primary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimary
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onPrimaryContainer,
                    maxLines = 1
                )
                Text(
                    text = estadoLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = estadoColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = String.format("%.2f", promedio),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${String.format("%.0f", porcentajeTotal)}% evaluado",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Vista previa del contenido de notas.
@Preview
@Composable
fun EncabezadoNotasPreview() {
    EduTrackTheme{
        ListaNotasPorPeriodo(List(1) { Notas("22","22",
            "ss", 20.0,
            20.0,
            "222",
            1) },
            3, "Trimestre", {}, {})
    }
}


// Lista de notas agrupadas por periodo con header de estado.
@Composable
private fun ListaNotasPorPeriodo(
    notas: List<Notas>,
    numeroPeriodos: Int,
    tipoPeriodo: String,
    onEditar: (Notas) -> Unit,
    onEliminar: (Notas) -> Unit
) {
    val grouped = notas.groupBy { it.periodo ?: 1 }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        (1..numeroPeriodos).forEach { periodo ->
            val lista = grouped[periodo].orEmpty()
            val mediaPeriodo = if (lista.isEmpty()) null else {
                val peso = lista.sumOf { it.porcentaje ?: 0.0 }
                if (peso > 0) lista.sumOf { (it.nota ?: 0.0) * (it.porcentaje ?: 0.0) } / peso else null
            }
            val porcentajePeriodo = lista.sumOf { it.porcentaje ?: 0.0 }

            item(key = "header_$periodo") {
                PeriodoHeader(
                    label = "$tipoPeriodo $periodo",
                    media = mediaPeriodo,
                    porcentaje = porcentajePeriodo
                )
            }
            if (lista.isEmpty()) {
                item(key = "empty_$periodo") {
                    EmptyPeriodoState()
                }
            } else {
                items(lista, key = { it.id ?: it.hashCode() }) { nota ->
                    NotaRow(nota, onEditar = { onEditar(nota) }, onEliminar = { onEliminar(nota) })
                }
            }
            item(key = "spacer_$periodo") { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun PeriodoHeader(label: String, media: Double?, porcentaje: Double) {
    val colorScheme = MaterialTheme.colorScheme
    val (estadoLabel, containerColor) = when {
        porcentaje <= 0.0 -> "Pendiente" to colorScheme.surfaceVariant
        porcentaje >= 99.9 -> "Completo" to colorScheme.tertiaryContainer
        media != null && media >= 5.0 -> "Vas bien" to colorScheme.secondaryContainer
        media != null -> "En riesgo" to colorScheme.errorContainer
        else -> "En curso" to colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = estadoLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (media != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1f", media),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(text = "media", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.0f", porcentaje)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(text = "evaluado", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyPeriodoState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Añade tu primera nota",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Fila de nota con jerarquía visual clara.
@Composable
private fun NotaRow(nota: Notas, onEditar: () -> Unit, onEliminar: () -> Unit) {
    val notaVal = nota.nota ?: 0.0
    val noteColor = when {
        notaVal >= 7.0 -> MaterialTheme.colorScheme.tertiary
        notaVal >= 5.0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditar() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(noteColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%.1f", notaVal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = noteColor
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nota.nombre ?: "Examen",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!nota.fecha.isNullOrBlank()) {
                        Text(
                            text = nota.fecha,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${nota.porcentaje ?: 0.0}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEditar, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Dialogo para crear o editar una nota con wheel pickers estilo iOS.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotaDialog(
    numeroPeriodos: Int,
    nota: Notas?,
    onDismiss: () -> Unit,
    onSave: (Notas) -> Unit,
    notasActuales: List<Notas>
) {
    val nombre = remember { mutableStateOf(nota?.nombre ?: "") }
    val fecha = remember { mutableStateOf(nota?.fecha ?: "") }
    val periodo = remember { mutableStateOf(nota?.periodo ?: 1) }
    val context = LocalContext.current
    var mostrarCalendario by remember { mutableStateOf(false) }

    val notaItems = remember { (0..100).map { String.format("%.1f", it * 0.1) } }
    val porcentajeItems = remember { (1..100).map { "$it" } }

    var notaIndex by remember {
        mutableIntStateOf(((nota?.nota ?: 5.0) * 10).roundToInt().coerceIn(0, 100))
    }
    var porcentajeIndex by remember {
        mutableIntStateOf(((nota?.porcentaje ?: 20.0) - 1).toInt().coerceIn(0, 99))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (nota == null) "Agregar nota" else "Editar nota",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nombre.value,
                    onValueChange = { nombre.value = it },
                    label = { Text("Nombre del examen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fecha.value,
                    onValueChange = {},
                    label = { Text("Fecha") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarCalendario = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
                if (mostrarCalendario) {
                    SelectorDeFecha(
                        onFechaSeleccionada = { fecha.value = it },
                        onDismiss = { mostrarCalendario = false }
                    )
                }

                // Wheel pickers
                Text(
                    text = "Nota y porcentaje",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Nota", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        WheelPicker(
                            items = notaItems,
                            initialIndex = notaIndex,
                            onItemSelected = { notaIndex = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = notaItems[notaIndex],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(220.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                            .align(Alignment.CenterVertically)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Porcentaje", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        WheelPicker(
                            items = porcentajeItems,
                            initialIndex = porcentajeIndex,
                            onItemSelected = { porcentajeIndex = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${porcentajeItems[porcentajeIndex]}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Periodo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Periodo:", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (p in 1..numeroPeriodos) {
                            val selected = periodo.value == p
                            Button(
                                onClick = { periodo.value = p },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            ) { Text("$p") }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        val notaDouble = notaIndex * 0.1
                        val porcentajeDouble = (porcentajeIndex + 1).toDouble()
                        if (nombre.value.isBlank()) {
                            Toast.makeText(context, "Escribe el nombre del examen.", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val porcentajeUsado = notasActuales
                            .filter { it.periodo == periodo.value && it.id != nota?.id }
                            .sumOf { it.porcentaje ?: 0.0 }
                        if (porcentajeUsado + porcentajeDouble > 100.0 + 1e-6) {
                            Toast.makeText(context, "El porcentaje total del periodo supera 100%.", Toast.LENGTH_LONG).show()
                            return@TextButton
                        }
                        onSave(
                            NotaConstruida(
                                base = nota,
                                nombre = nombre.value,
                                fecha = fecha.value,
                                nota = notaDouble,
                                porcentaje = porcentajeDouble,
                                periodo = periodo.value
                            )
                        )
                    }) { Text("Guardar", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

// Wheel picker estilo iOS con snap y fade en los extremos.
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 40.dp,
    visibleCount: Int = 5
) {
    val halfCount = visibleCount / 2
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex.coerceIn(0, maxOf(0, items.size - 1))
    )
    val snapBehavior = rememberSnapFlingBehavior(listState)

    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LaunchedEffect(selectedIndex) {
        onItemSelected(selectedIndex.coerceIn(0, maxOf(0, items.size - 1)))
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier.height(itemHeight * visibleCount)) {
        // Fondo del item seleccionado
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(primaryContainer, RoundedCornerShape(10.dp))
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * halfCount)
        ) {
            itemsIndexed(items) { index, item ->
                val distance = abs(index - selectedIndex)
                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.5f
                    else -> 0.2f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = if (distance == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                        fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (distance == 0) onPrimaryContainer else onSurface.copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Gradiente superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * halfCount)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(surfaceColor, Color.Transparent)))
        )
        // Gradiente inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * halfCount)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, surfaceColor)))
        )
    }
}

// Construye una nota nueva a partir de los datos del formulario.
private fun NotaConstruida(
    base: Notas?,
    nombre: String,
    fecha: String,
    nota: Double,
    porcentaje: Double,
    periodo: Int
): Notas {
    val id = base?.id?.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()
    return Notas(
        id = id,
        id_asignatura = base?.id_asignatura,
        nombre = nombre,
        nota = nota,
        porcentaje = porcentaje,
        fecha = fecha,
        periodo = periodo
    )
}

// Guarda una nota en Firebase para la asignatura.
private fun guardarNota(userId: String, anioId: String, asignaturaId: String, nota: Notas, onFinish: () -> Unit) {
    if (userId.isBlank() || anioId.isBlank() || asignaturaId.isBlank()) return
    val notaId = nota.id ?: UUID.randomUUID().toString()
    val notaFinal = Notas(
        id = notaId,
        id_asignatura = asignaturaId,
        nombre = nota.nombre,
        nota = nota.nota,
        porcentaje = nota.porcentaje,
        fecha = nota.fecha,
        periodo = nota.periodo
    )
    com.example.edutrack.notasRef(userId, anioId, asignaturaId)
        .child(notaId)
        .setValue(notaFinal)
        .addOnCompleteListener { onFinish() }
}

// Elimina una nota de Firebase.
private fun eliminarNota(userId: String, anioId: String, asignaturaId: String, nota: Notas) {
    val notaId = nota.id ?: return
    if (userId.isBlank() || anioId.isBlank()) return
    com.example.edutrack.notasRef(userId, anioId, asignaturaId)
        .child(notaId)
        .removeValue()
}

// Genera una abreviatura del nombre de la asignatura.
private fun abreviarNombre(nombre: String): String =
    nombre.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .map { it.first().toString().uppercase() }
        .joinToString("")
        .ifEmpty { "A" }
        .take(2)

// Calcula el promedio ponderado por porcentaje.
private fun calcularPromedio(notas: List<Notas>): Double {
    val totalPeso = notas.sumOf { it.porcentaje ?: 0.0 }
    if (totalPeso <= 0.0) return 0.0
    val ponderado = notas.sumOf { (it.nota ?: 0.0) * (it.porcentaje ?: 0.0) }
    return ponderado / totalPeso
}

private data class ResumenNotas(val media: Double?, val totalNotas: Int)

private fun calcularResumenNotas(notas: List<Notas>): ResumenNotas {
    if (notas.isEmpty()) {
        return ResumenNotas(media = null, totalNotas = 0)
    }
    val totalPeso = notas.sumOf { it.porcentaje ?: 0.0 }
    if (totalPeso <= 0.0) {
        return ResumenNotas(media = null, totalNotas = notas.size)
    }
    val ponderado = notas.sumOf { (it.nota ?: 0.0) * (it.porcentaje ?: 0.0) }
    return ResumenNotas(media = ponderado / totalPeso, totalNotas = notas.size)
}

private fun actualizarMediaAsignatura(userId: String, anioId: String?, asignaturaId: String, resumen: ResumenNotas) {
    if (userId.isBlank() || anioId.isNullOrBlank() || asignaturaId.isBlank()) return
    val ref = com.example.edutrack.asignaturaRef(userId, anioId, asignaturaId)
    ref.child("media").setValue(resumen.media)
    ref.child("numero_notas").setValue(resumen.totalNotas)
}
