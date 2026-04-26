package com.example.edutrack.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.edutrack.Inicio.formatMedia
import com.example.edutrack.Inicio.rememberAniosState
import com.example.edutrack.data.export.AcademicPdfExporter
import com.example.edutrack.domain.stats.calculateAcademicStats
import com.example.edutrack.ui.components.EdutrackButton
import com.example.edutrack.ui.components.EmptyState
import com.example.edutrack.ui.components.LimitReachedDialog
import com.example.edutrack.ui.components.ProfessionalTopBar
import com.example.edutrack.ui.components.StatCard
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(
    userId: String?,
    isPremium: Boolean,
    onBack: () -> Unit,
    onPremiumRequested: () -> Unit
) {
    val courses by rememberAniosState(userId)
    val stats = remember(courses) { calculateAcademicStats(courses) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProfessionalTopBar(
                title = "Estadísticas",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (courses.isEmpty()) {
                item {
                    EmptyState(
                        title = "Crea tu primer curso",
                        message = "Las estadísticas aparecerán cuando tengas cursos y asignaturas.",
                        icon = Icons.Default.Analytics
                    )
                }
            } else {
                item { StatCard("Cursos", stats.courseCount.toString(), icon = Icons.Default.School, modifier = Modifier.fillMaxWidth()) }
                item { StatCard("Asignaturas", stats.subjectCount.toString(), icon = Icons.Default.Book, modifier = Modifier.fillMaxWidth()) }
                item { StatCard("Media general", stats.average?.let { formatMedia(it) } ?: "--", icon = Icons.Default.Analytics, modifier = Modifier.fillMaxWidth()) }
                item { StatCard("Porcentaje usado medio", "${formatMedia(stats.usedPercentageAverage)}%", modifier = Modifier.fillMaxWidth()) }
                item {
                    Text(
                        "Mejor asignatura: ${stats.bestSubject?.nombre ?: "--"} · Peor asignatura: ${stats.worstSubject?.nombre ?: "--"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    EdutrackButton(
                        text = "Exportar PDF",
                        icon = Icons.Default.PictureAsPdf,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (!isPremium) {
                                onPremiumRequested()
                                return@EdutrackButton
                            }
                            val file = AcademicPdfExporter(context).exportSummary(courses)
                            scope.launch { snackbarHostState.showSnackbar("PDF generado: ${file.name}") }
                        }
                    )
                }
            }
        }
    }

    if (!isPremium) {
        LimitReachedDialog(
            title = "Estadísticas avanzadas",
            message = "Premium desbloquea estadísticas completas y exportación PDF.",
            onDismiss = onBack,
            onUnlockPremium = onPremiumRequested
        )
    }
}
