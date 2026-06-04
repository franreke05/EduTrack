package com.example.edutrack.Premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.premiumCacheRef
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.ui.LocalUserPlan

private val benefits = listOf(
    "Simulador completo de nota necesaria",
    "Cursos y asignaturas ilimitadas",
    "Grupos de clase",
    "Estadísticas avanzadas",
    "Recordatorios de exámenes",
    "Exportar a PDF",
    "Personalización avanzada",
    "Sin anuncios"
)

private data class ComparisonFeature(val label: String, val free: String, val premium: String)

private val comparisonFeatures = listOf(
    ComparisonFeature("Cursos", "2 máx.", "Sin límite"),
    ComparisonFeature("Simulador", "Solo obj. 5", "Obj. 5–10"),
    ComparisonFeature("Grupos", "Solo unirse", "Crear y unirse"),
    ComparisonFeature("Anuncios", "Sí", "No"),
    ComparisonFeature("Export PDF", "No", "Sí"),
    ComparisonFeature("Stats avanzadas", "No", "Sí")
)

// TODO (producción): reemplazar onPurchase con integración real de Google Play Billing.
// En beta, el estado Premium se escribe manualmente en premiumCacheRef/{uid}/isPremium = true.
@Composable
fun PaywallScreen(
    userId: String? = null,
    onDismiss: () -> Unit = {},
    onPurchase: (String) -> Unit = {},
    onRestorePurchase: () -> Unit = {}
) {
    var selectedPlan by remember { mutableStateOf("annual") }
    var isPurchasing by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val userPlan = LocalUserPlan.current
    val isAlreadyPremium = userPlan == UserPlan.PREMIUM

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primaryContainer.copy(alpha = 0.6f),
                            colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isAlreadyPremium) {
                    Text(
                        text = "Ya tienes Premium activo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Disfruta de todas las funciones sin límites.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Desbloquea el control total de tus notas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sabe cómo vas, qué necesitas sacar y organiza todo tu curso sin límites.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Benefits card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            benefits.forEach { benefit ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = benefit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Comparison table
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "",
                                    modifier = Modifier.weight(2f)
                                )
                                Text(
                                    text = "Gratis",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Premium",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            comparisonFeatures.forEachIndexed { index, feature ->
                                ComparisonRow(feature = feature)
                                if (index < comparisonFeatures.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = colorScheme.outlineVariant.copy(alpha = 0.25f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Plan selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlanCard(
                            modifier = Modifier.weight(1f),
                            label = "Mensual",
                            price = "2,49 €",
                            period = "/mes",
                            perMonthEquivalent = null,
                            isSelected = selectedPlan == "monthly",
                            badge = null,
                            onClick = { selectedPlan = "monthly" }
                        )
                        PlanCard(
                            modifier = Modifier.weight(1f),
                            label = "Anual",
                            price = "14,99 €",
                            period = "/año",
                            perMonthEquivalent = "1,25 €/mes",
                            isSelected = selectedPlan == "annual",
                            badge = "Mejor opción",
                            onClick = { selectedPlan = "annual" }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // TODO: Integrar con Google Play Billing cuando esté disponible
                    Button(
                        onClick = {
                            // Placeholder: en producción, abrirá Google Play Billing
                            // La suscripción se valida server-side mediante Cloud Function
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        enabled = false
                    ) {
                        Text(
                            text = "Billing en construcción",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Las suscripciones se gestionan directamente en Google Play. Pronto esta funcionalidad estará disponible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    TextButton(
                        onClick = onRestorePurchase,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Restaurar compra",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Cancela cuando quieras desde Google Play.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(feature: ComparisonFeature) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature.label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(2f),
            color = colorScheme.onSurface
        )
        Text(
            text = feature.free,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = feature.premium,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlanCard(
    modifier: Modifier,
    label: String,
    price: String,
    period: String,
    perMonthEquivalent: String?,
    isSelected: Boolean,
    badge: String?,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.large
            ),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorScheme.primaryContainer else colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(colorScheme.primary)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
            )
            Text(
                text = price,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface
            )
            Text(
                text = period,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) colorScheme.onPrimaryContainer.copy(0.7f) else colorScheme.onSurfaceVariant
            )
            if (perMonthEquivalent != null) {
                Text(
                    text = perMonthEquivalent,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) colorScheme.onPrimaryContainer.copy(0.8f) else colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
