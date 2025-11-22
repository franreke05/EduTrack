package com.example.edutrack.Anio

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.Inicio.toRoman
import com.example.edutrack.ui.theme.EduTrackTheme

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
fun AnioScreenWrapper() {
    //Inicializamos las sharedpreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE) //SharedPreferences
    val id_user = sharedPreferences.getString("USER", "") // Id del usuario
    //EL initialAnioId es el año que ha pulsado el usuario para ver sus asignaturas
    val initialAnioId = sharedPreferences.getString("id_anio", " ")

    val anios by rememberAniosState(id_user)

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
fun AnioScreen(modifier: Modifier = Modifier, anio: com.example.edutrack.dataclass.Anio, pageIndex: Int) {
    var showDescriptionDialog by remember { mutableStateOf(false) }

    // Lógica para desactivar el botón de añadir
    val maxAsignaturas = anio.numero_asignaturas ?: 0
    val anioLleno = (anio.lista_asignaturas?.size ?: 0) >= maxAsignaturas

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("EduTrack") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Volver a Inicio */ }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Ir a Perfil */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = { /* TODO: Buscar Asignatura */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { /* TODO: Agregar Asignatura */ }, enabled = !anioLleno) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Asignatura", tint = if (anioLleno) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface)
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
                Text(anio.nombre ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = { /* TODO: Editar nombre del año */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Nombre")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showDescriptionDialog = true }
            ) {
                Text("Descripción", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Mostrar Descripción", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(anio.lista_asignaturas ?: emptyList()) { index, asignatura ->
                    AsignaturaCard(index + 1, asignatura)
                }
            }
        }
    }

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("Descripción de ${anio.nombre}") },
            text = { Text(anio.descripcion ?: "No hay descripción.") },
            confirmButton = {
                TextButton(onClick = { showDescriptionDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun AsignaturaCard(index: Int, asignatura: com.example.edutrack.dataclass.Asignatura) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = { /* TODO: Navegar a la lista de notas */ },
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



@Preview(showBackground = true)
@Composable
fun AnioScreenPreview() {
    EduTrackTheme {
        val mockAsignaturas = List(8) { com.example.edutrack.dataclass.Asignatura(id="$it", nombre="Asignatura ${it + 1}") }
        val mockAnio = com.example.edutrack.dataclass.Anio(id="1", nombre="Año 2023-2024", descripcion="Descripción de prueba", lista_asignaturas = mockAsignaturas, numero_asignaturas = 8)
        AnioScreen(anio = mockAnio, pageIndex = 0)
    }
}
