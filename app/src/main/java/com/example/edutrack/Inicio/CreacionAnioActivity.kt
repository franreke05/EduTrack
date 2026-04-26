package com.example.edutrack.Inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.CrearAnio
import com.example.edutrack.core.FreemiumLimits
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.ui.components.LimitReachedDialog
import com.example.edutrack.ui.theme.EduTrackTheme

// Pantalla para crear un nuevo anio escolar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreacionAnioScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit = {},
    userId: String? = null,
    isPremium: Boolean = false,
    currentCourseCount: Int = 0,
    onPremiumRequested: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var mostrarDialoginicio by remember { mutableStateOf(false) }
    var mostrarDialogfin by remember { mutableStateOf(false) }
    var fechaFin by remember { mutableStateOf("") }
    var numero_asignaturas by remember { mutableStateOf("") }
    var showLimit by remember { mutableStateOf(false) }
    val currentCourses by rememberAniosState(userId)
    val effectiveCourseCount = maxOf(currentCourseCount, currentCourses.size)
    val spacing = 16.dp

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nuevo Año Escolar") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!isPremium && effectiveCourseCount >= FreemiumLimits.MAX_FREE_COURSES) {
                                showLimit = true
                                return@IconButton
                            }
                            if (comprobarCampos(numero_asignaturas, nombre, fechaInicio, fechaFin)) {
                                val anio = Anio(
                                    id = null,
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    fechaInicio = fechaInicio,
                                    fechaFin = fechaFin,
                                    numero_asignaturas.toInt(),
                                    id_user = userId,
                                    ownerId = userId
                                )
                                CrearAnio(anio)

                                onFinish()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Guardar",
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Text(
                    text = "Información del Año",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(spacing)) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del Curso (ej. 2024-2025)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                    }
                }

                Text(
                    text = "Duración",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(spacing)) {
                        OutlinedTextField(
                            value = fechaInicio,
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
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = fechaFin,
                            onValueChange = { },
                            label = { Text("Fecha de Fin") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { mostrarDialogfin = true }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Abrir calendario",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }

                if (mostrarDialoginicio) {
                    SelectorDeFecha(
                        onFechaSeleccionada = { fecha -> fechaInicio = fecha },
                        onDismiss = { mostrarDialoginicio = false }
                    )
                }
                if (mostrarDialogfin) {
                    SelectorDeFecha(
                        onFechaSeleccionada = { fecha -> fechaFin = fecha },
                        onDismiss = { mostrarDialogfin = false }
                    )
                }

                Text(
                    text = "Numero de Asignaturas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(spacing)) {
                        OutlinedTextField(
                            value = numero_asignaturas,
                            onValueChange = { nuevoValor ->
                                val textoFiltrado = nuevoValor.filter { it.isDigit() }
                                if (textoFiltrado.length <= 2) {
                                    if (textoFiltrado.isEmpty() || textoFiltrado.toInt() <= 20) {
                                        numero_asignaturas = textoFiltrado
                                    }
                                }
                            },
                            label = { Text("Numero de Asignaturas (máx. 20)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }

    if (showLimit) {
        LimitReachedDialog(
            title = "Límite de cursos",
            message = "La versión gratis permite hasta ${FreemiumLimits.MAX_FREE_COURSES} cursos.",
            onDismiss = { showLimit = false },
            onUnlockPremium = {
                showLimit = false
                onPremiumRequested()
            }
        )
    }
}

// Vista previa del formulario de creacion de anio.
@Preview(showBackground = true)
@Composable
fun CrearAnioScreenPreview() {
    EduTrackTheme {
        CreacionAnioScreen()
    }
}
