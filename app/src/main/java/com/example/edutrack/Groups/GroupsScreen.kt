package com.example.edutrack.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.Premium.UpgradeSheet
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.dataclass.UserGroup
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.domain.rememberUserPlan
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposScreen(
    userId: String?,
    onBack: () -> Unit = {},
    onPaywall: () -> Unit = {},
    onCrearGrupo: () -> Unit = {},
    onUnirseGrupo: () -> Unit = {},
    onGrupoDetalle: (String) -> Unit = {}
) {
    val userGroups by rememberUserGroupsState(userId)
    val userPlan by rememberUserPlan(userId)
    var showCreateUpgrade by remember { mutableStateOf(false) }
    var showJoinUpgrade by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis grupos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (userGroups.isEmpty()) {
                GruposEmptyState(
                    userPlan = userPlan,
                    onUnirse = {
                        if (!PlanManager.canJoinMoreGroups(userPlan, 0)) showJoinUpgrade = true
                        else onUnirseGrupo()
                    },
                    onCrear = {
                        if (!PlanManager.canCreateGroup(userPlan)) showCreateUpgrade = true
                        else onCrearGrupo()
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        userGroups,
                        key = { _, g -> g.groupId ?: g.hashCode().toString() }
                    ) { index, userGroup ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 70L + 60L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(300)) + slideInVertically { it / 3 }
                        ) {
                            GrupoCard(
                                userGroup = userGroup,
                                onClick = { userGroup.groupId?.let { onGrupoDetalle(it) } }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (!PlanManager.canJoinMoreGroups(userPlan, userGroups.size)) showJoinUpgrade = true
                                else onUnirseGrupo()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unirme con código")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (!PlanManager.canCreateGroup(userPlan)) showCreateUpgrade = true
                                else onCrearGrupo()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (PlanManager.canCreateGroup(userPlan))
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            if (!PlanManager.canCreateGroup(userPlan)) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                "Crear grupo",
                                color = if (PlanManager.canCreateGroup(userPlan))
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateUpgrade) {
        UpgradeSheet(
            title = "Crea tus propios grupos con Premium",
            message = "Organiza tu clase, comparte asignaturas y ayuda a tus compañeros a configurar porcentajes sin hacerlo todo a mano.",
            onUpgrade = onPaywall,
            onDismiss = { showCreateUpgrade = false }
        )
    }

    if (showJoinUpgrade) {
        UpgradeSheet(
            title = "Desbloquea más grupos",
            message = "Con el plan gratis puedes unirte a 1 grupo. Premium desbloquea más grupos y funciones avanzadas.",
            onUpgrade = onPaywall,
            onDismiss = { showJoinUpgrade = false }
        )
    }
}

@Composable
private fun GrupoCard(userGroup: UserGroup, onClick: () -> Unit) {
    val roleLabel = when (userGroup.role) {
        GroupRole.OWNER.name -> "Propietario"
        GroupRole.ADMIN.name -> "Admin"
        else -> "Miembro"
    }
    val roleBg = when (userGroup.role) {
        GroupRole.OWNER.name -> MaterialTheme.colorScheme.primary
        GroupRole.ADMIN.name -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val roleOnBg = when (userGroup.role) {
        GroupRole.OWNER.name -> MaterialTheme.colorScheme.onPrimary
        GroupRole.ADMIN.name -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userGroup.name ?: "Grupo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(color = roleBg, shape = MaterialTheme.shapes.small) {
                    Text(
                        text = roleLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = roleOnBg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun GruposEmptyState(
    userPlan: UserPlan,
    onUnirse: () -> Unit,
    onCrear: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(450)) + scaleIn(initialScale = 0.92f, animationSpec = tween(450))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "👥", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = "Todavía no estás en ningún grupo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Únete a tu clase o crea un grupo para compartir asignaturas y porcentajes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onUnirse,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unirme con código")
                }
                OutlinedButton(
                    onClick = onCrear,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    if (!PlanManager.canCreateGroup(userPlan)) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Crear grupo")
                }
            }
        }
    }
}
