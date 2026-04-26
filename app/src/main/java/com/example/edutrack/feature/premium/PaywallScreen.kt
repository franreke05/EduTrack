package com.example.edutrack.feature.premium

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edutrack.data.premium.BillingRepository
import com.example.edutrack.data.premium.PremiumProduct
import com.example.edutrack.ui.components.EdutrackButton
import com.example.edutrack.ui.components.EdutrackCard
import com.example.edutrack.ui.components.HeroPanel
import com.example.edutrack.ui.components.PremiumBadge
import com.example.edutrack.ui.components.ProfessionalTopBar
import kotlinx.coroutines.launch

private data class PremiumBenefit(val icon: ImageVector, val text: String)

@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    billingRepository: BillingRepository = rememberBillingRepository()
) {
    val state by billingRepository.state.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        billingRepository.connectAndRefresh()
    }

    Scaffold(
        topBar = {
            ProfessionalTopBar(
                title = "Edutrack Premium",
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
                HeroPanel(
                    title = "Más capacidad, menos ruido.",
                    subtitle = "Premium elimina anuncios y desbloquea cursos, grupos, estadísticas, recordatorios, PDF y personalización avanzada.",
                    icon = Icons.Default.WorkspacePremium,
                    trailing = { PremiumBadge() }
                )
            }

            items(
                listOf(
                    PremiumBenefit(Icons.Default.Block, "Sin anuncios"),
                    PremiumBenefit(Icons.Default.WorkspacePremium, "Cursos y asignaturas ilimitadas"),
                    PremiumBenefit(Icons.Default.Psychology, "Simulador de nota necesaria"),
                    PremiumBenefit(Icons.Default.Analytics, "Estadísticas avanzadas"),
                    PremiumBenefit(Icons.Default.Notifications, "Recordatorios académicos"),
                    PremiumBenefit(Icons.Default.PictureAsPdf, "Exportar resumen a PDF"),
                    PremiumBenefit(Icons.Default.Style, "Personalización avanzada"),
                    PremiumBenefit(Icons.Default.Groups, "Crear y gestionar grupos")
                )
            ) { benefit ->
                BenefitRow(benefit)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val products = state.products
                    if (products.isEmpty()) {
                        ProductPlaceholder(title = "Premium mensual", price = "2,49 €", period = "al mes")
                        ProductPlaceholder(title = "Premium anual", price = "14,99 €", period = "al año")
                    } else {
                        products.forEach { product ->
                            ProductCard(
                                product = product,
                                enabled = activity != null && !state.isLoading,
                                onBuy = { activity?.let { billingRepository.launchPurchase(it, product) } }
                            )
                        }
                    }
                    EdutrackButton(
                        text = "Restaurar compras",
                        onClick = { scope.launch { billingRepository.refreshPurchases(showMessage = true) } },
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        secondary = true
                    )
                }
            }

            state.message?.let { message ->
                item {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitRow(benefit: PremiumBenefit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(benefit.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(benefit.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ProductCard(
    product: PremiumProduct,
    enabled: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium)
                Text("${product.price} ${product.periodLabel}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (product.id.endsWith("yearly")) {
                    Text("Mejor valor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            EdutrackButton("Elegir", onClick = onBuy, enabled = enabled)
        }
    }
}

@Composable
private fun ProductPlaceholder(title: String, price: String, period: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("$price $period", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Pendiente", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun rememberBillingRepository(): BillingRepository {
    val context = LocalContext.current.applicationContext
    return remember { BillingRepository(context) }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
