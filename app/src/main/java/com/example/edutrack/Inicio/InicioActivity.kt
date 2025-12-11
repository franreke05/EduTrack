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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
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

    LaunchedEffect(aniosFromFirebase) {
        displayedAnios = aniosFromFirebase
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var anioToDelete by remember { mutableStateOf<Anio?>(null) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight * 0.055f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))
                )
            ),
    ) {}
    Column(modifier = modifier.fillMaxWidth()) {

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

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(bottom = screenHeight * 0.02f)
        ) {
            itemsIndexed(displayedAnios, key = { _, anio -> anio.id ?: anio.hashCode().toString() }) { index, anio ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            anioToDelete = anio
                            showDeleteDialog = true
                            false
                        } else true
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.fillMaxWidth(),
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(screenHeight * 0.08f)
                                .background(color)
                                .padding(horizontal = screenWidth * 0.05f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(screenHeight * 0.04f)
                            )
                        }
                    }
                ) {
                    AnioCard(anio, index + 1, screenHeight, screenWidth) {
                        onAnioSelected(anio.id)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar EliminaciÇün") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AnioCard(anio: Anio, index: Int, screenHeight: Dp, screenWidth: Dp, function : () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = screenWidth * 0.04f, vertical = screenHeight * 0.01f),
        elevation = CardDefaults.cardElevation(defaultElevation = screenHeight * 0.01f),
        onClick = function,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,) // Added shadow
    ) {
        Row(
            modifier = Modifier.padding(screenHeight * 0.02f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(screenHeight * 0.06f)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = toRoman(index),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(screenWidth * 0.04f))
            Column(modifier = Modifier.weight(0.55f).padding(start = screenWidth * 0.02f)) {
                anio.nombre?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) }
                anio.descripcion?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                anio.numero_asignaturas?.let { Text(text = "Número de asignaturas: $it", style = MaterialTheme.typography.bodyMedium) }
            }
            Column(
                modifier = Modifier.width(screenWidth * 0.04f).weight(0.35f).padding(end = screenWidth * 0.02f),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "Fecha Inicio", style = MaterialTheme.typography.titleMedium)
                anio.fechaInicio?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                Text(text = "Fecha Fin", style = MaterialTheme.typography.titleMedium)
                anio.fechaFin?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduTrackTheme {
        AnioCard(anio = Anio("Año 1", "Descripción del año 1","descripcion","14/10/25","14/10/25"), index = 1, screenHeight = 100.dp, screenWidth = 100.dp) { }
    }
}

@Composable
fun CircularActionButton(icon: ImageVector, label: String, onClick: () -> Unit, screenHeight: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(screenHeight * 0.07f)
                .shadow(elevation = screenHeight * 0.01f, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)

        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(screenHeight * 0.045f)
            )
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
        modifier = Modifier.padding(vertical = screenHeight * 0.02f)
    ) { isExpanded ->
        if (!isExpanded) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.04f),
                contentPadding = PaddingValues(horizontal = screenWidth * 0.04f)
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
                                "Opciones" -> { }
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
                    .padding(horizontal = screenWidth * 0.04f),
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
