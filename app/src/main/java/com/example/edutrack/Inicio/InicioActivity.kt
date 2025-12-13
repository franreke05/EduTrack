package com.example.edutrack.Inicio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoInicio(
    modifier: Modifier = Modifier,
    userId: String?,
    onAnioSelected: (String?) -> Unit = {},
    onCrearAnio: () -> Unit = {},
    onPerfil: () -> Unit = {}
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val aniosFromFirebase by rememberAniosState(userId)
    var displayedAnios by remember { mutableStateOf<List<Anio>>(emptyList()) }
    val selectedAnios = remember { mutableStateMapOf<String, Anio>() }
    val porcentajes = remember { mutableStateMapOf<String, String>() }
    var mediaConjunta by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(aniosFromFirebase) {
        displayedAnios = aniosFromFirebase
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var anioToDelete by remember { mutableStateOf<Anio?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.04f),
            verticalArrangement = Arrangement.spacedBy(screenHeight * 0.02f)
        ) {
            Text(
                text = "Tus años académicos",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = screenHeight * 0.01f)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface
            ) {
                AnimationSearch(
                    initialAnios = aniosFromFirebase,
                    onAniosFiltered = { filteredList ->
                        displayedAnios = filteredList
                    },
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    onCrearAnio = onCrearAnio,
                    onPerfil = onPerfil
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(screenHeight * 0.015f),
                contentPadding = PaddingValues(bottom = screenHeight * 0.04f)
            ) {
                itemsIndexed(displayedAnios, key = { _, anio -> anio.id ?: anio.hashCode().toString() }) { index, anio ->
                    val mediaAnio = calcularMediaAnio(anio)
                    val isSelected = anio.id?.let { selectedAnios.containsKey(it) } == true
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    anio.id?.let { id ->
                                        if (isSelected) {
                                            selectedAnios.remove(id)
                                            porcentajes.remove(id)
                                        } else {
                                            selectedAnios[id] = anio
                                            porcentajes.putIfAbsent(id, "")
                                        }
                                        mediaConjunta = null
                                    }
                                    false
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    anioToDelete = anio
                                    showDeleteDialog = true
                                    false
                                }
                                else -> false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier.fillMaxWidth(),
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight * 0.08f)
                                    .background(color)
                                    .padding(horizontal = screenWidth * 0.05f),
                                contentAlignment = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    else -> Alignment.CenterEnd
                                }
                            ) {
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seleccionar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(screenHeight * 0.04f)
                                        )
                                    }
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.size(screenHeight * 0.04f)
                                        )
                                    }
                                    else -> { }
                                }
                            }
                        }
                    ) {
                        AnioCard(
                            anio = anio,
                            index = index + 1,
                            screenHeight = screenHeight,
                            screenWidth = screenWidth,
                            mediaAnio = mediaAnio,
                            isSelected = isSelected
                        ) {
                            onAnioSelected(anio.id)
                        }
                    }
                }
            }

            if (selectedAnios.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.02f),
                        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.015f)
                    ) {
                        Text(
                            text = "Años seleccionados",
                            style = MaterialTheme.typography.titleMedium
                        )

                        selectedAnios.values.forEach { anioSeleccionado ->
                            val id = anioSeleccionado.id ?: return@forEach
                            val textoPorcentaje = porcentajes[id] ?: ""
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.02f)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(anioSeleccionado.nombre ?: "Sin nombre", style = MaterialTheme.typography.bodyLarge)
                                    calcularMediaAnio(anioSeleccionado)?.let { media ->
                                        Text(
                                            text = "Media: ${formatMedia(media)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = textoPorcentaje,
                                    onValueChange = { nuevo ->
                                        if (nuevo.length <= 3 && nuevo.all { it.isDigit() }) {
                                            porcentajes[id] = nuevo
                                            mediaConjunta = null
                                        }
                                    },
                                    modifier = Modifier.width(screenWidth * 0.45f),
                                    singleLine = true,
                                    label = { Text("Porcentaje") },
                                    placeholder = { Text("0-100") },
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    suffix = { Text("%") }
                                )
                                TextButton(
                                    onClick = {
                                        selectedAnios.remove(id)
                                        porcentajes.remove(id)
                                        mediaConjunta = null
                                    }
                                ) {
                                    Text("Quitar")
                                }
                            }
                        }

                        Button(
                            onClick = {
                                mediaConjunta = calcularMediaConjunta(selectedAnios, porcentajes)
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Mostrar media en conjunto")
                        }

                        mediaConjunta?.let { resultado ->
                            Text(
                                text = "Media ponderada: ${formatMedia(resultado)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar el año '${anioToDelete?.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        anioToDelete?.id?.let { anioId ->
                            if (anioId.isNotEmpty()) {
                                Firebase.database.reference.child("Edutrack").child("Anio").child(anioId)
                                    .removeValue()
                            }
                        }
                        showDeleteDialog = false
                        anioToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AnioCard(
    anio: Anio,
    index: Int,
    screenHeight: Dp,
    screenWidth: Dp,
    mediaAnio: Double?,
    isSelected: Boolean,
    function: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.large
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large,
        onClick = function,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.02f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f)
        ) {
            Column(
                modifier = Modifier.width(screenWidth * 0.18f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier.size(screenHeight * 0.06f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = toRoman(index),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = mediaAnio?.let { "Media: ${formatMedia(it)}" } ?: "Media: --",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                anio.nombre?.let { Text(text = it, style = MaterialTheme.typography.titleMedium) }
                anio.descripcion?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                anio.numero_asignaturas?.let { Text(text = "Número de asignaturas: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            Column(
                modifier = Modifier.weight(0.4f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "Fecha inicio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                anio.fechaInicio?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                Text(text = "Fecha fin", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                anio.fechaFin?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {

        AnioCard( anio = Anio(), index = 1, screenHeight = 500.dp, screenWidth = 500.dp, mediaAnio = 1.0, isSelected = false, function = {})
    }
}

@Composable
fun CircularActionButton(icon: ImageVector, label: String, onClick: () -> Unit, screenHeight: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f)
    ) {
        Surface(
            modifier = Modifier.size(screenHeight * 0.07f),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 3.dp
        ) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(screenHeight * 0.045f)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(screenHeight * 0.09f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDeFecha(onFechaSeleccionada: (String) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    val fechaSeleccionada = datePickerState.selectedDateMillis?.let {
                        formatearFecha(it)
                    } ?: ""
                    onFechaSeleccionada(fechaSeleccionada)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

fun formatearFecha(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(timeInMillis))
}

fun calcularMediaAnio(anio: Anio): Double? {
    val asignaturas = anio.lista_asignaturas?.values ?: return null
    var sumaPonderada = 0.0
    var sumaPesos = 0.0

    asignaturas.forEach { asignatura ->
        val mediaAsignatura = asignatura.media ?: return@forEach
        val peso = asignatura.creditos?.takeIf { it > 0 } ?: 1
        sumaPonderada += mediaAsignatura * peso
        sumaPesos += peso
    }

    if (sumaPesos <= 0.0) return null
    return sumaPonderada / sumaPesos
}

fun formatMedia(media: Double): String =
    String.format(Locale.getDefault(), "%.2f", media)

fun calcularMediaConjunta(
    seleccionados: Map<String, Anio>,
    porcentajes: Map<String, String>
): Double? {
    var acumulado = 0.0
    var pesoTotal = 0.0

    seleccionados.values.forEach { anio ->
        val id = anio.id ?: return@forEach
        val peso = porcentajes[id]?.toDoubleOrNull() ?: 0.0
        val mediaAnio = calcularMediaAnio(anio) ?: return@forEach

        if (peso > 0) {
            acumulado += mediaAnio * peso
            pesoTotal += peso
        }
    }

    return if (pesoTotal > 0) acumulado / pesoTotal else null
}

@Composable
fun rememberAniosState(id_user: String?): State<List<Anio>> {

    val aniosState = remember { mutableStateOf<List<Anio>>(emptyList()) }
    val dbRef = Firebase.database.reference.child("Edutrack").child("Anio")

    DisposableEffect(id_user) {

        if (id_user.isNullOrEmpty()) {
            aniosState.value = emptyList()
            onDispose {}
        } else {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val aniosList = snapshot.children.mapNotNull { parseAnioSnapshot(it) }
                        .filter { it.id_user == id_user }
                    aniosState.value = aniosList
                }

                override fun onCancelled(error: DatabaseError) { }
            }
            dbRef.addValueEventListener(valueEventListener)

            onDispose {
                dbRef.removeEventListener(valueEventListener)
            }
        }
    }

    return aniosState
}

private fun parseAnioSnapshot(snapshot: DataSnapshot): Anio? {
    val id = snapshot.child("id").getValue(String::class.java)
    val nombre = snapshot.child("nombre").getValue(String::class.java)
    val descripcion = snapshot.child("descripcion").getValue(String::class.java)
    val fechaInicio = snapshot.child("fechaInicio").getValue(String::class.java)
    val fechaFin = snapshot.child("fechaFin").getValue(String::class.java)
    val numeroAsignaturas = snapshot.child("numero_asignaturas").getValue(Long::class.java)?.toInt()
    val idUser = snapshot.child("id_user").getValue(String::class.java)

    val asignaturasMap = snapshot.child("lista_asignaturas")
        .children
        .mapNotNull { child ->
            child.getValue(Asignatura::class.java)?.let { asignatura ->
                (child.key ?: asignatura.id ?: asignatura.nombre)?.let { key -> key to asignatura }
            }
        }
        .toMap()
        .takeIf { it.isNotEmpty() }

    if (nombre == null && descripcion == null && fechaInicio == null && fechaFin == null) return null

    return Anio(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        numero_asignaturas = numeroAsignaturas,
        lista_asignaturas = asignaturasMap,
        id_user = idUser
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimationSearch(
    initialAnios: List<Anio>,
    onAniosFiltered: (List<Anio>) -> Unit,
    screenHeight: Dp,
    screenWidth: Dp,
    onCrearAnio: () -> Unit,
    onPerfil: () -> Unit
) {

    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf("Fecha Reciente") }

    val horizontalPadding = screenWidth * 0.04f

    val sortOptions = listOf("Fecha Reciente", "Último a primero", "Por nombre", "Por cantidad de asignaturas")
    val actions = listOf(
        "Perfil" to Icons.Default.Person,
        "Buscar" to Icons.Default.Search,
        "Añadir" to Icons.Default.Add,
        "Opciones" to Icons.Default.MoreVert
    )

    LaunchedEffect(searchQuery, selectedSortOption, initialAnios) {
        val filteredList =
            if (searchQuery.isNotBlank()) {
                initialAnios.filter { anio -> anio.nombre?.contains(searchQuery, ignoreCase = true) == true }
            } else {
                initialAnios
            }

        val sortedList = when (selectedSortOption) {
            "Fecha Reciente" -> filteredList.sortedByDescending {
                try {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(it.fechaInicio ?: "")
                } catch (e: Exception) {
                    null
                }
            }
            "Último a primero" -> filteredList.reversed()
            "Por nombre" -> filteredList.sortedBy { it.nombre }
            "Por cantidad de asignaturas" -> filteredList.sortedByDescending { it.lista_asignaturas?.size ?: 0 }
            else -> filteredList
        }

        onAniosFiltered(sortedList)
    }

    AnimatedContent(
        targetState = isSearchExpanded,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
        },
        modifier = Modifier.padding(horizontal = horizontalPadding, vertical = screenHeight * 0.02f)
    ) { isExpanded ->
        if (!isExpanded) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f),
                contentPadding = PaddingValues(horizontal = screenWidth * 0.02f, vertical = screenHeight * 0.02f)
            ) {
                items(actions) { (label, icon) ->
                    CircularActionButton(
                        icon = icon,
                        label = label,
                        onClick = {
                            when (label) {
                                "Buscar" -> isSearchExpanded = true
                                "Añadir" -> onCrearAnio()
                                "Perfil" -> onPerfil()
                                "Opciones" -> {
                                    //
                                }
                                else -> { }
                            }
                        },
                        screenHeight = screenHeight
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = screenHeight * 0.01f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.02f)
            ) {
                IconButton(onClick = { isSearchExpanded = false; searchQuery = "" }) {
                    Icon(Icons.Default.SearchOff, contentDescription = "Cerrar Búsqueda")
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar Año...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar Años")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSortOption = option
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
