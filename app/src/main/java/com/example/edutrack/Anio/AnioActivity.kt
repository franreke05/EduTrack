package com.example.edutrack.Anio

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.edutrack.Groups.rememberUserGroupsState
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.Inicio.toRoman
import com.example.edutrack.CrearAsignatura
import com.example.edutrack.Perfil.rememberUsuarioState
import com.example.edutrack.compartirAsignaturaConGrupo
import com.example.edutrack.dataclass.Asignatura
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.ui.theme.EduTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// Ruta de entrada: carga el anio seleccionado y muestra un indicador mientras falta data.
@Composable
fun AnioRoute(
    userId: String?,
    anioId: String?,
    onBack: () -> Unit = {},
    onOpenNotas: (Asignatura, String?) -> Unit = { _, _ -> }
) {
    val anios by rememberAniosState(userId)
    val anio = anios.firstOrNull { it.id == anioId }
    if (anio == null) {
        CircularProgressIndicator()
    } else {
        AnioScreen(anio = anio, userId = userId, pageIndex = anios.indexOf(anio), onBack = onBack, onOpenNotas = onOpenNotas)
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnioScreen(
    modifier: Modifier = Modifier,
    anio: com.example.edutrack.dataclass.Anio,
    userId: String? = null,
    pageIndex: Int,
    onBack: () -> Unit = {},
    onOpenNotas: (Asignatura, String?) -> Unit = { _, _ -> }
) {
    val userGroups by rememberUserGroupsState(userId)
    var asignaturaToShare by remember { mutableStateOf<Asignatura?>(null) }
    var showDescriptionDialog by remember { mutableStateOf(false) }
    var showAsignaturaDialog by remember { mutableStateOf(false) }
    val maxAsignaturas = anio.numero_asignaturas ?: 0
    val actuales = anio.lista_asignaturas?.size ?: 0
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val anioLleno = actuales >= maxAsignaturas
    val context = LocalContext.current
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val asignaturasBase = remember(anio.lista_asignaturas) {
        anio.lista_asignaturas?.values?.toList().orEmpty()
    }
    val asignaturasFiltradas by remember(showSearch, searchQuery, asignaturasBase) {
        derivedStateOf {
            if (showSearch && searchQuery.isNotBlank()) {
                asignaturasBase.filter { it.nombre?.contains(searchQuery, ignoreCase = true) == true }
            } else {
                asignaturasBase
            }
        }
    }
    val spacing = 16.dp

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(anio.nombre ?: "Año escolar") },
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
                            Toast.makeText(context, "Límite de asignaturas alcanzado", Toast.LENGTH_SHORT).show()
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = toRoman(pageIndex + 1),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = anio.nombre ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$actuales / $maxAsignaturas asignaturas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        if (anio.descripcion?.isNotBlank() == true) {
                            IconButton(onClick = { showDescriptionDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Ver descripción",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                if (asignaturasFiltradas.isEmpty() && !showSearch) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📚", style = MaterialTheme.typography.displaySmall)
                            Text(
                                text = "No hay asignaturas aún",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Pulsa + para añadir tu primera asignatura",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(asignaturasFiltradas) { index, asignatura ->
                            AsignaturaCard(
                                index = index + 1,
                                asignatura = asignatura,
                                onClick = { onOpenNotas(asignatura, anio.id) },
                                onLongClick = if (userGroups.isNotEmpty()) {
                                    { asignaturaToShare = asignatura }
                                } else null
                            )
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

    asignaturaToShare?.let { asig ->
        PublicarAsignaturaDialog(
            asignatura = asig,
            grupos = userGroups,
            userId = userId,
            onDismiss = { asignaturaToShare = null }
        )
    }

    if (showAsignaturaDialog) {
        CrearAsignaturaDialog(
            anioId = anio.id,
            idUsuario = null,
            maxAsignaturas = maxAsignaturas,
            actuales = actuales,
            onDismiss = { showAsignaturaDialog = false }
        )
    }
}

// Tarjeta para una asignatura con media visible.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AsignaturaCard(
    index: Int,
    asignatura: Asignatura,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val media = asignatura.media
    val colorScheme = MaterialTheme.colorScheme
    val mediaColor = when {
        media == null -> colorScheme.onSurfaceVariant
        media >= 7.0 -> colorScheme.tertiary
        media >= 5.0 -> colorScheme.primary
        else -> colorScheme.error
    }
    val abbreviation = abbreviateName(asignatura.nombre ?: "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar + score row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = abbreviation,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onPrimaryContainer
                    )
                }
                if (media != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format("%.1f", media),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = mediaColor
                        )
                        Text(
                            text = "media",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Name
            Text(
                text = asignatura.nombre ?: "Asignatura $index",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val creditos = asignatura.creditos ?: 0
                if (creditos > 0) {
                    Surface(color = colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text(
                            text = "$creditos cr",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                asignatura.tipo_periodo?.let { tipo ->
                    Surface(color = colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                        Text(
                            text = tipo.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// Recoge datos del usuario y crea una nueva asignatura.
@Composable
fun CrearAsignaturaDialog(anioId: String?, idUsuario: String?, maxAsignaturas: Int, actuales: Int, onDismiss: () -> Unit) {
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
                if (actuales >= maxAsignaturas) {
                    Toast.makeText(context, "Limite de asignaturas alcanzado", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                val creditosInt = creditos.value.toIntOrNull() ?: 0
                if (creditosInt <= 0) {
                    Toast.makeText(context, "Ingresa creditos validos.", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                val numeroPeriodos = if (tipo.value == "Cuatrimestre") 2 else 3
                val uid = idUsuario
                    ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    ?: return@TextButton
                val asignatura = Asignatura(
                    nombre = nombre.value,
                    descripcion = descripcion.value,
                    creditos = creditosInt,
                    tipo_periodo = tipo.value,
                    numero_periodos = numeroPeriodos
                )
                CrearAsignatura(uid, anioId, asignatura)
                onDismiss()
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PublicarAsignaturaDialog(
    asignatura: Asignatura,
    grupos: List<com.example.edutrack.dataclass.UserGroup>,
    userId: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val usuario by rememberUsuarioState(userId)
    var selectedGroupId by remember { mutableStateOf(grupos.firstOrNull()?.groupId) }
    var isSharing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publicar en grupo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "\"${asignatura.nombre}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (asignatura.media != null && asignatura.media > 0.0) {
                    Text(
                        text = "Media: ${String.format("%.1f", asignatura.media)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Elige el grupo donde publicar:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                grupos.forEach { grupo ->
                    val isSelected = selectedGroupId == grupo.groupId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroupId = grupo.groupId },
                        label = { Text(grupo.name ?: "Grupo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val gid = selectedGroupId ?: return@Button
                    val uid = userId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                    val userName = usuario?.nombre?.takeIf { it.isNotBlank() }
                        ?: FirebaseAuth.getInstance().currentUser?.displayName
                        ?: "Usuario"
                    isSharing = true
                    val subject = GroupSharedSubject(
                        name = asignatura.nombre,
                        tipoPeriodo = asignatura.tipo_periodo,
                        numeroPeriodos = asignatura.numero_periodos,
                        media = asignatura.media,
                        sharedBy = uid,
                        sharedByName = userName,
                        sharedByPhotoUrl = usuario?.photoUrl,
                        sharedAt = System.currentTimeMillis()
                    )
                    compartirAsignaturaConGrupo(gid, subject) { success ->
                        isSharing = false
                        Toast.makeText(
                            context,
                            if (success) "Publicado en el grupo" else "Error al publicar",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    }
                },
                enabled = selectedGroupId != null && !isSharing
            ) {
                if (isSharing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Publicar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
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
