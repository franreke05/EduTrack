package com.example.edutrack.Inicio

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.edutrack.data.educationLevelFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.edutrack.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.CrearAnio
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.ui.theme.EduTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreacionAnioScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit = {},
    userId: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var mostrarDialoginicio by remember { mutableStateOf(false) }
    var mostrarDialogfin by remember { mutableStateOf(false) }
    var numero_asignaturas by remember { mutableStateOf("") }
    var notaMinima by remember { mutableStateOf(5.0) }
    var tipoPonderacion by remember { mutableStateOf("creditos") }
    var tipoPeriodo by remember { mutableStateOf("Cuatrimestre") }

    val context = LocalContext.current
    val educationLevel by context.educationLevelFlow().collectAsState(initial = "")
    var defaultsApplied by remember { mutableStateOf(false) }
    LaunchedEffect(educationLevel) {
        if (!defaultsApplied && educationLevel.isNotEmpty()) {
            val d = defaultsForEducationLevel(educationLevel)
            tipoPonderacion = d.tipoPonderacion
            tipoPeriodo = d.tipoPeriodo
            defaultsApplied = true
        }
    }

    val notasOpciones = listOf(4.0, 4.5, 5.0, 5.5, 6.0)

    fun guardar() {
        if (comprobarCampos(numero_asignaturas, nombre, fechaInicio, fechaFin)) {
            val uid = userId
                ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: return
            val anio = Anio(
                nombre = nombre,
                descripcion = descripcion,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                numero_asignaturas = numero_asignaturas.toIntOrNull() ?: 0,
                nota_minima_aprobado = notaMinima,
                tipo_ponderacion = tipoPonderacion,
                tipo_periodo = tipoPeriodo
            )
            CrearAnio(uid, anio)
            onFinish()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.creacion_anio_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.creacion_anio_subtitle), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.creacion_anio_back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = ::guardar) {
                        Icon(Icons.Default.Done, contentDescription = stringResource(R.string.creacion_anio_save_cd), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección 1: Información básica
            FormSection(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.creacion_anio_section1_title),
                subtitle = stringResource(R.string.creacion_anio_section1_subtitle)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.creacion_anio_field_nombre)) },
                    placeholder = { Text(stringResource(R.string.creacion_anio_field_nombre_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(stringResource(R.string.creacion_anio_field_descripcion)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }

            // Sección 2: Periodo académico
            FormSection(
                icon = Icons.Default.CalendarMonth,
                title = stringResource(R.string.creacion_anio_section2_title),
                subtitle = stringResource(R.string.creacion_anio_section2_subtitle)
            ) {
                OutlinedTextField(
                    value = fechaInicio,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.creacion_anio_field_fecha_inicio)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarDialoginicio = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.creacion_anio_date_cd), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.creacion_anio_field_fecha_fin)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarDialogfin = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = stringResource(R.string.creacion_anio_date_cd), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }

            // Sección 3: Asignaturas
            FormSection(
                icon = Icons.Default.FormatListNumbered,
                title = stringResource(R.string.creacion_anio_section3_title),
                subtitle = stringResource(R.string.creacion_anio_section3_subtitle)
            ) {
                OutlinedTextField(
                    value = numero_asignaturas,
                    onValueChange = { nuevo ->
                        val filtrado = nuevo.filter { it.isDigit() }
                        if (filtrado.length <= 2) {
                            if (filtrado.isEmpty() || filtrado.toInt() <= 20) {
                                numero_asignaturas = filtrado
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.creacion_anio_field_num_asig)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Sección 4: Tipo de periodo
            FormSection(
                icon = Icons.Default.DateRange,
                title = stringResource(R.string.creacion_anio_section4_title),
                subtitle = stringResource(R.string.creacion_anio_section4_subtitle)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Cuatrimestre", "Trimestre").forEach { opcion ->
                        FilterChip(
                            selected = tipoPeriodo == opcion,
                            onClick = { tipoPeriodo = opcion },
                            label = {
                                Text(
                                    opcion,
                                    fontWeight = if (tipoPeriodo == opcion) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Sección 5: Cálculo de medias
            FormSection(
                icon = Icons.Default.Calculate,
                title = stringResource(R.string.creacion_anio_section5_title),
                subtitle = stringResource(R.string.creacion_anio_section5_subtitle)
            ) {
                // Nota mínima para aprobar
                Text(
                    text = stringResource(R.string.creacion_anio_nota_minima_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.creacion_anio_nota_minima_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    notasOpciones.forEach { nota ->
                        FilterChip(
                            selected = notaMinima == nota,
                            onClick = { notaMinima = nota },
                            label = {
                                Text(
                                    text = String.format("%.1f", nota),
                                    fontWeight = if (notaMinima == nota) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Tipo de ponderación
                Text(
                    text = stringResource(R.string.creacion_anio_ponderacion_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.creacion_anio_ponderacion_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tipoPonderacion == "creditos",
                        onClick = { tipoPonderacion = "creditos" },
                        label = {
                            Text(
                                stringResource(R.string.creacion_anio_ponderacion_creditos),
                                fontWeight = if (tipoPonderacion == "creditos") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = tipoPonderacion == "simple",
                        onClick = { tipoPonderacion = "simple" },
                        label = {
                            Text(
                                stringResource(R.string.creacion_anio_ponderacion_simple),
                                fontWeight = if (tipoPonderacion == "simple") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                // Info box
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (tipoPonderacion == "creditos")
                            stringResource(R.string.creacion_anio_formula_creditos)
                        else
                            stringResource(R.string.creacion_anio_formula_simple),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Botón principal de guardado
            Button(
                onClick = ::guardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(stringResource(R.string.creacion_anio_btn_crear), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    }
}

// Sección reutilizable con icono, título, subtítulo y contenido en card.
@Composable
private fun FormSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrearAnioScreenPreview() {
    EduTrackTheme {
        CreacionAnioScreen()
    }
}
