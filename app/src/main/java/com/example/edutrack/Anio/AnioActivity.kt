package com.example.edutrack.Anio

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.Inicio.toRoman
import com.example.edutrack.Perfil.PerfilActivity
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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
    val context = LocalContext.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var showSearch by remember(anio.id) { mutableStateOf(false) }
    var searchQuery by remember(anio.id) { mutableStateOf("") }
    var sortOption by remember(anio.id) { mutableStateOf("NONE") }
    var showEditDialog by remember { mutableStateOf(false) }
    var newAnioName by remember { mutableStateOf(anio.nombre ?: "") }
    var showFilterMenu by remember(anio.id) { mutableStateOf(false) }

    val baseAsignaturas = anio.lista_asignaturas ?: emptyList()
    val filteredBySearch = if (searchQuery.isNotBlank()) {
        baseAsignaturas.filter { it.nombre?.contains(searchQuery, ignoreCase = true) == true }
    } else {
        baseAsignaturas
    }
    val asignaturasOrdenadas = when (sortOption) {
        "NAME_ASC" -> filteredBySearch.sortedBy { it.nombre ?: "" }
        "NAME_DESC" -> filteredBySearch.sortedByDescending { it.nombre ?: "" }
        else -> filteredBySearch
    }

    val maxAsignaturas = anio.numero_asignaturas ?: 0
    val anioLleno = (anio.lista_asignaturas?.size ?: 0) >= maxAsignaturas

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("EduTrack") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, PerfilActivity::class.java))
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) {
                            searchQuery = ""
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = {
                        if (!anioLleno) {
                            val sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE)
                            val idUser = sharedPreferences.getString("USER", "")
                            val listaActual = anio.lista_asignaturas?.toMutableList() ?: mutableListOf()
                            if (listaActual.isEmpty()) {
                                val asignatura1 = com.example.edutrack.dataclass.Asignatura(
                                    nombre = "Asignatura de ejemplo 1",
                                    creditos = 6,
                                    descripcion = "Primera asignatura de ejemplo",
                                    id_usuario = idUser,
                                    id_anio = anio.id
                                )
                                val asignatura2 = com.example.edutrack.dataclass.Asignatura(
                                    nombre = "Asignatura de ejemplo 2",
                                    creditos = 4,
                                    descripcion = "Segunda asignatura de ejemplo",
                                    id_usuario = idUser,
                                    id_anio = anio.id
                                )
                                com.example.edutrack.CrearAsignatura(asignatura1)
                                com.example.edutrack.CrearAsignatura(asignatura2)
                                listaActual.add(asignatura1)
                                listaActual.add(asignatura2)
                                val anioId = anio.id
                                if (!anioId.isNullOrEmpty()) {
                                    Firebase.database.reference
                                        .child("Edutrack")
                                        .child("Anio")
                                        .child(anioId)
                                        .child("lista_asignaturas")
                                        .setValue(listaActual)
                                }
                                Toast.makeText(context, "Se han creado 2 asignaturas de ejemplo", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ya hay asignaturas en este año", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, enabled = !anioLleno) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Asignatura", tint = if (anioLleno) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(
                    horizontal = screenWidth * 0.04f,
                    vertical = screenHeight * 0.02f
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .height(screenHeight * 0.07f)
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

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar Asignaturas")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin ordenar") },
                            onClick = {
                                sortOption = "NONE"
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Nombre A-Z") },
                            onClick = {
                                sortOption = "NAME_ASC"
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Nombre Z-A") },
                            onClick = {
                                sortOption = "NAME_DESC"
                                showFilterMenu = false
                            }
                        )
                    }
                }
                Text(anio.nombre ?: "", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = { showEditDialog = true }) {
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

            Spacer(modifier = Modifier.height(screenHeight * 0.02f))
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar asignatura...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(screenHeight * 0.02f))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f),
                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.02f)
            ) {
                itemsIndexed(asignaturasOrdenadas) { index, asignatura ->
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

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar nombre del año") },
            text = {
                OutlinedTextField(
                    value = newAnioName,
                    onValueChange = { newAnioName = it },
                    label = { Text("Nombre del año") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val anioId = anio.id
                    if (!anioId.isNullOrEmpty() && newAnioName.isNotBlank()) {
                        Firebase.database.reference
                            .child("Edutrack")
                            .child("Anio")
                            .child(anioId)
                            .child("nombre")
                            .setValue(newAnioName)
                    }
                    showEditDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AsignaturaCard(index: Int, asignatura: com.example.edutrack.dataclass.Asignatura) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = { /* TODO: Navegar a la lista de notas */ },
        elevation = CardDefaults.cardElevation(defaultElevation = screenHeight * 0.01f)
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
