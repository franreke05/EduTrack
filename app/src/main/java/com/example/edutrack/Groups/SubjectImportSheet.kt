package com.example.edutrack.Groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.aniosRef
import com.example.edutrack.currentMonthKey
import com.example.edutrack.dataclass.Anio
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.groupImportsMetaRef
import com.example.edutrack.importarAsignaturaDesdeGrupo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

private const val FREE_MONTHLY_IMPORTS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectImportSheet(
    uid: String,
    sourceGroupId: String,
    shared: GroupSharedSubject,
    plan: UserPlan,
    actorName: String?,
    actorPhotoUrl: String?,
    onPaywall: () -> Unit,
    onDismiss: () -> Unit,
    onImported: (message: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val anios by rememberUserAniosState(uid)
    val monthlyImports by rememberMonthlyImportCount(uid)
    var selectedAnioId by remember { mutableStateOf<String?>(null) }
    var creditosText by remember { mutableStateOf("6") }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val isPremium = plan == UserPlan.PREMIUM
    val quotaReached = !isPremium && monthlyImports >= FREE_MONTHLY_IMPORTS

    LaunchedEffect(anios) {
        if (selectedAnioId == null) selectedAnioId = anios.firstOrNull()?.id
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Copiar a mis asignaturas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Crearemos esta asignatura en tu curso con los mismos periodos. Tus notas no se copian.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = shared.name ?: "Asignatura",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${shared.tipoPeriodo ?: "Trimestre"} · ${shared.numeroPeriodos ?: 3} periodos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (quotaReached) {
                QuotaReachedCard(
                    used = monthlyImports,
                    limit = FREE_MONTHLY_IMPORTS,
                    onPaywall = {
                        onDismiss()
                        onPaywall()
                    }
                )
            } else {
                if (!isPremium) {
                    Text(
                        text = "Has importado $monthlyImports de $FREE_MONTHLY_IMPORTS este mes (plan gratis).",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Año destino",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (anios.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Crea primero un curso desde la pantalla principal para poder importar aquí.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(anios, key = { it.id ?: it.hashCode().toString() }) { anio ->
                            AnioOptionRow(
                                anio = anio,
                                selected = anio.id == selectedAnioId,
                                onClick = { selectedAnioId = anio.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = creditosText,
                    onValueChange = { v -> creditosText = v.filter { it.isDigit() }.take(3) },
                    label = { Text("Créditos") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Por defecto 6. Puedes ajustarlo después.") }
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val anioId = selectedAnioId
                        val creditos = creditosText.toIntOrNull() ?: 0
                        if (anioId.isNullOrBlank()) {
                            errorMsg = "Elige un año destino"
                            return@Button
                        }
                        if (creditos < 1) {
                            errorMsg = "Los créditos deben ser al menos 1"
                            return@Button
                        }
                        errorMsg = null
                        loading = true
                        importarAsignaturaDesdeGrupo(
                            uid = uid,
                            targetAnioId = anioId,
                            sourceGroupId = sourceGroupId,
                            shared = shared,
                            creditos = creditos,
                            actorName = actorName,
                            actorPhotoUrl = actorPhotoUrl
                        ) { success, err ->
                            loading = false
                            if (success) onImported("Lista. Ahora añade tus notas")
                            else errorMsg = err ?: "Error al importar"
                        }
                    },
                    enabled = !loading && anios.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Copiando...")
                    } else {
                        Text("Copiar a mi curso")
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AnioOptionRow(anio: Anio, selected: Boolean, onClick: () -> Unit) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = anio.nombre ?: "Curso",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
private fun QuotaReachedCard(used: Int, limit: Int, onPaywall: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Has alcanzado el límite mensual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Has usado $used de $limit importaciones gratis este mes. Premium te da importaciones ilimitadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onPaywall,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Hacerme Premium")
            }
        }
    }
}

@Composable
private fun rememberUserAniosState(uid: String): androidx.compose.runtime.State<List<Anio>> {
    val state = remember { mutableStateOf<List<Anio>>(emptyList()) }
    DisposableEffect(uid) {
        if (uid.isBlank()) return@DisposableEffect onDispose {}
        val ref = aniosRef(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.children.mapNotNull { it.getValue(Anio::class.java) }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}

@Composable
private fun rememberMonthlyImportCount(uid: String): androidx.compose.runtime.State<Int> {
    val state = remember { mutableStateOf(0) }
    DisposableEffect(uid) {
        if (uid.isBlank()) return@DisposableEffect onDispose {}
        val ref = groupImportsMetaRef(uid, currentMonthKey()).child("count")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                state.value = snapshot.getValue(Long::class.java)?.toInt() ?: 0
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }
    return state
}
