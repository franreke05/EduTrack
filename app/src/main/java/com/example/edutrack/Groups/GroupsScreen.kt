package com.example.edutrack.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.Premium.UpgradeSheet
import com.example.edutrack.dataclass.GroupRole
import com.example.edutrack.dataclass.UserGroup
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.ui.LocalUserGroups
import com.example.edutrack.ui.LocalUserPlan
import com.example.edutrack.gestures.swipeBackGesture
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

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
    val userGroups = LocalUserGroups.current
    val userPlan = LocalUserPlan.current
    var showCreateUpgrade by remember { mutableStateOf(false) }
    var showJoinUpgrade by remember { mutableStateOf(false) }

    val isPremium = PlanManager.isPremium(userPlan)
    val maxJoinable = if (isPremium) null else 1

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
                .padding(innerPadding)
                .swipeBackGesture(onBack),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                // Patrón tablet: limitar ancho del contenido y centrarlo (igual que CreateGroupScreen).
                val isTablet = maxWidth > 600.dp
                val hPad = if (isTablet) (maxWidth - 560.dp) / 2 else 0.dp

                if (userGroups.isEmpty()) {
                    GruposEmptyState(
                        userPlan = userPlan,
                        horizontalPadding = hPad,
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = if (isTablet) hPad else 16.dp,
                                end = if (isTablet) hPad else 16.dp,
                                top = 8.dp,
                                bottom = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Hero / header informativo con contador y chip de plan.
                            item {
                                GruposHeroHeader(
                                    groupsCount = userGroups.size,
                                    userPlan = userPlan,
                                    maxJoinable = maxJoinable,
                                    onUpgrade = onPaywall
                                )
                            }

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

                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }

                        // Sticky bottom bar con ambas acciones diferenciadas.
                        GruposBottomActionBar(
                            userPlan = userPlan,
                            horizontalPadding = if (isTablet) hPad else 16.dp,
                            onUnirse = {
                                if (!PlanManager.canJoinMoreGroups(userPlan, userGroups.size)) showJoinUpgrade = true
                                else onUnirseGrupo()
                            },
                            onCrear = {
                                if (!PlanManager.canCreateGroup(userPlan)) showCreateUpgrade = true
                                else onCrearGrupo()
                            }
                        )
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

// Header con resumen visual: contador grande de grupos + chip de plan (incentivo de upgrade).
@Composable
private fun GruposHeroHeader(
    groupsCount: Int,
    userPlan: UserPlan,
    maxJoinable: Int?,
    onUpgrade: () -> Unit
) {
    val isPremium = PlanManager.isPremium(userPlan)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Workspaces,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$groupsCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (groupsCount == 1) "grupo activo" else "grupos activos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                PlanChip(
                    isPremium = isPremium,
                    groupsCount = groupsCount,
                    maxJoinable = maxJoinable,
                    onUpgrade = onUpgrade
                )
            }
        }
    }
}

