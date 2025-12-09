package com.example.edutrack.Anio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.Inicio.toRoman
import com.example.edutrack.Notas.NotasActivity
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun AnioRoute(
    userId: String?,
    anioId: String?,
    onBack: () -> Unit = {},
    onOpenNotas: (Asignatura) -> Unit = {}
) {
    val anios by rememberAniosState(userId)
    val anio = anios.firstOrNull { it.id == anioId }
    if (anio == null) {
        androidx.compose.material3.CircularProgressIndicator()
    } else {
        AnioScreen(anio = anio, pageIndex = anios.indexOf(anio), onBack = onBack, onOpenNotas = onOpenNotas)
    }
}

class AnioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EduTrackTheme {
                AnioScreenWrapper()
            }
        }
    }
}

@Composable
fun AnioScreenWrapper(userId: String?, initialAnioId: String?) {
    val anios by rememberAniosState(userId)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnioScreen(
    modifier: Modifier = Modifier,
    anio: com.example.edutrack.dataclass.Anio,
    pageIndex: Int,
    onBack: () -> Unit = {},
    onOpenNotas: (Asignatura) -> Unit = {}
) {
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showAsignaturaDialog by remember { mutableStateOf(false) }

    val maxAsignaturas = anio.numero_asignaturas ?: 0
    val anioLleno = (anio.lista_asignaturas?.size ?: 0) >= maxAsignaturas

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("EduTrack") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Perfil */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = { /* TODO: Buscar Asignatura */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { showAsignaturaDialog = true }, enabled = !anioLleno) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar Asignatura",
                            tint = if (anioLleno) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .aspectRatio(1.5f)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = toRoman(pageIndex + 1),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* TODO: Filtrar asignaturas */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtrar Asignaturas")
                }
                Text(
                    anio.nombre ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { /* TODO: Editar nombre del aヵo */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Nombre")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showDescriptionDialog = true }
            ) {
                Text("DescripciИn", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Mostrar DescripciИn", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(anio.lista_asignaturas ?: emptyList()) { index, asignatura ->
                    AsignaturaCard(index + 1, asignatura) { onOpenNotas(asignatura) }
                }
            }
        }
    }

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("DescripciИn de ${anio.nombre}") },
            text = { Text(anio.descripcion ?: "No hay descripciИn.") },
            confirmButton = {
                TextButton(onClick = { showDescriptionDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showAsignaturaDialog) {
        CrearAsignaturaDialog(
            anioId = anio.id,
            idUsuario = anio.id_user,
            onDismiss = { showAsignaturaDialog = false }
        )
    }
}

@Composable
fun AsignaturaCard(index: Int, asignatura: Asignatura, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "$index", style = MaterialTheme.typography.titleLarge)
            Text(text = abbreviateName(asignatura.nombre ?: ""), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CrearAsignaturaDialog(anioId: String?, idUsuario: String?, onDismiss: () -> Unit) {
    val nombre = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val creditos = remember { mutableStateOf("") }
    val tipo = remember { mutableStateOf("Trimestre") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva asignatura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre.value, onValueChange = { nombre.value = it }, label = { Text("Nombre") })
                OutlinedTextField(value = descripcion.value, onValueChange = { descripcion.value = it }, label = { Text("DescripciИn") })
                OutlinedTextField(value = creditos.value, onValueChange = { creditos.value = it.filter { c -> c.isDigit() } }, label = { Text("CrИditos") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Tipo:")
                    listOf("Trimestre", "Cuatrimestre").forEach { opcion ->
                        val selected = tipo.value == opcion
                        Button(
                            onClick = { tipo.value = opcion },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text(opcion.take(3)) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (anioId.isNullOrEmpty() || nombre.value.isBlank()) {
                    Toast.makeText(context, "Completa el nombre del curso.", Toast.LENGTH_LONG).show()
                    return@TextButton
                }
                val creditosInt = creditos.value.toIntOrNull() ?: 0
                val numeroPeriodos = if (tipo.value == "Cuatrimestre") 4 else 3
                val dbRef = Firebase.database.reference
                val nuevoId = dbRef.child("Edutrack").child("Asignatura").push().key
                if (nuevoId == null) {
                    Toast.makeText(context, "No se pudo generar ID.", Toast.LENGTH_LONG).show()
                    return@TextButton
                }
                val asignatura = Asignatura(
                    id = nuevoId,
                    nombre = nombre.value,
                    descripcion = descripcion.value,
                    creditos = creditosInt,
                    id_usuario = idUsuario,
                    tipo_periodo = tipo.value,
                    numero_periodos = numeroPeriodos
                )
                dbRef.child("Edutrack").child("Asignatura").child(nuevoId).setValue(asignatura)
                dbRef.child("Edutrack").child("Anio").child(anioId).child("lista_asignaturas").child(nuevoId).setValue(asignatura)
                onDismiss()
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Preview(showBackground = true)
@Composable
fun AnioScreenPreview() {
    EduTrackTheme {
        val mockAsignaturas = List(8) { Asignatura(id = "$it", nombre = "Asignatura ${it + 1}") }
        val mockAnio = com.example.edutrack.dataclass.Anio(id = "1", nombre = "Aヵo 2023-2024", descripcion = "DescripciИn de prueba", lista_asignaturas = mockAsignaturas, numero_asignaturas = 8)
        AnioScreen(anio = mockAnio, pageIndex = 0)
    }
}
