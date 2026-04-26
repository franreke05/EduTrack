package com.example.edutrack.Anio

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.Inicio.toRoman
import com.example.edutrack.CrearAsignatura
import com.example.edutrack.core.FreemiumLimits
import com.example.edutrack.Notas.anioId
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.ui.components.EdutrackCard
import com.example.edutrack.ui.components.EmptyState
import com.example.edutrack.ui.components.LimitReachedDialog
import com.example.edutrack.ui.components.MetricPill
import com.example.edutrack.ui.components.ProfessionalTopBar
import com.example.edutrack.ui.theme.EduTrackTheme

// Ruta de entrada: carga el anio seleccionado y muestra un indicador mientras falta data.
@Composable
fun AnioRoute(
    userId: String?,
    anioId: String?,
    isPremium: Boolean = false,
    onBack: () -> Unit = {},
    onPremiumRequested: () -> Unit = {},
    onOpenNotas: (Asignatura) -> Unit = {}
) {
    val anios by rememberAniosState(userId)
    val anio = anios.firstOrNull { it.id == anioId }
    if (anio == null) {
        CircularProgressIndicator()
    } else {
        AnioScreen(
            anio = anio,
            pageIndex = anios.indexOf(anio),
            isPremium = isPremium,
            onBack = onBack,
            onPremiumRequested = onPremiumRequested,
            onOpenNotas = onOpenNotas
        )
    }
}

// Envuelve la pantalla del anio con navegacion por deslizamiento entre anios.
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

// Pantalla principal del anio: header, busqueda y grilla de asignaturas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnioScreen(
    modifier: Modifier = Modifier,
    anio: com.example.edutrack.dataclass.Anio,
    pageIndex: Int,
    isPremium: Boolean = false,
    onBack: () -> Unit = {},
    onPremiumRequested: () -> Unit = {},
    onOpenNotas: (Asignatura) -> Unit = {}
) {
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showAsignaturaDialog by remember { mutableStateOf(false) }
    val maxAsignaturas = anio.numero_asignaturas ?: 0
    val actuales = anio.lista_asignaturas?.size ?: 0
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val anioLleno = actuales >= maxAsignaturas
    val context = LocalContext.current
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showLimitDialog by remember { mutableStateOf(false) }
    val asignaturasBase = anio.lista_asignaturas?.values?.toList() ?: emptyList()
    val asignaturasFiltradas = if (showSearch && searchQuery.isNotBlank()) {
        asignaturasBase.filter { it.nombre?.contains(searchQuery, ignoreCase = true) == true }
    } else asignaturasBase
    val spacing = 16.dp

    Scaffold(
        topBar = {
            ProfessionalTopBar(
                title = anio.nombre ?: "Año escolar",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (anioLleno) {
                            showLimitDialog = true
                        } else {
                            showAsignaturaDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir asignatura",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar asignatura",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = spacing, vertical = spacing)
            ) {
                if (showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar asignatura") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing),
                        singleLine = true
                    )
                }

                EdutrackCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing),
                    elevated = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(92.dp)
                                .aspectRatio(1.45f)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(22.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = toRoman(pageIndex + 1),
                                style = MaterialTheme.typography.displaySmall,
                                color = if (anioLleno) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { showDescriptionDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Ver descripción",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = anio.nombre ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { /* Filtro reservado */ }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtrar asignaturas",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricPill("Asignaturas", "$actuales / $maxAsignaturas", modifier = Modifier.weight(1f))
                            MetricPill("Periodo", asignaturasBase.firstOrNull()?.tipo_periodo ?: "Sin definir", modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (asignaturasFiltradas.isEmpty()) {
                    EmptyState(
                        title = "Añade tu primera asignatura",
                        message = "Crea una asignatura para empezar a registrar notas."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(asignaturasFiltradas) { index, asignatura ->
                            AsignaturaCard(index + 1, asignatura) {
                                onOpenNotas(asignatura)
                                anioId = anio.id.toString()
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
            title = { Text("Descripción de ${anio.nombre}") },
            text = { Text(anio.descripcion ?: "No hay descripción.") },
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
            maxAsignaturas = maxAsignaturas,
            actuales = actuales,
            isPremium = isPremium,
            onLimitReached = { showLimitDialog = true },
            onDismiss = { showAsignaturaDialog = false }
        )
    }

    if (showLimitDialog) {
        LimitReachedDialog(
            title = "Límite de asignaturas",
            message = "La versión gratis permite 8 asignaturas por curso trimestral o 9 si trabajas por cuatrimestres. Premium desbloquea asignaturas ilimitadas.",
            onDismiss = { showLimitDialog = false },
            onUnlockPremium = {
                showLimitDialog = false
                onPremiumRequested()
            }
        )
    }
}

// Tarjeta para una asignatura individual.
@Composable
fun AsignaturaCard(index: Int, asignatura: Asignatura, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    text = abbreviateName(asignatura.nombre ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }
            Text(
                text = asignatura.nombre ?: "Asignatura $index",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// Recoge datos del usuario y crea una nueva asignatura.
@Composable
fun CrearAsignaturaDialog(
    anioId: String?,
    idUsuario: String?,
    maxAsignaturas: Int,
    actuales: Int,
    isPremium: Boolean = false,
    onLimitReached: () -> Unit = {},
    onDismiss: () -> Unit
) {
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
                OutlinedTextField(value = descripcion.value, onValueChange = { descripcion.value = it }, label = { Text("Descripción") })
                OutlinedTextField(value = creditos.value, onValueChange = { creditos.value = it.filter { c -> c.isDigit() } }, label = { Text("Créditos") })
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
                Text(
                    text = "Asignaturas actuales: $actuales / $maxAsignaturas",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (anioId.isNullOrEmpty() || nombre.value.isBlank()) {
                    Toast.makeText(context, "Completa el nombre de la asignatura", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                val freeLimit = FreemiumLimits.maxSubjectsForPeriodType(tipo.value)
                if ((!isPremium && actuales >= freeLimit) || actuales >= maxAsignaturas) {
                    onLimitReached()
                    return@TextButton
                }
                val creditosInt = creditos.value.toIntOrNull() ?: 0
                val numeroPeriodos = if (tipo.value == "Cuatrimestre") 4 else 3
                val asignatura = Asignatura(
                    nombre = nombre.value,
                    descripcion = descripcion.value,
                    creditos = creditosInt,
                    id_usuario = idUsuario,
                    id_anio = anioId,
                    tipo_periodo = tipo.value,
                    numero_periodos = numeroPeriodos,
                    ownerId = idUsuario,
                    yearId = anioId,
                    periodType = tipo.value,
                    totalPeriods = numeroPeriodos
                )
                CrearAsignatura(asignatura)
                onDismiss()
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
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
        AnioScreen(anio = mockAnio, pageIndex = 0)
    }
}
