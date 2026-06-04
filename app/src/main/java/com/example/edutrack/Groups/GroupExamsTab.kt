package com.example.edutrack.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.dataclass.GroupExam
import com.example.edutrack.dataclass.GroupFeedEventType
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.groupExamsRef
import com.example.edutrack.groupFeedRef
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupExamsTab(
    groupId: String,
    userId: String?,
    isAdmin: Boolean,
    myMember: GroupMember?,
    modifier: Modifier = Modifier
) {
    val exams by rememberGroupExamsState(groupId)
    var showAddSheet by remember { mutableStateOf(false) }

    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val upcoming = exams.filter { (it.fecha ?: "") >= today }
    val past = exams.filter { (it.fecha ?: "") < today }

    Box(modifier = modifier.fillMaxSize()) {
        if (exams.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Sin exámenes anunciados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Avisa al grupo de los próximos exámenes para que nadie se quede sin estudiar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showAddSheet = true },
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anunciar examen", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (upcoming.isNotEmpty()) {
                    item {
                        Text(
                            text = "Próximos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    itemsIndexed(upcoming, key = { _, e -> e.id ?: e.hashCode().toString() }) { index, exam ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(exam.id) { delay(index * 50L + 20L); visible = true }
                        AnimatedVisibility(visible = visible, enter = fadeIn(tween(250)) + slideInVertically { it / 4 }) {
                            ExamCard(
                                exam = exam,
                                isPast = false,
                                canDelete = isAdmin || exam.authorId == userId,
                                onDelete = { groupExamsRef(groupId).child(exam.id ?: return@ExamCard).removeValue() }
                            )
                        }
                    }
                }

                if (past.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pasados",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    itemsIndexed(past, key = { _, e -> (e.id ?: e.hashCode().toString()) + "_past" }) { index, exam ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(exam.id) { delay(index * 40L + 10L); visible = true }
                        AnimatedVisibility(visible = visible, enter = fadeIn(tween(200))) {
                            ExamCard(
                                exam = exam,
                                isPast = true,
                                canDelete = isAdmin || exam.authorId == userId,
                                onDelete = { groupExamsRef(groupId).child(exam.id ?: return@ExamCard).removeValue() }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Anunciar examen", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if (showAddSheet) {
        AddGroupExamSheet(
            groupId = groupId,
            userId = userId,
            myMember = myMember,
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun ExamCard(
    exam: GroupExam,
    isPast: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val daysLabel = exam.fecha?.let { daysUntil(it) } ?: ""
    val alpha = if (isPast) 0.55f else 1f
    var showConfirmDelete by remember { mutableStateOf(false) }

    val chipColor = when {
        isPast -> MaterialTheme.colorScheme.surfaceVariant
        daysLabel == "Hoy" -> MaterialTheme.colorScheme.errorContainer
        daysLabel.startsWith("Mañana") -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val chipTextColor = when {
        isPast -> MaterialTheme.colorScheme.onSurfaceVariant
        daysLabel == "Hoy" -> MaterialTheme.colorScheme.onErrorContainer
        daysLabel.startsWith("Mañana") -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPast) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono de fecha coloreado según urgencia
            Surface(
                modifier = Modifier.size(52.dp),
                shape = MaterialTheme.shapes.large,
                color = chipColor
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = chipTextColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (!exam.asignatura.isNullOrBlank()) {
                    Text(
                        text = exam.asignatura!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = exam.nombre ?: "Examen",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fechaHora = buildString {
                        exam.fecha?.let { append(it) }
                        exam.hora?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                    }
                    if (fechaHora.isNotBlank()) {
                        Text(
                            text = fechaHora,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                        )
                    }
                }
                if (!exam.authorName.isNullOrBlank()) {
                    Text(
                        text = "por ${exam.authorName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (daysLabel.isNotBlank()) {
                    Surface(color = chipColor, shape = MaterialTheme.shapes.small) {
                        Text(
                            text = daysLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = chipTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (canDelete) {
                    if (showConfirmDelete) {
                        TextButton(
                            onClick = { onDelete(); showConfirmDelete = false },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Borrar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGroupExamSheet(
    groupId: String,
    userId: String?,
    myMember: GroupMember?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var asignatura by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var fechaError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    fun guardar() {
        var ok = true
        if (nombre.isBlank()) { nombreError = true; ok = false }
        if (fecha.isBlank()) { fechaError = true; ok = false }
        if (!ok) return
        val uid = userId ?: return
        isSaving = true
        val ref = groupExamsRef(groupId).push()
        val eid = ref.key ?: return
        val exam = GroupExam(
            id = eid,
            nombre = nombre.trim(),
            asignatura = asignatura.trim().ifBlank { null },
            fecha = fecha,
            hora = hora.trim().ifBlank { null },
            authorId = uid,
            authorName = myMember?.displayName,
            authorPhotoUrl = myMember?.photoUrl,
            createdAt = System.currentTimeMillis()
        )
        ref.setValue(exam).addOnCompleteListener {
            val label = buildString {
                append(nombre.trim())
                if (asignatura.isNotBlank()) append(" · ${asignatura.trim()}")
                append(" ($fecha)")
            }
            val feedRef = groupFeedRef(groupId).push()
            val event = mapOf(
                "id" to feedRef.key,
                "type" to GroupFeedEventType.EXAM.name,
                "actorUid" to uid,
                "actorName" to myMember?.displayName,
                "actorPhotoUrl" to myMember?.photoUrl,
                "targetLabel" to label,
                "createdAt" to ServerValue.TIMESTAMP
            )
            feedRef.setValue(event).addOnCompleteListener {
                isSaving = false
                scope.launch { sheetState.hide(); onDismiss() }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Anunciar examen", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { sheetState.hide(); onDismiss() } }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    },
                    actions = {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(20.dp), strokeWidth = 2.dp)
                        } else {
                            TextButton(
                                onClick = ::guardar,
                                enabled = !isSaving && nombre.isNotBlank() && fecha.isNotBlank()
                            ) {
                                Text("Anunciar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Datos del examen",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { if (it.length <= 200) { nombre = it; nombreError = false } },
                            label = { Text("Nombre del examen *") },
                            placeholder = { Text("Ej: Parcial 1, Examen final…") },
                            isError = nombreError,
                            supportingText = if (nombreError) ({ Text("El nombre es obligatorio") }) else null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = asignatura,
                            onValueChange = { if (it.length <= 200) asignatura = it },
                            label = { Text("Asignatura (opcional)") },
                            placeholder = { Text("Ej: Álgebra, Historia del Arte…") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                    }
                }

                Text(
                    text = "Fecha y hora",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            label = { Text("Fecha *") },
                            placeholder = { Text("Selecciona la fecha") },
                            isError = fechaError,
                            supportingText = if (fechaError) ({ Text("La fecha es obligatoria") }) else null,
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Seleccionar fecha",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        )

                        OutlinedTextField(
                            value = hora,
                            onValueChange = { if (it.length <= 5) hora = it },
                            label = { Text("Hora (opcional)") },
                            placeholder = { Text("HH:mm") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            singleLine = true
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "El anuncio será visible para todos los miembros del grupo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Button(
                    onClick = ::guardar,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    enabled = !isSaving && nombre.isNotBlank() && fecha.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Anunciar examen", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        fecha = "%02d/%02d/%04d".format(
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)
                        )
                        fechaError = false
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun daysUntil(fechaStr: String): String {
    return runCatching {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val examDate = sdf.parse(fechaStr) ?: return@runCatching ""
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val diff = TimeUnit.MILLISECONDS.toDays(examDate.time - now)
        when {
            diff < 0 -> ""
            diff == 0L -> "Hoy"
            diff == 1L -> "Mañana"
            diff <= 7 -> "En $diff días"
            else -> { val w = diff / 7; if (w == 1L) "En 1 semana" else "En $w semanas" }
        }
    }.getOrElse { "" }
}
