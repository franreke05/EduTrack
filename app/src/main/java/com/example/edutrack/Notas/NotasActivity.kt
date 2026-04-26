package com.example.edutrack.Notas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.Inicio.SelectorDeFecha
import com.example.edutrack.borrarAsignaturaCompleta
import com.example.edutrack.core.FreemiumLimits
import com.example.edutrack.data.firebase.FirebasePaths
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.domain.grades.GradeSimulationResult
import com.example.edutrack.domain.grades.calculateRequiredGrade
import com.example.edutrack.eliminarNotaSegura
import com.example.edutrack.guardarNotaSegura
import com.example.edutrack.ui.components.EdutrackButton
import com.example.edutrack.ui.components.EdutrackCard
import com.example.edutrack.ui.components.EmptyState
import com.example.edutrack.ui.components.LimitReachedDialog
import com.example.edutrack.ui.components.MetricPill
import com.example.edutrack.ui.components.ProfessionalTopBar
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Locale
import java.util.UUID

// Activity que muestra la pantalla de notas de una asignatura.
class NotasActivity : ComponentActivity() {
    // Lee los parametros de la intent y abre la UI de notas.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val asignaturaId = intent.getStringExtra("ASIGNATURA_ID") ?: ""
        val yearId = intent.getStringExtra("YEAR_ID") ?: anioId
        val asignaturaNombre = intent.getStringExtra("ASIGNATURA_NOMBRE") ?: "Asignatura"
        val tipoPeriodo = intent.getStringExtra("TIPO_PERIODO") ?: "Trimestre"
        val numeroPeriodos = intent.getIntExtra("NUMERO_PERIODOS", 3)

        if (asignaturaId.isEmpty()) {
            Toast.makeText(this, "Asignatura no encontrada.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            EduTrackTheme {
                NotasScreen(
                    asignaturaId = asignaturaId,
                    yearId = yearId,
                    asignaturaNombre = asignaturaNombre,
                    tipoPeriodo = tipoPeriodo,
                    numeroPeriodos = numeroPeriodos,
                    onBack = { finish() }
                )
            }
        }
    }
}

// Id del anio actual usado al borrar la asignatura completa.
var anioId = ""

