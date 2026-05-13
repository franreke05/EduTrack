package com.example.edutrack.Notas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.edutrack.dataclass.Notas
import com.example.edutrack.domain.UserPlan

@Preview
@Composable
fun previewEvolutionSection(){
    var nota : MutableList<Notas> = mutableListOf()
    nota.add( Notas("1","2","Prueba",6.5,20.2,"",2,22312452))
    nota.add( Notas("2","3","Prueba2",1.5,20.2,"",2,22312452))
    nota.add( Notas("3","4","Prueba3",10.0,20.2,"",2,22312452))
   /*
   *  EvolucionSection(
        notas = nota, userPlan = UserPlan.PREMIUM, onPaywall = {}
    )
   * */
    EvolucionLineChart(nota)
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
    val sorted = remember(notas) {
        notas.filter { it.creadoEn > 0L }.sortedBy { it.creadoEn }
    }
    if (sorted.size < 2) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Evolución de notas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            EvolucionLineChart(notas = sorted)
        }
    }
}

@Composable
private fun EvolucionLineChart(notas: List<Notas>) {
    val colorScheme = MaterialTheme.colorScheme
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val progress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(1400),
        label = "chartDraw"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val w = size.width
        val h = size.height
        val pad = 12.dp.toPx()

        val points = notas.mapIndexed { i, n ->
            val x = if (notas.size == 1) w / 2
            else pad + i * (w - 2 * pad) / (notas.size - 1).toFloat()
            val y = h - pad - ((n.nota?.toFloat() ?: 0f) / 10f) * (h - 2 * pad)
            Offset(x, y)
        }

        listOf(0f, 5f, 10f).forEach { v ->
            val y = h - pad - (v / 10f) * (h - 2 * pad)
            drawLine(
                color = colorScheme.outlineVariant.copy(alpha = 0.4f),
                start = Offset(pad, y),
                end = Offset(w - pad, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val cutoffIndex = ((progress * (points.size - 1)).toInt()).coerceAtMost(points.size - 2)
        val frac = (progress * (points.size - 1)) - cutoffIndex

        val path = Path()
        for (i in 0..cutoffIndex) {
            if (i == 0) path.moveTo(points[i].x, points[i].y)
            else path.lineTo(points[i].x, points[i].y)
        }
        val lastPt = if (cutoffIndex < points.size - 1 && frac > 0f) {
            val p1 = points[cutoffIndex]; val p2 = points[cutoffIndex + 1]
            Offset(p1.x + (p2.x - p1.x) * frac, p1.y + (p2.y - p1.y) * frac)
        } else points[cutoffIndex]
        if (cutoffIndex < points.size - 1) path.lineTo(lastPt.x, lastPt.y)

        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(lastPt.x, h - pad)
        fillPath.lineTo(points[0].x, h - pad)
        fillPath.close()
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(colorScheme.primary.copy(alpha = 0.22f), Color.Transparent),
                startY = 0f, endY = h
            )
        )

        drawPath(
            path = path,
            color = colorScheme.primary,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val drawCount = (cutoffIndex + 1).coerceAtMost(points.size)
        for (i in 0 until drawCount) {
            val nota = notas[i].nota?.toFloat() ?: 0f
            val dotColor = when {
                nota >= 7f -> colorScheme.tertiary
                nota >= 5f -> colorScheme.primary
                else -> colorScheme.error
            }
            drawCircle(color = colorScheme.surface, radius = 5.dp.toPx(), center = points[i])
            drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = points[i])
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
                .height(160.dp)
                .blur(10.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.extraLarge
                )
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "Evolución de notas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Visualiza cómo han evolucionado tus notas con Premium",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onPaywall, shape = MaterialTheme.shapes.extraLarge) {
                    Text("Desbloquear Premium")
                }
            }
        }
    }
}
