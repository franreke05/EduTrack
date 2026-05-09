package com.example.edutrack.Groups

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
                        if (!PlanManager.canJoinMoreGroups(userPlan, 0)) {
                            showJoinUpgrade = true
                        } else {
                            onUnirseGrupo()
                        }
                    },
                    onCrear = {
                        if (!PlanManager.canCreateGroup(userPlan)) {
                            showCreateUpgrade = true
                        } else {
                            onCrearGrupo()
                        }
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(userGroups, key = { it.groupId ?: it.hashCode().toString() }) { userGroup ->
                        GrupoCard(
                            userGroup = userGroup,
                            onClick = { userGroup.groupId?.let { onGrupoDetalle(it) } }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Botón Unirse
                        OutlinedButton(
                            onClick = {
                                if (!PlanManager.canJoinMoreGroups(userPlan, userGroups.size)) {
                                    showJoinUpgrade = true
                                } else {
                                    onUnirseGrupo()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unirme con código")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón Crear
                        Button(
                            onClick = {
                                if (!PlanManager.canCreateGroup(userPlan)) {
                                    showCreateUpgrade = true
                                } else {
                                    onCrearGrupo()
                                }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
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
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userGroup.name ?: "Grupo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val roleLabel = when (userGroup.role) {
                    GroupRole.OWNER.name -> "Propietario"
                    GroupRole.ADMIN.name -> "Administrador"
                    else -> "Miembro"
                }
                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GruposEmptyState(
    userPlan: UserPlan,
    onUnirse: () -> Unit,
    onCrear: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
