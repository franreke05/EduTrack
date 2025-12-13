package com.example.edutrack.Notas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.borrarAsignaturaCompleta
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

class NotasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val asignaturaId = intent.getStringExtra("ASIGNATURA_ID") ?: ""
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
                    asignaturaNombre = asignaturaNombre,
                    tipoPeriodo = tipoPeriodo,
                    numeroPeriodos = numeroPeriodos,
                    onBack = { finish() }
                )
            }
        }
    }
}
@Preview
@Composable
fun NotasScreen(
    asignaturaId: String,
    asignaturaNombre: String,
    tipoPeriodo: String,
    numeroPeriodos: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notasState = remember { mutableStateOf<List<Notas>>(emptyList()) }
    val showDialog = remember { mutableStateOf(false) }
    val notaEnEdicion = remember { mutableStateOf<Notas?>(null) }
    val showDeleteConfirm = remember { mutableStateOf<Notas?>(null) }
    var showDeleteAsignatura by remember { mutableStateOf(false) }
    idAsignatura = asignaturaId

    DisposableEffect(asignaturaId) {
        val notasRef = Firebase.database.reference
            .child("Edutrack")
            .child("Asignatura")
            .child(asignaturaId)
            .child("Notas")

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

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val promedio = calcularPromedio(notasState.value)
    val porcentajeTotal = notasState.value.sumOf { it.porcentaje ?: 0.0 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(asignaturaNombre) },
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
                    screenHeight = screenHeight
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
                guardarNota(asignaturaId, nota) {
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
                        eliminarNota(asignaturaId, nota)
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
                        borrarAsignaturaCompleta(asignaturaId = asignaturaId, anioId = anioId)
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

@Composable
private fun EncabezadoNotas(nombre: String, promedio: Double, porcentajeTotal: Double, screenHeight: androidx.compose.ui.unit.Dp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .size(screenHeight * 0.2f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = abreviarNombre(nombre),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Media", style = MaterialTheme.typography.labelMedium)
                    Text(text = String.format("%.2f", promedio), style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Porcentaje usado", style = MaterialTheme.typography.labelMedium)
                    Text(text = String.format("%.0f%%", porcentajeTotal), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

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

@Composable
private fun NotaRow(nota: Notas, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onEditar() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (nota == null) "Agregar nota" else "Editar nota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre.value, onValueChange = { nombre.value = it }, label = { Text("Nombre del examen") })
                OutlinedTextField(value = fecha.value, onValueChange = { fecha.value = it }, label = { Text("Fecha") })
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
                    .filter { it.periodo == periodo.value && it.id != nota?.id }
                    .sumOf { it.porcentaje ?: 0.0 }
                if (porcentajeUsado + porcentajeDouble > 100.0 + 1e-6) {
                    Toast.makeText(context, "El porcentaje total del periodo supera 100%.", Toast.LENGTH_LONG).show()
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

private fun guardarNota(asignaturaId: String, nota: Notas, onFinish: () -> Unit) {
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
    Firebase.database.reference
        .child("Edutrack")
        .child("Asignatura")
        .child(asignaturaId)
        .child("Notas")
        .child(notaId)
        .setValue(notaFinal)
        .addOnCompleteListener { onFinish() }
}

private fun eliminarNota(asignaturaId: String, nota: Notas) {
    val notaId = nota.id ?: return
    Firebase.database.reference
        .child("Edutrack")
        .child("Asignatura")
        .child(asignaturaId)
        .child("Notas")
        .child(notaId)
        .removeValue()
}

private fun abreviarNombre(nombre: String): String =
    nombre.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .map { it.first().toString().uppercase() }
        .joinToString("")
        .ifEmpty { "A" }
        .take(2)

private fun calcularPromedio(notas: List<Notas>): Double {
    val totalPeso = notas.sumOf { it.porcentaje ?: 0.0 }
    if (totalPeso <= 0.0) return 0.0
    val ponderado = notas.sumOf { (it.nota ?: 0.0) * (it.porcentaje ?: 0.0) }
    return ponderado / totalPeso
}
