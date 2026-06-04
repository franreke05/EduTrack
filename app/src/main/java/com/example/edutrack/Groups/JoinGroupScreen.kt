package com.example.edutrack.Groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.Premium.UpgradeSheet
import com.example.edutrack.domain.PlanManager
import com.example.edutrack.ui.LocalUserGroups
import com.example.edutrack.ui.LocalUsuario
import com.example.edutrack.ui.LocalUserPlan
import com.example.edutrack.unirseAGrupoPorCodigo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnirseGrupoScreen(
    userId: String?,
    onBack: () -> Unit = {},
    onPaywall: () -> Unit = {},
    onGroupJoined: () -> Unit = {}
) {
    val userGroups = LocalUserGroups.current
    val userPlan = LocalUserPlan.current
    val usuario = LocalUsuario.current

    var showUpgrade by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val canJoin = PlanManager.canJoinMoreGroups(userPlan, userGroups.size)

    // Animation states
    val iconScale = remember { Animatable(0f) }
    var titleVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(200L); titleVisible = true
        delay(80L); cardVisible = true
        delay(80L); buttonVisible = true
    }

    // Mostrar upgrade si el usuario alcanzó el límite de grupos.
    if (!canJoin || showUpgrade) {
        UpgradeSheet(
            title = "Desbloquea más grupos",
            message = "Con el plan gratis puedes unirte a 1 grupo. Premium desbloquea más grupos y funciones avanzadas.",
            onUpgrade = onPaywall,
            onDismiss = { showUpgrade = false; onBack() }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Unirme a un grupo", fontWeight = FontWeight.Bold) },
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                val isTablet = maxWidth > 600.dp
                val contentModifier = if (isTablet)
                    Modifier
                        .widthIn(max = 480.dp)
                        .align(Alignment.Center)
                else
                    Modifier.fillMaxWidth()

                Column(
                    modifier = contentModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Spring-animated group icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .graphicsLayer {
                                scaleX = iconScale.value
                                scaleY = iconScale.value
                            }
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Title + subtitle stagger entrance
                    AnimatedVisibility(
                        visible = titleVisible,
                        enter = fadeIn(tween(350)) + slideInVertically { it / 3 }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Únete a tu clase",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Introduce el código de invitación que te ha compartido el creador del grupo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Card input section stagger entrance
                    AnimatedVisibility(
                        visible = cardVisible,
                        enter = fadeIn(tween(350)) + slideInVertically { it / 4 }
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = inviteCode,
                                    onValueChange = { v -> inviteCode = v.uppercase().take(6) },
                                    label = { Text("Código de invitación") },
                                    placeholder = { Text("Ej. ABC123") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.large,
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                    isError = inviteCode.length in 1..5
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (inviteCode.length in 1..5) {
                                        Text(
                                            "Código incompleto",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        Spacer(Modifier.weight(1f))
                                    }
                                    Text(
                                        "${inviteCode.length} / 6",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (inviteCode.length == 6)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Button stagger entrance
                    AnimatedVisibility(
                        visible = buttonVisible,
                        enter = fadeIn(tween(300)) + slideInVertically { it / 5 }
                    ) {
                        Button(
                            onClick = {
                                if (inviteCode.length != 6) {
                                    scope.launch { snackbarHostState.showSnackbar("El código debe tener 6 caracteres") }
                                    return@Button
                                }
                                val uid = userId
                                    ?: FirebaseAuth.getInstance().currentUser?.uid
                                    ?: return@Button
                                val displayName = usuario?.nombre?.takeIf { it.isNotBlank() }
                                    ?: FirebaseAuth.getInstance().currentUser?.displayName
                                    ?: "Usuario"
                                isLoading = true
                                unirseAGrupoPorCodigo(uid, inviteCode, displayName, usuario?.photoUrl) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        onGroupJoined()
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            enabled = inviteCode.length == 6 && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Unirme al grupo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
