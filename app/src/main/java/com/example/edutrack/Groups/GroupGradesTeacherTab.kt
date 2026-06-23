package com.example.edutrack.Groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.example.edutrack.R
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.edutrack.addGradeToStudent
import com.example.edutrack.dataclass.GroupGrade
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.removeGradeFromStudent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupGradesTeacherTab(
    groupId: String,
    userId: String?,
    members: List<GroupMember>,
    allGrades: Map<String, List<GroupGrade>>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var selectedStudent by remember { mutableStateOf<GroupMember?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var gradeToDelete by remember { mutableStateOf<GroupGrade?>(null) }

    val students = members.filter {
        val role = it.role?.let { r -> runCatching { GroupRole.valueOf(r) }.getOrNull() } ?: GroupRole.MEMBER
        role == GroupRole.MEMBER
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedStudent != null) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir nota")
                }
            }
        }
    ) { innerPadding ->
        if (selectedStudent == null) {
            // Listado de alumnos
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.group_no_students),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Selecciona un alumno para ver o añadir notas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(students) { student ->
                        val grades = allGrades[student.uid] ?: emptyList()
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedStudent = student },
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!student.photoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = student.photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        student.displayName ?: "Alumno",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${grades.size} nota${if (grades.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Vista de notas de un alumno concreto
            val student = selectedStudent!!
            val grades = allGrades[student.uid] ?: emptyList()

            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                // Back header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedStudent = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Column {
                        Text(
                            student.displayName ?: "Alumno",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${grades.size} nota${if (grades.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider()

                if (grades.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Sin notas aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pulsa + para añadir la primera",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(grades) { grade ->
                            GradeRow(
                                grade = grade,
                                onDelete = { gradeToDelete = grade }
                            )
                        }
                    }
                }
            }
        }
    }

    // Sheet para añadir nota
    if (showAddSheet && selectedStudent != null) {
        AddGradeSheet(
            sheetState = sheetState,
            onDismiss = { showAddSheet = false },
            onConfirm = { grade ->
                addGradeToStudent(groupId, selectedStudent!!.uid!!, grade) { ok ->
                    scope.launch {
                        showAddSheet = false
                        if (!ok) snackbarHostState.showSnackbar(context.getString(R.string.state_error))
                    }
                }
            }
        )
    }

    // Confirmar borrado
    gradeToDelete?.let { grade ->
        AlertDialog(
            onDismissRequest = { gradeToDelete = null },
            title = { Text("Eliminar nota") },
            text = { Text("¿Eliminar «${grade.nombreNota}»? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = selectedStudent?.uid ?: return@Button
                        removeGradeFromStudent(groupId, uid, grade.id!!) { ok ->
                            scope.launch {
                                gradeToDelete = null
                                if (!ok) snackbarHostState.showSnackbar(context.getString(R.string.state_error))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { gradeToDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun GradeRow(grade: GroupGrade, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        ListItem(
            headlineContent = {
                Text(grade.nombreNota ?: "Nota", fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                val sub = buildString {
                    grade.subjectName?.let { append(it).append(" · ") }
                    grade.fecha?.let { append(it).append(" · ") }
                    append("Peso ${grade.peso?.toInt() ?: 0}%")
                }
                Text(sub, style = MaterialTheme.typography.bodySmall)
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%.1f".format(grade.valor ?: 0.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGradeSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: (GroupGrade) -> Unit
) {
    var nombreNota by remember { mutableStateOf("") }
    var valorStr by remember { mutableStateOf("") }
    var pesoStr by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val errNombreReq = stringResource(R.string.error_name_required)
    val errGrade = stringResource(R.string.grade_value_error)
    val errWeight = stringResource(R.string.grade_weight_error)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.grade_add), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = nombreNota,
                onValueChange = { nombreNota = it },
                label = { Text(stringResource(R.string.grade_name_hint) + " *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            OutlinedTextField(
                value = subjectName,
                onValueChange = { subjectName = it },
                label = { Text("${stringResource(R.string.field_subject)} (${stringResource(R.string.state_optional)})") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = valorStr,
                    onValueChange = { valorStr = it },
                    label = { Text("${stringResource(R.string.field_grade)} (0-10) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = pesoStr,
                    onValueChange = { pesoStr = it },
                    label = { Text("${stringResource(R.string.field_weight)} % *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val valor = valorStr.replace(",", ".").toDoubleOrNull()
                    val peso = pesoStr.replace(",", ".").toDoubleOrNull()
                    when {
                        nombreNota.isBlank() -> errorMsg = errNombreReq
                        valor == null || valor < 0 || valor > 10 -> errorMsg = errGrade
                        peso == null || peso < 0 || peso > 100 -> errorMsg = errWeight
                        else -> {
                            errorMsg = null
                            onConfirm(
                                GroupGrade(
                                    nombreNota = nombreNota.trim(),
                                    valor = valor,
                                    peso = peso,
                                    subjectName = subjectName.trim().ifBlank { null },
                                    fecha = fecha.trim().ifBlank { null }
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}