// Pantalla principal para gestionar notas por periodo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    asignaturaId: String,
    yearId: String,
    asignaturaNombre: String,
    tipoPeriodo: String,
    numeroPeriodos: Int,
    isPremium: Boolean = false,
    hasSimulatorAccess: Boolean = isPremium,
    onPremiumRequested: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notasState = remember { mutableStateOf<List<Notas>>(emptyList()) }
    val showDialog = remember { mutableStateOf(false) }
    val notaEnEdicion = remember { mutableStateOf<Notas?>(null) }
    val showDeleteConfirm = remember { mutableStateOf<Notas?>(null) }
    var showDeleteAsignatura by remember { mutableStateOf(false) }
    var targetAverage by remember { mutableStateOf("") }
    var simulationResult by remember { mutableStateOf<GradeSimulationResult?>(null) }
    var showSimulatorLimit by remember { mutableStateOf(false) }

    DisposableEffect(asignaturaId) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid.isNullOrBlank() || yearId.isBlank()) {
            notasState.value = emptyList()
            onDispose {}
        } else {
        val notasRef = Firebase.database.reference
            .child("users")
            .child(uid)
            .child("years")
            .child(yearId)
            .child("subjects")
            .child(asignaturaId)
            .child("notes")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(Notas::class.java) }
                notasState.value = lista
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error leyendo notas", Toast.LENGTH_SHORT).show()
            }
        }
        notasRef.addValueEventListener(listener)
        onDispose { notasRef.removeEventListener(listener) }
        }
    }

    val promedio = calcularPromedio(notasState.value)
    val porcentajeTotal = notasState.value.sumOf { it.percentage ?: it.porcentaje ?: 0.0 }

    Scaffold(
        topBar = {
            ProfessionalTopBar(
                title = asignaturaNombre,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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

                SimuladorNotaCard(
                    notas = notasState.value,
                    targetAverage = targetAverage,
                    onTargetChange = { targetAverage = it },
                    result = simulationResult,
                    hasAccess = hasSimulatorAccess,
                    onCalculate = {
                        simulationResult = calculateRequiredGrade(
                            notasState.value,
                            targetAverage.toDoubleOrNull() ?: -1.0
                        )
                    },
                    onLockedClick = { showSimulatorLimit = true }
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
                val uid = Firebase.auth.currentUser?.uid
                if (uid.isNullOrBlank() || yearId.isBlank()) {
                    Toast.makeText(context, "Sesion no valida.", Toast.LENGTH_SHORT).show()
                } else {
                    guardarNota(yearId, asignaturaId, nota) { success ->
                        if (!success) {
                            Toast.makeText(context, "No se pudo guardar. Revisa el porcentaje total.", Toast.LENGTH_LONG).show()
                        }
                        showDialog.value = false
                    }
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
                        eliminarNota(yearId, asignaturaId, nota)
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
                        borrarAsignaturaCompleta(asignaturaId = asignaturaId, anioId = yearId.ifBlank { anioId })
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

    if (showSimulatorLimit) {
        LimitReachedDialog(
            title = "Simulador Premium",
            message = "Premium desbloquea el cálculo de nota necesaria para tus próximas pruebas.",
            onDismiss = { showSimulatorLimit = false },
            onUnlockPremium = {
                showSimulatorLimit = false
                onPremiumRequested()
            }
        )
    }
}

// Encabezado con promedio y porcentaje total usado.
@Composable
private fun EncabezadoNotas(nombre: String, promedio: Double, porcentajeTotal: Double) {
    val context = LocalContext.current

    EdutrackCard(modifier = Modifier.fillMaxWidth(), elevated = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(74.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = abreviarNombre(nombre),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Seguimiento por pruebas y porcentajes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPill(label = "Media", value = String.format("%.2f", promedio), modifier = Modifier.weight(1f))
                MetricPill(label = "Usado", value = String.format("%.0f%%", porcentajeTotal), modifier = Modifier.weight(1f))
                if (porcentajeTotal > 100) {
                    IconButton(onClick = {
                        Toast.makeText(context, "El porcentaje total supera el 100%.", Toast.LENGTH_LONG).show()
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Información")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimuladorNotaCard(
    notas: List<Notas>,
    targetAverage: String,
    onTargetChange: (String) -> Unit,
    result: GradeSimulationResult?,
    hasAccess: Boolean,
    onCalculate: () -> Unit,
    onLockedClick: () -> Unit
) {
    EdutrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Simulador de nota necesaria", style = MaterialTheme.typography.titleMedium)
            if (!hasAccess) {
                Text(
                    "Disponible con Premium.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                EdutrackButton("Desbloquear Premium", onClick = onLockedClick, modifier = Modifier.fillMaxWidth())
                return@Column
            }
            OutlinedTextField(
                value = targetAverage,
                onValueChange = { value -> onTargetChange(value.filter { it.isDigit() || it == '.' }) },
                label = { Text("Media objetivo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            EdutrackButton("Calcular", onClick = onCalculate, modifier = Modifier.fillMaxWidth())
            val message = when (result) {
                is GradeSimulationResult.Required -> "Necesitas un ${String.format(Locale.getDefault(), "%.1f", result.grade)}"
                GradeSimulationResult.AlreadyEnough -> "Ya tienes suficiente"
                GradeSimulationResult.Impossible -> "No es posible con el porcentaje restante"
                GradeSimulationResult.NoRemainingPercentage -> "No queda porcentaje disponible"
                GradeSimulationResult.InvalidTarget -> "Introduce una media objetivo entre 0 y 10"
                null -> "Porcentaje restante: ${String.format(Locale.getDefault(), "%.0f%%", FreemiumLimits.MAX_TOTAL_GRADE_PERCENT - notas.sumOf { it.porcentaje ?: 0.0 })}"
            }
            Text(message, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
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


// Lista de notas agrupadas por periodo.
@Composable
private fun ListaNotasPorPeriodo(
    notas: List<Notas>,
    numeroPeriodos: Int,
    tipoPeriodo: String,
    onEditar: (Notas) -> Unit,
    onEliminar: (Notas) -> Unit
) {
    if (notas.isEmpty()) {
        EmptyState(
            title = "Registra tu primera nota",
            message = "Añade exámenes, trabajos o prácticas con su porcentaje."
        )
        return
    }
    val grouped = notas.groupBy { it.periodo ?: 1 }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        (1..numeroPeriodos).forEach { periodo ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Text(
                        text = "$tipoPeriodo $periodo",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                    )
                }
            }
            val lista = grouped[periodo].orEmpty()
            items(lista) { nota ->
                NotaRow(nota, onEditar = { onEditar(nota) }, onEliminar = { onEliminar(nota) })
                Divider()
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}

// Fila de nota con acciones de editar y eliminar.
@Composable
private fun NotaRow(nota: Notas, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onEditar() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nota.nombre ?: "Examen", style = MaterialTheme.typography.bodyLarge)
                Text(text = nota.fecha.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                Text(text = String.format("%.1f", nota.nota ?: 0.0), fontWeight = FontWeight.Bold)
                Text(text = "${nota.porcentaje ?: 0.0}%", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Dialogo para crear o editar una nota.
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
    val notaValor = remember { mutableStateOf((nota?.nota ?: 0.0).toString()) }
    val porcentaje = remember { mutableStateOf((nota?.porcentaje ?: 0.0).toString()) }
    val periodo = remember { mutableStateOf(nota?.periodo ?: 1) }
    val context = LocalContext.current
    var mostrarDialoginicio by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (nota == null) "Agregar nota" else "Editar nota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre.value, onValueChange = { nombre.value = it }, label = { Text("Nombre del examen") })
                OutlinedTextField(
                    value = fecha.value,
                    onValueChange = { },
                    label = { Text("Fecha de Inicio") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { mostrarDialoginicio = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Abrir calendario",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                if (mostrarDialoginicio) {
                    SelectorDeFecha(
                        onFechaSeleccionada = { fecha2 -> fecha.value = fecha2 },
                        onDismiss = { mostrarDialoginicio = false }
                    )

                }
                OutlinedTextField(value = notaValor.value, onValueChange = { notaValor.value = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Nota (0-10)") })
                OutlinedTextField(value = porcentaje.value, onValueChange = { porcentaje.value = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Porcentaje (0-100)") })
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Periodo:")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (p in 1..numeroPeriodos) {
                            val selected = periodo.value == p
                            Button(
                                onClick = { periodo.value = p },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            ) { Text("$p") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val notaDouble = notaValor.value.toDoubleOrNull() ?: -1.0
                val porcentajeDouble = porcentaje.value.toDoubleOrNull() ?: -1.0
                if (nombre.value.isBlank() || notaDouble < 0 || porcentajeDouble <= 0) {
                    Toast.makeText(context, "Completa los campos correctamente.", Toast.LENGTH_LONG).show()
                    return@TextButton
                }
                val porcentajeUsado = notasActuales
                    .filter { it.id != nota?.id }
                    .sumOf { it.porcentaje ?: 0.0 }
                if (porcentajeUsado + porcentajeDouble > FreemiumLimits.MAX_TOTAL_GRADE_PERCENT + 1e-6) {
                    Toast.makeText(context, "El porcentaje total de notas no puede superar 100%.", Toast.LENGTH_LONG).show()
                    return@TextButton
                }
                val nuevaNota = NotaConstruida(
                    base = nota,
                    nombre = nombre.value,
                    fecha = fecha.value,
                    nota = notaDouble,
                    porcentaje = porcentajeDouble,
                    periodo = periodo.value
                )
                onSave(nuevaNota)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
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
private fun guardarNota(yearId: String, asignaturaId: String, nota: Notas, onFinish: (Boolean) -> Unit) {
    val uid = Firebase.auth.currentUser?.uid
    if (uid.isNullOrBlank() || yearId.isBlank()) {
        onFinish(false)
        return
    }
    val notaId = nota.id ?: UUID.randomUUID().toString()
    val notaFinal = Notas(
        id = notaId,
        id_asignatura = asignaturaId,
        nombre = nota.nombre,
        nota = nota.nota,
        porcentaje = nota.porcentaje,
        fecha = nota.fecha,
        periodo = nota.periodo,
        ownerId = uid,
        yearId = yearId,
        subjectId = asignaturaId,
        name = nota.nombre,
        grade = nota.nota,
        percentage = nota.porcentaje,
        period = nota.periodo
    )
    guardarNotaSegura(uid, yearId, asignaturaId, notaFinal, onFinish)
}

// Elimina una nota de Firebase.
private fun eliminarNota(yearId: String, asignaturaId: String, nota: Notas) {
    val uid = Firebase.auth.currentUser?.uid ?: return
    val notaId = nota.id ?: return
    eliminarNotaSegura(uid, yearId, asignaturaId, notaId)
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
    val totalPeso = notas.sumOf { it.percentage ?: it.porcentaje ?: 0.0 }
    if (totalPeso <= 0.0) return 0.0
    val ponderado = notas.sumOf { (it.grade ?: it.nota ?: 0.0) * (it.percentage ?: it.porcentaje ?: 0.0) }
    return ponderado / totalPeso
}
