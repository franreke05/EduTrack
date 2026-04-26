package com.example.edutrack.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edutrack.data.firebase.FirebasePaths
import com.example.edutrack.data.groups.GroupRepository
import com.example.edutrack.data.groups.GroupResult
import com.example.edutrack.dataclass.Group
import com.example.edutrack.dataclass.GroupGradePrivacy
import com.example.edutrack.dataclass.GroupSettings
import com.example.edutrack.ui.components.EdutrackButton
import com.example.edutrack.ui.components.EdutrackCard
import com.example.edutrack.ui.components.EmptyState
import com.example.edutrack.ui.components.HeroPanel
import com.example.edutrack.ui.components.LimitReachedDialog
import com.example.edutrack.ui.components.LoadingState
import com.example.edutrack.ui.components.PremiumBadge
import com.example.edutrack.ui.components.ProfessionalTopBar
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.launch

@Composable
fun GroupScreen(
    userId: String?,
    isPremium: Boolean,
    onBack: () -> Unit,
    onCreateGroup: () -> Unit,
    onOpenGroup: (String) -> Unit,
    onPremiumRequested: () -> Unit
) {
    val groups by rememberMyGroupsState(userId)
    val repository = remember { GroupRepository() }
    val scope = rememberCoroutineScope()
    var joinCode by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var showLimit by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ProfessionalTopBar(
                title = "Grupos",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isPremium) onCreateGroup() else showLimit = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Crear grupo")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeroPanel(
                    title = "Espacios de estudio",
                    subtitle = "Crea grupos de clase, carrera o amigos para compartir progreso y preparar rankings.",
                    icon = Icons.Default.Groups,
                    trailing = { if (isPremium) PremiumBadge() }
                )
            }
            item {
                EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Unirse por código", style = MaterialTheme.typography.titleMedium)
                            if (!isPremium) Text("${groups.size}/1", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { joinCode = it.trim() },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Código o ID del grupo") },
                            singleLine = true
                        )
                        EdutrackButton(
                            text = "Unirme",
                            icon = Icons.Default.Login,
                            onClick = {
                                val uid = userId ?: return@EdutrackButton
                                if (!isPremium && groups.isNotEmpty()) {
                                    showLimit = true
                                    return@EdutrackButton
                                }
                                scope.launch {
                                    when (val result = repository.joinGroup(uid, uid.take(8), joinCode, isPremium)) {
                                        is GroupResult.Success -> {
                                            joinCode = ""
                                            message = "Te has unido al grupo."
                                        }
                                        is GroupResult.Error -> message = result.message
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }

            if (groups.isEmpty()) {
                item {
                    EmptyState(
                        title = "Únete a tu primer grupo",
                        message = "Comparte asignaturas, medias y rankings con tu clase o amigos.",
                        icon = Icons.Default.Groups
                    )
                }
            } else {
                items(groups, key = { it.id.orEmpty() }) { group ->
                    GroupCard(group = group, onOpen = { group.id?.let(onOpenGroup) })
                }
            }
        }
    }

    if (showLimit) {
        LimitReachedDialog(
            title = "Límite de grupos",
            message = "La versión gratis permite unirse a 1 grupo. Premium desbloquea crear y gestionar grupos sin límite práctico.",
            onDismiss = { showLimit = false },
            onUnlockPremium = {
                showLimit = false
                onPremiumRequested()
            }
        )
    }
}

@Composable
fun CreateGroupScreen(
    userId: String?,
    isPremium: Boolean,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    onPremiumRequested: () -> Unit
) {
    val repository = remember { GroupRepository() }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf(GroupGradePrivacy.PRIVATE) }
    var message by remember { mutableStateOf<String?>(null) }
    var showLimit by remember { mutableStateOf(!isPremium) }

    Scaffold(
        topBar = {
            ProfessionalTopBar(
                title = "Crear grupo",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Nuevo grupo", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                            PremiumBadge()
                        }
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                        Text("Privacidad de notas", style = MaterialTheme.typography.titleMedium)
                        GroupGradePrivacy.entries.forEach { option ->
                            TextButton(
                                onClick = { privacy = option },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (privacy == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(option.label, fontWeight = if (privacy == option) FontWeight.SemiBold else FontWeight.Normal)
                            }
                        }
                        EdutrackButton(
                            text = "Crear grupo",
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isPremium,
                            onClick = {
                                val uid = userId ?: return@EdutrackButton
                                scope.launch {
                                    val result = repository.createGroup(
                                        userId = uid,
                                        displayName = uid.take(8),
                                        name = name,
                                        description = description,
                                        settings = GroupSettings(gradePrivacy = privacy.value),
                                        isPremium = isPremium
                                    )
                                    when (result) {
                                        is GroupResult.Success -> onCreated(result.groupId)
                                        is GroupResult.Error -> message = result.message
                                    }
                                }
                            }
                        )
                        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    if (showLimit) {
        LimitReachedDialog(
            title = "Crear grupos es Premium",
            message = "Premium permite crear grupos de clase, carrera o amigos y preparar funciones avanzadas como ranking y chat.",
            onDismiss = {
                showLimit = false
                onBack()
            },
            onUnlockPremium = {
                showLimit = false
                onPremiumRequested()
            }
        )
    }
}

@Composable
fun GroupDetailScreen(
    groupId: String?,
    userId: String?,
    onBack: () -> Unit
) {
    val group by rememberGroupState(groupId)
    Scaffold(
        topBar = {
            ProfessionalTopBar(
                title = group?.name ?: "Grupo",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        if (group == null) {
            LoadingState()
        } else {
            val currentGroup = group ?: return@Scaffold
            LazyColumn(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(currentGroup.name.orEmpty(), style = MaterialTheme.typography.headlineSmall)
                            Text(currentGroup.description.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            AssistChip(
                                onClick = {},
                                label = { Text(GroupGradePrivacy.fromValue(currentGroup.settings?.gradePrivacy).label) }
                            )
                        }
                    }
                }
                item {
                    Text("Miembros", style = MaterialTheme.typography.titleMedium)
                }
                items(currentGroup.members?.values?.toList().orEmpty(), key = { it.userId.orEmpty() }) { member ->
                    EdutrackCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(member.displayName ?: "Miembro", style = MaterialTheme.typography.titleMedium)
                                Text(member.role ?: "member", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (member.userId == userId) {
                                Text("Tú", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(group: Group, onOpen: () -> Unit) {
    EdutrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(group.name ?: "Grupo", style = MaterialTheme.typography.titleMedium)
                Text(group.description ?: "Sin descripción", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${group.members?.size ?: 0} miembros", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = onOpen) { Text("Abrir") }
        }
    }
}

@Composable
private fun rememberMyGroupsState(userId: String?): androidx.compose.runtime.State<List<Group>> {
    val groups = remember { mutableStateOf<List<Group>>(emptyList()) }
    DisposableEffect(userId) {
        if (userId.isNullOrBlank()) {
            groups.value = emptyList()
            onDispose {}
        } else {
            val ref = FirebasePaths.userGroups(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupIds = snapshot.children.mapNotNull { it.key }
                    if (groupIds.isEmpty()) {
                        groups.value = emptyList()
                        return
                    }
                    val loaded = mutableListOf<Group>()
                    groupIds.forEach { groupId ->
                        FirebasePaths.group(groupId).get()
                            .addOnSuccessListener { groupSnapshot ->
                                val group = groupSnapshot.getValue<Group>() ?: return@addOnSuccessListener
                                FirebasePaths.groupMembers(groupId).get().addOnSuccessListener { membersSnapshot ->
                                    if (!membersSnapshot.hasChild(userId)) return@addOnSuccessListener
                                    val members = membersSnapshot.children.mapNotNull { child ->
                                        child.getValue(com.example.edutrack.dataclass.GroupMember::class.java)?.let { member ->
                                            (member.userId ?: child.key)?.let { it to member }
                                        }
                                    }.toMap()
                                    group.id = groupId
                                    loaded.removeAll { it.id == groupId }
                                    loaded.add(group.copy(members = members))
                                    groups.value = loaded.sortedBy { it.name.orEmpty() }
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    groups.value = emptyList()
                }
            }
            ref.addValueEventListener(listener)
            onDispose { ref.removeEventListener(listener) }
        }
    }
    return groups
}

@Composable
private fun rememberGroupState(groupId: String?): androidx.compose.runtime.State<Group?> {
    val group = remember { mutableStateOf<Group?>(null) }
    DisposableEffect(groupId) {
        if (groupId.isNullOrBlank()) {
            group.value = null
            onDispose {}
        } else {
            val ref = FirebasePaths.group(groupId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupIdValue = snapshot.key ?: groupId
                    val baseGroup = snapshot.getValue(Group::class.java)?.also { it.id = groupIdValue }
                    if (baseGroup == null) {
                        group.value = null
                    } else {
                        FirebasePaths.groupMembers(groupIdValue).get().addOnSuccessListener { membersSnapshot ->
                            val members = membersSnapshot.children.mapNotNull { child ->
                                child.getValue(com.example.edutrack.dataclass.GroupMember::class.java)?.let { member ->
                                    (member.userId ?: child.key)?.let { it to member }
                                }
                            }.toMap()
                            group.value = baseGroup.copy(members = members)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    group.value = null
                }
            }
            ref.addValueEventListener(listener)
            onDispose { ref.removeEventListener(listener) }
        }
    }
    return group
}
