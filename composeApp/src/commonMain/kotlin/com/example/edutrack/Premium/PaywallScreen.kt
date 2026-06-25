package com.example.edutrack.Premium

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edutrack.domain.UserPlan
import com.example.edutrack.ui.LocalUserPlan

/* ── Paleta ─────────────────────────────────────────────────── */
private val Indigo900   = Color(0xFF0F1226)
private val Indigo700   = Color(0xFF2E3CC4)
private val Indigo500   = Color(0xFF5868F5)
private val Lavender50  = Color(0xFFF1F0FF)
private val Lavender100 = Color(0xFFE5E3FF)
private val Amber500    = Color(0xFFFFC233)
private val Success500  = Color(0xFF22C55E)
private val Slate500    = Color(0xFF64748B)
private val Slate900    = Color(0xFF0B1020)
private val SurfaceW    = Color(0xFFFFFFFF)
private val BgGradTop   = Color(0xFFEFEFFF)
private val BgGradBot   = Color(0xFFFAFAFF)

private data class Benefit(val icon: ImageVector, val title: String, val subtitle: String)
private data class PlanRow(val label: String, val free: String, val premium: String)

private val benefitList = listOf(
    Benefit(Icons.Rounded.Calculate,          "Simulador completo",    "Calcula la nota que necesitas en cada examen"),
    Benefit(Icons.Rounded.LibraryBooks,       "Cursos ilimitados",     "Añade todas las asignaturas que quieras"),
    Benefit(Icons.Rounded.Groups,             "Grupos de clase",       "Crea y comparte con tus compañeros"),
    Benefit(Icons.Rounded.Insights,           "Estadísticas avanzadas","Visualiza tu evolución académica"),
    Benefit(Icons.Rounded.NotificationsActive,"Recordatorios",         "Nunca olvides un examen importante"),
    Benefit(Icons.Rounded.PictureAsPdf,       "Exportar a PDF",        "Comparte tus notas en un clic"),
    Benefit(Icons.Rounded.Palette,            "Personalización",       "Temas y colores a tu gusto"),
    Benefit(Icons.Rounded.Block,              "Sin anuncios",          "Experiencia 100% limpia")
)

private val planRows = listOf(
    PlanRow("Cursos",          "2 máx.",      "Sin límite"),
    PlanRow("Simulador",       "Solo obj. 5", "Obj. 5 – 10"),
    PlanRow("Grupos",          "Solo unirse", "Crear y unirse"),
    PlanRow("Anuncios",        "Sí",          "No"),
    PlanRow("Exportar PDF",    "—",           "Sí"),
    PlanRow("Stats avanzadas", "—",           "Sí")
)

// TODO (producción): conectar onPurchase con Google Play Billing.
// En beta el estado Premium se escribe manualmente en premiumCacheRef/{uid}/isPremium = true.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    userId: String? = null,
    onDismiss: () -> Unit = {},
    onPurchase: (String) -> Unit = {},
    onRestorePurchase: () -> Unit = {}
) {
    var selectedPlan by remember { mutableStateOf("annual") }
    val userPlan = LocalUserPlan.current
    val isAlreadyPremium = userPlan == UserPlan.PREMIUM

    if (isAlreadyPremium) {
        AlreadyPremiumState(onDismiss = onDismiss)
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {},
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = Slate900)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomCta(
                selectedPlan = selectedPlan,
                onSubscribe = onPurchase,
                onRestore = onRestorePurchase
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgGradTop, BgGradBot)))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            HeroHeader()
            Spacer(Modifier.height(28.dp))
            BenefitsCard()
            Spacer(Modifier.height(20.dp))
            ComparisonCard()
            Spacer(Modifier.height(20.dp))
            PlanSelector(selected = selectedPlan, onSelect = { selectedPlan = it })
            Spacer(Modifier.height(16.dp))
            TrustRow()
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ── Ya Premium ──────────────────────────────────────────────── */
@Composable
private fun AlreadyPremiumState(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradTop, BgGradBot))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null,
                tint = Amber500, modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(16.dp))
            Text("Ya tienes Premium activo", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = Slate900, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Disfruta de todas las funciones sin límites.",
                fontSize = 15.sp, color = Slate500, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
            ) {
                Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            }
        }
    }
}