@Composable
private fun PlanChip(
    isPremium: Boolean,
    groupsCount: Int,
    maxJoinable: Int?,
    onUpgrade: () -> Unit
) {
    val label = if (isPremium) {
        "Premium • Ilimitado"
    } else {
        "Plan Free • $groupsCount/${maxJoinable ?: 1} grupos"
    }
    val bg = if (isPremium) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surface
    val fg = if (isPremium) MaterialTheme.colorScheme.onTertiaryContainer
        else MaterialTheme.colorScheme.onSurface
    Surface(
        shape = CircleShape,
        color = bg,
        onClick = { if (!isPremium) onUpgrade() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Default.Star else Icons.Default.Lock,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}

// Hash determinista del nombre del grupo -> color HSL estable para diferenciación visual.
private fun colorForGroupName(name: String?): Color {
    if (name.isNullOrBlank()) return Color(0xFF6B7AFF)
    val hue = (name.hashCode().absoluteValue % 360).toFloat()
    return hslToColor(hue, 0.58f, 0.55f)
}

private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val hp = h / 60f
    val x = c * (1f - kotlin.math.abs(hp % 2f - 1f))
    val (r1, g1, b1) = when {
        hp < 1f -> Triple(c, x, 0f)
        hp < 2f -> Triple(x, c, 0f)
        hp < 3f -> Triple(0f, c, x)
        hp < 4f -> Triple(0f, x, c)
        hp < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = l - c / 2f
    return Color(r1 + m, g1 + m, b1 + m)
}

@Composable
private fun GrupoCard(userGroup: UserGroup, onClick: () -> Unit) {
    val roleLabel = when (userGroup.role) {
        GroupRole.OWNER.name -> "Propietario"
        GroupRole.ADMIN.name -> "Admin"
        else -> "Miembro"
    }
    val roleIcon: ImageVector = when (userGroup.role) {
        GroupRole.OWNER.name -> Icons.Default.Star
        GroupRole.ADMIN.name -> Icons.Default.Shield
        else -> Icons.Default.People
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

    // Gradiente determinista por nombre para que cada grupo tenga identidad visual única.
    val baseColor = colorForGroupName(userGroup.name)
    val avatarGradient = Brush.linearGradient(
        listOf(baseColor, baseColor.copy(alpha = 0.65f))
    )
    val initials = (userGroup.name ?: "G")
        .trim()
        .split(" ", "-")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "G" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(avatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = userGroup.name ?: "Grupo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(color = roleBg, shape = CircleShape) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = null,
                            tint = roleOnBg,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = roleOnBg
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Sticky bottom bar con jerarquía clara: secundario (unirse) + primario (crear).
@Composable
private fun GruposBottomActionBar(
    userPlan: UserPlan,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    onUnirse: () -> Unit,
    onCrear: () -> Unit
) {
    val canCreate = PlanManager.canCreateGroup(userPlan)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onUnirse,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unirme", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onCrear,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canCreate) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (canCreate) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (canCreate) Icons.Default.Add else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GruposEmptyState(
    userPlan: UserPlan,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    onUnirse: () -> Unit,
    onCrear: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val canCreate = PlanManager.canCreateGroup(userPlan)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(450)) + scaleIn(initialScale = 0.92f, animationSpec = tween(450))
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .padding(horizontal = (horizontalPadding + 24.dp).coerceAtLeast(24.dp), vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Ilustración decorativa: stack de tarjetas falsas + avatares (sin datos reales).
                EmptyIllustration()

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tu primera clase te espera",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Únete con un código de tu profesor o crea un grupo para compartir asignaturas, porcentajes y exámenes con tu clase.",
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
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unirme con código", fontWeight = FontWeight.SemiBold)
                }
                FilledTonalButton(
                    onClick = onCrear,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    if (!canCreate) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (canCreate) "Crear grupo" else "Crear grupo (Premium)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Ilustración estática: tres "cards" apiladas en abanico + cluster de avatares circulares.
@Composable
private fun EmptyIllustration() {
    Box(
        modifier = Modifier
            .size(width = 220.dp, height = 140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Card trasera izquierda
        Box(
            modifier = Modifier
                .offset(x = (-56).dp, y = 8.dp)
                .size(width = 110.dp, height = 88.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f))
        )
        // Card trasera derecha
        Box(
            modifier = Modifier
                .offset(x = 56.dp, y = 8.dp)
                .size(width = 110.dp, height = 88.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
        )
        // Card frontal
        Box(
            modifier = Modifier
                .size(width = 140.dp, height = 104.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                // Cluster decorativo de "miembros"
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    AvatarDot(MaterialTheme.colorScheme.primary)
                    AvatarDot(MaterialTheme.colorScheme.tertiary)
                    AvatarDot(MaterialTheme.colorScheme.secondary)
                    AvatarDot(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun AvatarDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}
