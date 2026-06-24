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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edutrack.R
import coil.compose.AsyncImage
import com.example.edutrack.dataclass.GroupMember
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.dataclass.GroupSharedSubject
import com.example.edutrack.ui.LocalUserPlan
import com.example.edutrack.eliminarAsignaturaCompartida
import com.example.edutrack.salirDeGrupo
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrupoDetalleScreen(
    userId: String?,
    groupId: String,
    onBack: () -> Unit = {},
    onPaywall: () -> Unit = {},
    onNavigateToAnio: ((String) -> Unit)? = null
) {
    val group by rememberGroupState(groupId)
    val members by rememberGroupMembersState(groupId)
    val sharedSubjects by rememberGroupSharedSubjectsState(groupId)
    val userPlan = LocalUserPlan.current

    var selectedTab by remember { mutableStateOf(0) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var isLeaving by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<GroupSharedSubject?>(null) }
    var subjectToDelete by remember { mutableStateOf<GroupSharedSubject?>(null) }
    var isDeletingSubject by remember { mutableStateOf(false) }
    var showQrSheet by remember { mutableStateOf(false) }
    var showCourseSheet by remember { mutableStateOf(false) }
    var importTarget by remember { mutableStateOf<GroupSharedSubject?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    val myMember = members.find { it.uid == userId }
    val myRole = myMember?.role?.let { runCatching { GroupRole.valueOf(it) }.getOrNull() }
        ?: GroupRole.MEMBER
    val isOwner = myRole == GroupRole.OWNER
    val isAdmin = myRole == GroupRole.ADMIN || isOwner

    val isClassroom = group?.type == "CLASSROOM"
    val allGrades by rememberAllGroupGradesState(if (isClassroom && isAdmin) groupId else null)
    val myGrades by rememberStudentGradesState(if (isClassroom && !isAdmin) groupId else null, userId)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(group?.name ?: "Grupo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (!isOwner) {
                        IconButton(onClick = { showLeaveDialog = true }) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Salir del grupo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (group == null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            }
            return@Scaffold
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth > 600.dp
            val hPad = if (isTablet) (maxWidth - 600.dp) / 2 else 16.dp
            val tabletSidePad = if (isTablet) (maxWidth - 600.dp) / 2 else 0.dp
            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.group_tab_summary), fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.group_tab_feed), fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(stringResource(R.string.group_tab_resources), fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text(stringResource(R.string.group_tab_exams), fontWeight = FontWeight.SemiBold) }
                    )
                    if (isClassroom) {
                        Tab(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            text = { Text(stringResource(R.string.group_tab_grades), fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
                when (selectedTab) {
                    1 -> GroupFeedTab(
                        groupId = groupId,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = tabletSidePad)
                    )
                    2 -> GroupResourcesTab(
                        groupId = groupId,
                        userId = userId,
                        isAdmin = isAdmin,
                        myMember = myMember,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = tabletSidePad)
                    )
                    3 -> GroupExamsTab(
                        groupId = groupId,
                        userId = userId,
                        isAdmin = isAdmin,
                        myMember = myMember,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = tabletSidePad)
                    )
                    4 -> if (isClassroom) {
                        if (isAdmin) {
                            GroupGradesTeacherTab(
                                groupId = groupId,
                                userId = userId,
                                members = members,
                                allGrades = allGrades,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = tabletSidePad)
                            )
                        } else {
                            GroupGradesStudentTab(
                                grades = myGrades,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = tabletSidePad)
                            )
                        }
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = hPad, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
            // Header card
            item {
                var headerVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { delay(50L); headerVisible = true }
                AnimatedVisibility(headerVisible, enter = fadeIn(tween(400)) + slideInVertically { it / 4 }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = group?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (!group?.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = group?.description ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${group?.memberCount ?: members.size} miembros",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                } // end AnimatedVisibility header
            }

            // Botón "Ver curso" para grupos CLASSROOM con anio vinculado
            val linkedAnioId = group?.linkedAnioId?.takeIf { it.isNotBlank() }
            if (isClassroom && linkedAnioId != null) {
                item {
                    Button(
                        onClick = {
                            if (isOwner && onNavigateToAnio != null) onNavigateToAnio(linkedAnioId)
                            else showCourseSheet = true
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver curso", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Código de invitación (visible para todos los miembros)
            if (!group?.inviteCode.isNullOrBlank()) {
                item {
                    Text(
                        text = stringResource(R.string.group_invite_code),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(
                                text = "Comparte este código con tus compañeros",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = group?.inviteCode ?: "",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { showQrSheet = true }) {
                                    Icon(
                                        Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        stringResource(R.string.action_share),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Miembros
            item {
                Text(
                    text = "Miembros (${members.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (members.isEmpty()) {
                item {
                    Text(
                        text = "Cargando miembros...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        members.forEachIndexed { index, member ->
                            val roleLabel = when (member.role) {
                                GroupRole.OWNER.name -> stringResource(R.string.group_role_owner)
                                GroupRole.ADMIN.name -> stringResource(R.string.group_role_admin)
                                else -> stringResource(R.string.group_role_member)
                            }
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = member.displayName ?: "Usuario",
                                        fontWeight = if (member.uid == userId) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                supportingContent = { Text(roleLabel) },
                                leadingContent = {
                                    Box(modifier = Modifier.size(40.dp)) {
                                        if (!member.photoUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = member.photoUrl,
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                androidx.compose.foundation.layout.Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        text = (member.displayName ?: "?").take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                            if (index < members.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Asignaturas compartidas
            item {
                Text(
                    text = "Asignaturas compartidas (${sharedSubjects.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (sharedSubjects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.group_no_subjects),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isAdmin) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Comparte una asignatura desde la pantalla de notas para que tus compañeros puedan importar su estructura.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(sharedSubjects, key = { _, subject -> subject.id ?: subject.hashCode().toString() }) { index, subject ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { delay(index * 70L + 60L); visible = true }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)) + slideInVertically { it / 3 }
                    ) {
                        SharedSubjectCard(
                            subject = subject,
                            isAdmin = isAdmin,
                            onClick = { selectedSubject = subject },
                            onDelete = { subjectToDelete = subject }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
                    } // end LazyColumn (Resumen)
                } // end when(selectedTab)
            } // end Column
        } // end BoxWithConstraints
    }

    selectedSubject?.let { subject ->
        SharedSubjectDetailDialog(
            subject = subject,
            canImport = !subject.sharedBy.isNullOrBlank() && subject.sharedBy != userId,
            onImport = {
                importTarget = subject
                selectedSubject = null
            },
            onDismiss = { selectedSubject = null }
        )
    }

    if (showCourseSheet) {
        GroupCourseSheet(
            groupName = group?.name ?: "",
            sharedSubjects = sharedSubjects,
            onDismiss = { showCourseSheet = false }
        )
    }

    if (showQrSheet && !group?.inviteCode.isNullOrBlank()) {
        InviteQrSheet(
            inviteCode = group?.inviteCode ?: "",
            onDismiss = { showQrSheet = false },
            onCopied = {
                clipboard.setText(AnnotatedString(group?.inviteCode ?: ""))
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.group_invite_copied)) }
            }
        )
    }

    importTarget?.let { subject ->
        val uid = userId
        if (uid != null) {
            SubjectImportSheet(
                uid = uid,
                sourceGroupId = groupId,
                shared = subject,
                plan = userPlan,
                actorName = myMember?.displayName,
                actorPhotoUrl = myMember?.photoUrl,
                onPaywall = {
                    importTarget = null
                    onPaywall()
                },
                onDismiss = { importTarget = null },
                onImported = { message ->
                    importTarget = null
                    scope.launch { snackbarHostState.showSnackbar(message) }
                }
            )
        }
    }

    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingSubject) subjectToDelete = null },
            title = { Text("¿Eliminar asignatura compartida?") },
            text = {
                Text("Se eliminará \"${subject.name ?: "esta asignatura"}\" del grupo. Los miembros ya no podrán verla. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sid = subject.id ?: return@Button
                        val uid = userId ?: return@Button
                        isDeletingSubject = true
                        eliminarAsignaturaCompartida(
                            groupId = groupId,
                            subjectId = sid,
                            actorUid = uid,
                            actorName = myMember?.displayName,
                            actorPhotoUrl = myMember?.photoUrl,
                            subjectName = subject.name
                        ) { success ->
                            isDeletingSubject = false
                            subjectToDelete = null
                            if (!success) {
                                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.state_error)) }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !isDeletingSubject
                ) {
                    if (isDeletingSubject) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onError, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.action_delete))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }, enabled = !isDeletingSubject) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("¿Salir del grupo?") },
            text = { Text("Dejarás de tener acceso al contenido del grupo.") },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = userId ?: return@Button
                        isLeaving = true
                        salirDeGrupo(
                            uid = uid,
                            groupId = groupId,
                            actorName = myMember?.displayName,
                            actorPhotoUrl = myMember?.photoUrl
                        ) { success ->
                            isLeaving = false
                            showLeaveDialog = false
                            if (success) onBack()
                            else scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.state_error)) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isLeaving) CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                    else Text(stringResource(R.string.action_leave))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun SharedSubjectCard(
    subject: GroupSharedSubject,
    isAdmin: Boolean = false,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val media = subject.media
    val mediaColor = when {
        media == null -> colorScheme.onSurfaceVariant
        media >= 7.0 -> colorScheme.tertiary
        media >= 5.0 -> colorScheme.primary
        else -> colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sharer avatar
            Box(modifier = Modifier.size(40.dp)) {
                if (!subject.sharedByPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = subject.sharedByPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        color = colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = (subject.sharedByName ?: "?").take(1).uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = subject.name ?: "Asignatura",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "${subject.tipoPeriodo ?: "Trimestre"} · ${subject.numeroPeriodos ?: 3} periodos",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
                if (!subject.sharedByName.isNullOrBlank()) {
                    Text(
                        text = subject.sharedByName!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Grade badge
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

            if (isAdmin && onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar asignatura",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SharedSubjectDetailDialog(
    subject: GroupSharedSubject,
    canImport: Boolean = false,
    onImport: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val media = subject.media
    val mediaColor = when {
        media == null -> colorScheme.onSurfaceVariant
        media >= 7.0 -> colorScheme.tertiary
        media >= 5.0 -> colorScheme.primary
        else -> colorScheme.error
    }
    val dateStr = subject.sharedAt?.let {
        SimpleDateFormat("dd MMM yyyy", Locale("es", "ES")).format(Date(it))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = subject.name ?: "Asignatura",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Grade display — always visible
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nota media",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (media != null) String.format("%.1f", media) else "--",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (media != null) mediaColor else colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                        )
                    }
                }

                // Subject info
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tipo", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        Text(subject.tipoPeriodo ?: "Trimestre", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Periodos", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                        Text("${subject.numeroPeriodos ?: 3}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Sharer info
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(36.dp)) {
                        if (!subject.sharedByPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = subject.sharedByPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                color = colorScheme.secondaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = (subject.sharedByName ?: "?").take(1).uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    Column {
                        Text(
                            text = subject.sharedByName ?: "Usuario",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (dateStr != null) {
                            Text(
                                text = "Compartido el $dateStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (canImport) {
                Button(onClick = onImport) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_copy))
                }
            } else {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
            }
        },
        dismissButton = if (canImport) {
            { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) } }
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCourseSheet(
    groupName: String,
    sharedSubjects: List<GroupSharedSubject>,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface
            )
            Text(
                text = "Asignaturas compartidas por el profesor",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )
            HorizontalDivider(color = cs.outlineVariant)

            if (sharedSubjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El profesor aún no ha compartido asignaturas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sharedSubjects, key = { it.id ?: it.hashCode().toString() }) { subject ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = cs.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .androidx.compose.foundation.background(
                                            cs.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = cs.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name ?: "Asignatura",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = cs.onSurface
                                    )
                                    Text(
                                        text = "${subject.tipoPeriodo ?: "Trimestre"} · ${subject.numeroPeriodos ?: 3} periodos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = cs.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
