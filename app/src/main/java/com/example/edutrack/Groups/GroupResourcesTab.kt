package com.example.edutrack.Groups

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.edutrack.dataclass.GroupFeedEventType
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupResource
import com.example.edutrack.groupFeedRef
import com.example.edutrack.groupResourcesRef
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupResourcesTab(
    groupId: String,
    userId: String?,
    isAdmin: Boolean,
    myMember: GroupMember?,
    modifier: Modifier = Modifier
) {
    val resources by rememberGroupResourcesState(groupId)
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (resources.isEmpty()) {
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
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Sin recursos aún",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Comparte apuntes, enlaces a temario, vídeos o cualquier material útil para el grupo.",
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
                    Text("Añadir recurso", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(resources, key = { _, r -> r.id ?: r.hashCode().toString() }) { index, resource ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(resource.id) {
                        delay(index * 50L + 20L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(250)) + slideInVertically { it / 4 }
                    ) {
                        ResourceCard(
                            resource = resource,
                            canDelete = isAdmin || resource.authorId == userId,
                            onDelete = {
                                val rid = resource.id ?: return@ResourceCard
                                groupResourcesRef(groupId).child(rid).removeValue()
                            }
                        )
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
                Icon(Icons.Default.Add, contentDescription = "Añadir recurso", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if (showAddSheet) {
        AddResourceSheet(
            groupId = groupId,
            userId = userId,
            myMember = myMember,
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun ResourceCard(
    resource: GroupResource,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val timeStr = resource.createdAt?.let {
        SimpleDateFormat("dd MMM · HH:mm", Locale("es", "ES")).format(Date(it))
    } ?: ""
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: avatar + author + time + tag + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!resource.authorPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = resource.authorPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = (resource.authorName ?: "?").take(1).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.authorName ?: "Usuario",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!resource.tag.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = resource.tag!!,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                if (canDelete) {
                    if (showConfirmDelete) {
                        TextButton(
                            onClick = { onDelete(); showConfirmDelete = false },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Borrar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        IconButton(onClick = { showConfirmDelete = true }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Título
            Text(
                text = resource.title ?: "Recurso",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Descripción
            if (!resource.description.isNullOrBlank()) {
                Text(
                    text = resource.description!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // URL clickable con card visual
            if (!resource.url.isNullOrBlank()) {
                Surface(
                    onClick = {
                        runCatching {
                            val uri = Uri.parse(resource.url)
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = resource.url!!.removePrefix("https://").removePrefix("http://").take(60).let {
                                if (resource.url!!.length > 70) "$it…" else it
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddResourceSheet(
    groupId: String,
    userId: String?,
    myMember: GroupMember?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Añadir recurso", fontWeight = FontWeight.Bold) },
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
                                onClick = {
                                    if (title.isBlank()) { titleError = true; return@TextButton }
                                    val uid = userId ?: return@TextButton
                                    isSaving = true
                                    val ref = groupResourcesRef(groupId).push()
                                    val rid = ref.key ?: return@TextButton
                                    val urlClean = url.trim().let {
                                        if (it.isNotBlank() && !it.startsWith("http")) "https://$it" else it
                                    }
                                    val resource = GroupResource(
                                        id = rid,
                                        title = title.trim(),
                                        url = urlClean.ifBlank { null },
                                        description = description.trim().ifBlank { null },
                                        tag = tag.trim().ifBlank { null },
                                        authorId = uid,
                                        authorName = myMember?.displayName,
                                        authorPhotoUrl = myMember?.photoUrl,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    ref.setValue(resource).addOnCompleteListener {
                                        val feedRef = groupFeedRef(groupId).push()
                                        val event = mapOf(
                                            "id" to feedRef.key,
                                            "type" to GroupFeedEventType.RESOURCE.name,
                                            "actorUid" to uid,
                                            "actorName" to myMember?.displayName,
                                            "actorPhotoUrl" to myMember?.photoUrl,
                                            "targetLabel" to title.trim(),
                                            "createdAt" to ServerValue.TIMESTAMP
                                        )
                                        feedRef.setValue(event).addOnCompleteListener {
                                            isSaving = false
                                            scope.launch { sheetState.hide(); onDismiss() }
                                        }
                                    }
                                },
                                enabled = !isSaving && title.isNotBlank()
                            ) {
                                Text("Publicar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                    text = "Información del recurso",
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
                            value = title,
                            onValueChange = { if (it.length <= 200) { title = it; titleError = false } },
                            label = { Text("Título *") },
                            placeholder = { Text("Ej: Apuntes tema 3, Vídeo explicativo…") },
                            isError = titleError,
                            supportingText = if (titleError) ({ Text("El título es obligatorio") }) else ({
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Text("${title.length}/200", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 1000) description = it },
                            label = { Text("Descripción (opcional)") },
                            placeholder = { Text("Breve descripción del recurso…") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            maxLines = 3
                        )
                    }
                }

                Text(
                    text = "Enlace y etiqueta",
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
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("Enlace (opcional)") },
                            placeholder = { Text("https://…") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = tag,
                            onValueChange = { if (it.length <= 100) tag = it },
                            label = { Text("Asignatura / etiqueta (opcional)") },
                            placeholder = { Text("Ej: Matemáticas, Historia…") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "El recurso será visible para todos los miembros del grupo. Puedes añadir un enlace a Drive, Notion, YouTube o cualquier sitio web.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Button(
                    onClick = {
                        if (title.isBlank()) { titleError = true; return@Button }
                        val uid = userId ?: return@Button
                        isSaving = true
                        val ref = groupResourcesRef(groupId).push()
                        val rid = ref.key ?: return@Button
                        val urlClean = url.trim().let {
                            if (it.isNotBlank() && !it.startsWith("http")) "https://$it" else it
                        }
                        val resource = GroupResource(
                            id = rid,
                            title = title.trim(),
                            url = urlClean.ifBlank { null },
                            description = description.trim().ifBlank { null },
                            tag = tag.trim().ifBlank { null },
                            authorId = uid,
                            authorName = myMember?.displayName,
                            authorPhotoUrl = myMember?.photoUrl,
                            createdAt = System.currentTimeMillis()
                        )
                        ref.setValue(resource).addOnCompleteListener {
                            val feedRef = groupFeedRef(groupId).push()
                            val event = mapOf(
                                "id" to feedRef.key,
                                "type" to GroupFeedEventType.RESOURCE.name,
                                "actorUid" to uid,
                                "actorName" to myMember?.displayName,
                                "actorPhotoUrl" to myMember?.photoUrl,
                                "targetLabel" to title.trim(),
                                "createdAt" to ServerValue.TIMESTAMP
                            )
                            feedRef.setValue(event).addOnCompleteListener {
                                isSaving = false
                                scope.launch { sheetState.hide(); onDismiss() }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    enabled = !isSaving && title.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar recurso", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