/* ── Hero ────────────────────────────────────────────────────── */
@Composable
private fun HeroHeader() {
    val infinite = rememberInfiniteTransition(label = "glow")
    val scale by infinite.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(132.dp)
                .background(
                    Brush.radialGradient(colors = listOf(Amber500.copy(alpha = 0.35f), Color.Transparent)),
                    shape = CircleShape
                )
        )
        Box(
            Modifier
                .size((92 * scale).dp)
                .shadow(16.dp, CircleShape, ambientColor = Amber500, spotColor = Amber500)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFE27A), Amber500)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(46.dp))
        }
    }

    Spacer(Modifier.height(20.dp))

    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Indigo900)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text("✦  PREMIUM", color = Color.White, fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
    }

    Spacer(Modifier.height(14.dp))

    Text(
        "Desbloquea el control\ntotal de tus notas",
        textAlign = TextAlign.Center,
        fontSize = 28.sp, lineHeight = 34.sp,
        fontWeight = FontWeight.ExtraBold, color = Slate900
    )
    Spacer(Modifier.height(10.dp))
    Text(
        "Sabe cómo vas, qué necesitas sacar y organiza\ntodo tu curso sin límites.",
        textAlign = TextAlign.Center, fontSize = 15.sp, color = Slate500, lineHeight = 22.sp
    )
}

/* ── Beneficios ──────────────────────────────────────────────── */
@Composable
private fun BenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceW),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Lavender100)
    ) {
        Column(Modifier.padding(18.dp)) {
            benefitList.forEachIndexed { i, b ->
                BenefitRow(b)
                if (i != benefitList.lastIndex) Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun BenefitRow(b: Benefit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(40.dp)
                .background(Lavender50, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(b.icon, contentDescription = null, tint = Indigo500, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(b.title, fontWeight = FontWeight.SemiBold, color = Slate900, fontSize = 15.sp)
            Text(b.subtitle, color = Slate500, fontSize = 13.sp, lineHeight = 18.sp)
        }
        Icon(Icons.Rounded.CheckCircle, contentDescription = null,
            tint = Success500, modifier = Modifier.size(22.dp))
    }
}

/* ── Comparativa ─────────────────────────────────────────────── */
@Composable
private fun ComparisonCard() {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceW),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Lavender100)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Compara planes", fontWeight = FontWeight.Bold,
                    color = Slate900, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text("Gratis", color = Slate500, fontSize = 13.sp,
                    modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                Box(
                    Modifier
                        .width(96.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Indigo500)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Premium", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Lavender100)
            planRows.forEach { r ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(r.label, color = Slate900, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(r.free, color = Slate500, fontSize = 14.sp,
                        modifier = Modifier.width(80.dp), textAlign = TextAlign.Center)
                    Text(r.premium, color = Indigo700, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(96.dp), textAlign = TextAlign.Center)
                }
                HorizontalDivider(color = Lavender100.copy(alpha = 0.6f))
            }
        }
    }
}

/* ── Selector de plan ────────────────────────────────────────── */
@Composable
private fun PlanSelector(selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PlanCard(
            title = "Mensual", price = "2,49 €", unit = "/mes",
            footer = "Cancela cuando quieras",
            selected = selected == "monthly",
            modifier = Modifier.weight(1f),
            onClick = { onSelect("monthly") }
        )
        PlanCard(
            title = "Anual", price = "14,99 €", unit = "/año",
            footer = "Solo 1,25 €/mes",
            selected = selected == "annual",
            badge = "AHORRA 58%",
            modifier = Modifier.weight(1f),
            onClick = { onSelect("annual") }
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    unit: String,
    footer: String,
    selected: Boolean,
    badge: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (selected) Lavender50 else SurfaceW),
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Indigo500 else Lavender100),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, color = Slate500, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text(price, color = Slate900, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(unit, color = Slate500, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Text(footer,
                    color = if (selected) Indigo700 else Slate500,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center)
            }
        }
        if (badge != null) {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Amber500, Color(0xFFFF8A3D))))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ── Social proof ────────────────────────────────────────────── */
@Composable
private fun TrustRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrustItem(Icons.Rounded.Verified, "Datos en la UE", "Servidores RGPD")
        TrustItem(Icons.Rounded.Star,     "Pago seguro",    "Google Play")
        TrustItem(Icons.Rounded.Cancel,   "Sin permanencia", "Cancela cuando quieras")
    }
}

@Composable
private fun TrustItem(icon: ImageVector, title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Indigo500, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Text(subtitle, fontSize = 10.sp, color = Slate500, textAlign = TextAlign.Center)
    }
}

/* ── CTA inferior ────────────────────────────────────────────── */
@Composable
private fun BottomCta(
    selectedPlan: String,
    onSubscribe: (String) -> Unit,
    onRestore: () -> Unit
) {
    Surface(color = SurfaceW, shadowElevation = 12.dp, modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onSubscribe(selectedPlan) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                // Trial como gancho principal: requiere configurar una oferta de
                // prueba gratuita de 7 días en Play Console para edutrack_premium_*.
                // Si no la configuras, cambia este texto por "Suscribirme — …".
                Text(
                    "Empezar 7 días gratis",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (selectedPlan == "annual") "Luego 14,99 €/año · cancela cuando quieras"
                else "Luego 2,49 €/mes · cancela cuando quieras",
                fontSize = 11.sp, color = Slate500, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Restaurar compra",
                fontSize = 12.sp, color = Indigo700,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onRestore() }
            )
        }
    }
}
