package com.example.edutrack.Notas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.domain.UserPlan

@Preview
@Composable
fun previewEvolutionSection() {
    val notas = listOf(
        Notas("1", "a", "Parcial 1", 6.5, 20.0, "10/01/2025", 1, 1000L),
        Notas("2", "a", "Parcial 2", 7.0, 30.0, "20/02/2025", 1, 2000L),
        Notas("3", "a", "Final",     5.5, 50.0, "15/03/2025", 1, 3000L),
        Notas("4", "a", "Parcial 1", 8.0, 40.0, "10/04/2025", 2, 4000L),
        Notas("5", "a", "Final",     6.0, 60.0, "20/05/2025", 2, 5000L),
    )
    EvolucionSection(notas, UserPlan.PREMIUM, {})
}

@Composable
fun EvolucionSection(
    notas: List<Notas>,
    userPlan: UserPlan,
    onPaywall: () -> Unit
) {
    if (userPlan != UserPlan.PREMIUM) {
        LockedEvolucionCard(onPaywall)
        return
    }

    // Solo notas con fecha de creación válida, agrupadas por período
    val byPeriodo = remember(notas) {
        notas
            .filter { it.creadoEn > 0L }
            .groupBy { it.periodo ?: 1 }
            .filterValues { it.isNotEmpty() }
            .mapValues { (_, list) -> list.sortedBy { it.creadoEn } }
            .toSortedMap()
    }
    val hasEnoughData = byPeriodo.values.any { it.size >= 1 }
    if (!hasEnoughData) return

    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Botón toggle ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ShowChart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        "Evolución de notas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Gráfico desplegable ───────────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(tween(200)),
                exit = shrinkVertically() + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    EvolucionMultiPeriodoChart(byPeriodo = byPeriodo)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chart: una sección por período, línea punto-a-punto dentro de cada sección
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EvolucionMultiPeriodoChart(byPeriodo: Map<Int, List<Notas>>) {
    val cs = MaterialTheme.colorScheme
    val periodColors = listOf(cs.primary, cs.tertiary, cs.secondary, cs.error)

    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val progress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(1200),
        label = "chartProgress"
    )

    val periodos = byPeriodo.keys.toList()
    val maxNotasPorPeriodo = byPeriodo.values.maxOf { it.size }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        val w = size.width
        val h = size.height
        val padTop = 12.dp.toPx()
        val padBottom = 28.dp.toPx()   // espacio para etiquetas de período
        val padH = 8.dp.toPx()
        val chartH = h - padTop - padBottom

        val numPeriodos = periodos.size
        val sectionW = (w - 2 * padH) / numPeriodos.toFloat()
        val sepW = if (numPeriodos > 1) 12.dp.toPx() else 0f  // espacio entre secciones

        // ── Guías horizontales (0, 5, 10) ────────────────────────────────
        listOf(0f, 5f, 10f).forEach { v ->
            val y = padTop + chartH - (v / 10f) * chartH
            drawLine(
                color = cs.outlineVariant.copy(alpha = 0.3f),
                start = Offset(padH, y),
                end = Offset(w - padH, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // ── Dibujar cada período como segmento independiente ──────────────
        periodos.forEachIndexed { pIdx, periodo ->
            val notas = byPeriodo[periodo] ?: return@forEachIndexed
            val color = periodColors[pIdx.coerceIn(0, periodColors.size - 1)]

            val secLeft = padH + pIdx * sectionW + sepW / 2
            val secRight = padH + (pIdx + 1) * sectionW - sepW / 2

            // Posición X de cada nota dentro de su sección
            val puntos = notas.mapIndexed { i, nota ->
                val x = if (notas.size == 1) (secLeft + secRight) / 2f
                else secLeft + i * (secRight - secLeft) / (notas.size - 1).toFloat()
                val y = padTop + chartH - ((nota.nota?.toFloat() ?: 0f) / 10f) * chartH
                Offset(x, y) to nota
            }

            // Cuántos puntos mostrar según el progreso de animación
            val totalSegmentos = periodos.sumOf { (byPeriodo[it]?.size ?: 1) - 1 }.toFloat()
                .coerceAtLeast(1f)
            val segOffset = periodos.take(pIdx).sumOf { (byPeriodo[it]?.size ?: 1) - 1 }.toFloat()
            val localProgress = ((progress * totalSegmentos - segOffset) / (notas.size - 1).coerceAtLeast(1))
                .coerceIn(0f, 1f)

            if (puntos.isEmpty()) return@forEachIndexed

            // ── Fill degradado bajo la curva ──────────────────────────────
            val fillPath = Path()
            puntos.forEachIndexed { i, (pt, _) ->
                val animatedPt = if (i == 0) pt else {
                    val prev = puntos[i - 1].first
                    val segFrac = ((localProgress * (puntos.size - 1)) - (i - 1)).coerceIn(0f, 1f)
                    Offset(prev.x + (pt.x - prev.x) * segFrac, prev.y + (pt.y - prev.y) * segFrac)
                }
                if (i == 0) fillPath.moveTo(animatedPt.x, animatedPt.y)
                else fillPath.lineTo(animatedPt.x, animatedPt.y)
            }
            val lastAnimPt = puntos.lastOrNull()?.first ?: return@forEachIndexed
            fillPath.lineTo(lastAnimPt.x, padTop + chartH)
            fillPath.lineTo(puntos.first().first.x, padTop + chartH)
            fillPath.close()
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.18f), Color.Transparent),
                    startY = padTop, endY = padTop + chartH
                )
            )

            // ── Línea punto-a-punto ───────────────────────────────────────
            val linePath = Path()
            puntos.forEachIndexed { i, (pt, _) ->
                if (i == 0) {
                    linePath.moveTo(pt.x, pt.y)
                } else {
                    val prev = puntos[i - 1].first
                    val segFrac = ((localProgress * (puntos.size - 1)) - (i - 1)).coerceIn(0f, 1f)
                    if (segFrac > 0f) {
                        val animPt = Offset(prev.x + (pt.x - prev.x) * segFrac, prev.y + (pt.y - prev.y) * segFrac)
                        linePath.lineTo(animPt.x, animPt.y)
                    }
                }
            }
            drawPath(
                linePath,
                color = color,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // ── Puntos (dots) para cada nota animada ──────────────────────
            puntos.forEachIndexed { i, (pt, nota) ->
                val segFrac = ((localProgress * (puntos.size - 1)) - i).coerceIn(0f, 1f)
                    .let { if (i == 0) 1f else (localProgress * (puntos.size - 1) - (i - 1)).coerceIn(0f, 1f) }
                if (localProgress * (puntos.size - 1) < i - 0.01f) return@forEachIndexed

                val dotColor = when {
                    (nota.nota ?: 0.0) >= 7.0 -> cs.tertiary
                    (nota.nota ?: 0.0) >= 5.0 -> color
                    else -> cs.error
                }
                drawCircle(cs.surface, radius = 5.5.dp.toPx(), center = pt)
                drawCircle(dotColor, radius = 3.5.dp.toPx(), center = pt)
                drawCircle(dotColor, radius = 3.5.dp.toPx(), style = Stroke(1.dp.toPx()), center = pt)
            }

            // ── Etiqueta del período en la parte inferior ─────────────────
            val labelX = (secLeft + secRight) / 2f
            val labelY = h - 4.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(
                "P$periodo",
                labelX,
                labelY,
                android.graphics.Paint().apply {
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    this.color = android.graphics.Color.argb(
                        (cs.onSurfaceVariant.alpha * 255).toInt(),
                        (cs.onSurfaceVariant.red * 255).toInt(),
                        (cs.onSurfaceVariant.green * 255).toInt(),
                        (cs.onSurfaceVariant.blue * 255).toInt()
                    )
                }
            )

            // ── Separador vertical entre períodos ─────────────────────────
            if (pIdx < numPeriodos - 1) {
                val sepX = padH + (pIdx + 1) * sectionW
                drawLine(
                    color = cs.outlineVariant.copy(alpha = 0.4f),
                    start = Offset(sepX, padTop),
                    end = Offset(sepX, padTop + chartH),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(3.dp.toPx(), 3.dp.toPx())
                    )
                )
            }
        }
    }
}

@Composable
private fun LockedEvolucionCard(onPaywall: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .blur(6.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.extraLarge
                )
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Evolución de notas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Disponible con Premium",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(onClick = onPaywall, shape = MaterialTheme.shapes.extraLarge) {
                    Text("Premium")
                }
            }
        }
    }
}
